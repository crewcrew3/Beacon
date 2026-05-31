package ru.itis.data.impl.local.importer

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.bson.BsonBinaryReader
import org.bson.BsonDocument
import org.bson.ByteBufNIO
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.io.ByteBufferBsonInput
import java.io.BufferedInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import ru.itis.data.impl.local.dao.SafetyCameraDao
import ru.itis.data.impl.local.dao.LightingPoleDao
import ru.itis.data.impl.local.entity.LightingPoleEntity
import ru.itis.data.impl.local.entity.SafetyCameraEntity
import java.io.DataInputStream
import java.io.FileNotFoundException
import java.nio.ByteBuffer

/**
 * Импортер данных из BSON-файлов открытых данных Москвы.
 * Запускается при первом запуске приложения в фоне.
 */
@Singleton
internal class BsonDataImporter @Inject constructor(
    private val cameraDao: SafetyCameraDao,
    private val poleDao: LightingPoleDao,
    @ApplicationContext private val context: Context,
) {

    /** Безопасно читает числовое поле как Long (поддержка INT32 и INT64) */
    private fun BsonDocument.getSafeLong(key: String): Long? = try {
        get(key)?.asNumber()?.longValue()
    } catch (e: Exception) { null }

    /** Безопасно читает числовое поле как Int */
    private fun BsonDocument.getSafeInt(key: String): Int? = try {
        get(key)?.asNumber()?.intValue()
    } catch (e: Exception) { null }

    /** Безопасно читает строку (вернёт null, если ключа нет или тип не String) */
    private fun BsonDocument.getSafeString(key: String): String? = try {
        get(key)?.asString()?.value
    } catch (e: Exception) { null }

    companion object {
        private const val TAG = "BsonDataImporter"
        private const val CAMERAS_FILE = "cameras_data.bson"
        private const val POLES_FILE = "poles_data.bson"
    }

    suspend fun importIfNeeded() {
        if (poleDao.getPolesCount() == 0L) {
            Log.i(TAG, "Starting lighting poles import...")
            importLightingPoles()
            Log.i(TAG, "Lighting poles import finished.")
        }

        if (cameraDao.getCamerasCount() == 0L) {
            Log.i(TAG, "Starting cameras import...")
            importCameras()
            Log.i(TAG, "Cameras import finished.")
        }
    }

    private suspend fun importCameras() {
        val inputStream = try {
            context.assets.open(CAMERAS_FILE)
        } catch (e: FileNotFoundException) {
            throw IllegalStateException(
                "File $CAMERAS_FILE not found in assets. " +
                        "Place it in :data:impl/src/main/assets/", e
            )
        }
        parseAndInsertInBatches(
            inputStream = inputStream,
            batchSize = 1000,
            mapper = { doc ->
            try {
                // Обязательные поля (если их нет — документ пропускаем)
                val globalId = doc.getSafeLong("global_id") ?: return@parseAndInsertInBatches null
                val address = doc.getSafeString("Address") ?: return@parseAndInsertInBatches null

                // Опциональные строковые поля
                val district = doc.getSafeString("District")
                val ovdAddress = doc.getSafeString("OVDAddress")

                // Безопасное извлечение вложенного объекта и массива координат
                val geoData = doc.get("geoData")?.asDocument() ?: return@parseAndInsertInBatches null
                val coordinates = geoData.get("coordinates")?.asArray() ?: return@parseAndInsertInBatches null

                if (coordinates.size < 2) return@parseAndInsertInBatches null

                val longitude = coordinates[0].asNumber().doubleValue()
                val latitude = coordinates[1].asNumber().doubleValue()

                SafetyCameraEntity(
                    globalId = globalId,
                    address = address,
                    district = district,
                    latitude = latitude,
                    longitude = longitude,
                    ovdAddress = ovdAddress
                )
            } catch (e: Exception) {
                Log.w(TAG, "Skipping invalid camera document", e)
                null
            }
        },
            insertBatch = { entities -> cameraDao.insertCameras(entities) }
        )
    }

    private suspend fun importLightingPoles() {
        val inputStream = try {
            context.assets.open(POLES_FILE)
        } catch (e: FileNotFoundException) {
            throw IllegalStateException(
                "File $POLES_FILE not found in assets. " +
                        "Place it in :data:impl/src/main/assets/", e
            )
        }

        parseAndInsertInBatches(
            inputStream = inputStream,
            batchSize = 1000,
            mapper = { doc ->
            try {
                // Обязательные поля
                val globalId = doc.getSafeLong("global_id") ?: return@parseAndInsertInBatches null
                val geoData = doc.getDocument("geoData") ?: return@parseAndInsertInBatches null
                val coordinates = geoData.getArray("coordinates") ?: return@parseAndInsertInBatches null

                if (coordinates.size < 2) return@parseAndInsertInBatches null

                // Опциональные поля
                val pillarNumber = doc.getSafeString("PillarNumber")
                val pillarType = doc.getSafeString("PillarType")
                val lightsNumber = doc.getSafeInt("LightsNumber")
                val status = doc.getSafeString("Status")
                val district = doc.getSafeString("District")

                val longitude = coordinates[0].asNumber().doubleValue()
                val latitude = coordinates[1].asNumber().doubleValue()

                LightingPoleEntity(
                    globalId = globalId,
                    pillarNumber = pillarNumber,
                    pillarType = pillarType,
                    lightsNumber = lightsNumber,
                    status = status,
                    latitude = latitude,
                    longitude = longitude,
                    district = district
                )
            } catch (e: Exception) {
                Log.w(TAG, "Skipping invalid pole document", e)
                null
            }
        },
            insertBatch = { entities -> poleDao.insertPoles(entities) }
        )
    }

    private suspend fun <T> parseAndInsertInBatches(
        inputStream: InputStream,
        batchSize: Int = 1000,
        mapper: (BsonDocument) -> T?,
        insertBatch: suspend (List<T>) -> Unit
    ) = withContext(Dispatchers.IO) {
        val batch = mutableListOf<T>()

        try {
            DataInputStream(BufferedInputStream(inputStream)).use { dataStream ->
                val codec = BsonDocumentCodec()
                val decoderContext = DecoderContext.builder().build()

                while (true) {
                    // Читаем 4-байтовый размер документа (little-endian)
                    val docSize = try {
                        val sizeBytes = ByteArray(4)
                        val read = dataStream.read(sizeBytes)
                        if (read < 4) break // Конец файла
                        ByteBuffer.wrap(sizeBytes)
                            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                            .int
                    } catch (e: java.io.EOFException) {
                        break // Достигли конца потока
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to read document size, stopping", e)
                        break
                    }

                    // Валидация размера документа
                    if (docSize !in 5..16_777_216) { // 16MB лимит на документ
                        Log.w(TAG, "Invalid BSON document size: $docSize, stopping")
                        break
                    }

                    // Читаем тело документа (размер минус 4 байта префикса)
                    val remainingBytes = docSize - 4
                    val docBytes = ByteArray(remainingBytes)
                    var totalRead = 0
                    while (totalRead < remainingBytes) {
                        val read = dataStream.read(docBytes, totalRead, remainingBytes - totalRead)
                        if (read == -1) {
                            Log.w(TAG, "Unexpected EOF while reading document body")
                            break
                        }
                        totalRead += read
                    }
                    if (totalRead < remainingBytes) {
                        Log.w(TAG, "Incomplete document: expected $remainingBytes, read $totalRead")
                        break
                    }

                    // Собираем полный буфер документа (размер + тело)
                    val fullDocBuffer = ByteBuffer.allocate(docSize).apply {
                        order(java.nio.ByteOrder.LITTLE_ENDIAN)
                        putInt(docSize) // Записываем размер в little-endian
                        put(docBytes)
                        flip() // Переключаем в режим чтения
                    }

                    try {
                        // Парсим один BSON-документ
                        val byteBuf = ByteBufNIO(fullDocBuffer)
                        val bsonInput = ByteBufferBsonInput(byteBuf)
                        val reader = BsonBinaryReader(bsonInput)

                        val doc = codec.decode(reader, decoderContext)
                        mapper(doc)?.let { batch.add(it) }

                        // Батчевая вставка: когда набрали пачку — сохраняем и очищаем
                        if (batch.size >= batchSize) {
                            insertBatch(batch)
                            batch.clear() // Освобождаем ссылки для сборщика мусора
                            yield()
                        }
                    } catch (e: org.bson.BsonInvalidOperationException) {
                        Log.w(TAG, "Failed to parse BSON document of size $docSize, skipping", e)
                        continue // Пропускаем битый документ и читаем следующий
                    } catch (e: Exception) {
                        Log.w(TAG, "Unexpected error parsing document", e)
                        break // При неизвестной ошибке останавливаем импорт
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Stream parse failed", e)
        } finally {
            // Вставляем оставшиеся записи, если они есть
            if (batch.isNotEmpty()) {
                try {
                    insertBatch(batch)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to insert final batch", e)
                }
            }
        }
    }
}