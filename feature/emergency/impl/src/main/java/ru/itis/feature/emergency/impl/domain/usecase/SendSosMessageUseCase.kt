package ru.itis.feature.emergency.impl.domain.usecase

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.SosSettingsRepository
import ru.itis.core.utils.BusinessErrorCode
import ru.itis.core.utils.OperationResult
import javax.inject.Inject
import kotlin.coroutines.resume

internal class SendSosMessageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SosSettingsRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {
    @SuppressLint("MissingPermission")
    suspend operator fun invoke(): OperationResult<Unit> = withContext(dispatcher) {
        try {
            // Проверяем наличие необходимых разрешений
            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            val hasLocationPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
            Log.i("EMERGENCY_SOS_DEBUG", "sms permission: $hasSmsPermission, location permission: $hasLocationPermission, phone state permission: $hasPhoneStatePermission")

            if (!hasSmsPermission || !hasLocationPermission || !hasPhoneStatePermission) {
                Log.e("EMERGENCY_SOS_DEBUG", "Missing permissions")
                return@withContext OperationResult.Error(
                    OperationResult.ErrorType.Business(BusinessErrorCode.SOS_PERMISSIONS_DENIED)
                )
            }

            // Получаем текущие настройки (берем первое значение из Flow)
            val currentSettings = repository.getSettings().first()
            Log.i("EMERGENCY_SOS_DEBUG", "Current settings. Message: ${currentSettings.message}, phones size: ${currentSettings.trustedContacts.size}")

            // Если список контактов пуст, SMS не отправляем
            if (currentSettings.trustedContacts.isEmpty()) {
                Log.e("EMERGENCY_SOS_DEBUG", "No trusted contacts")
                return@withContext OperationResult.Error(
                    OperationResult.ErrorType.Business(BusinessErrorCode.SOS_NO_CONTACTS)
                )
            }

            // Получаем текущие координаты
            val location = getCurrentLocation()
            Log.i("EMERGENCY_SOS_DEBUG", "Current location: ${location?.latitude}, ${location?.longitude}")

            val locationText = if (location != null) {
                //"https://maps.google.com/?q=${location.latitude},${location.longitude}"
                "https://yandex.ru/maps/?pt=${location.longitude},${location.latitude}&z=15&l=map"
            } else {
                Log.i("EMERGENCY_SOS_DEBUG", "No location!")
                return@withContext OperationResult.Error(
                    OperationResult.ErrorType.Business(BusinessErrorCode.SOS_NO_COORDS)
                )
            }

            // Формируем сообщение: если пользователь не задал свое, используем дефолтное
            val message = if (currentSettings.message.isBlank()) {
                "SOS! $locationText"
            } else {
                "${currentSettings.message} $locationText"
            }

            Log.i("EMERGENCY_SOS_DEBUG","SOS message: $message")

            // Отправляем SMS всем доверенным контактам
            val smsManager = context.getSystemService(SmsManager::class.java)
            Log.i("EMERGENCY_SOS_DEBUG","Got sms manager: $smsManager")

            currentSettings.trustedContacts.forEachIndexed { index, phone ->
                try {
                    Log.i("EMERGENCY_SOS_DEBUG", "Sending SMS #$index to: $phone")
                    Log.i("EMERGENCY_SOS_DEBUG", "Message length: ${message.length} chars")

                    smsManager.sendTextMessage(phone, null, message, null, null)

                    Log.i("EMERGENCY_SOS_DEBUG", "SMS #$index queued for delivery to: $phone")
                } catch (e: Exception) {
                    Log.e("EMERGENCY_SOS_DEBUG", "Failed to send SMS #$index to $phone: ${e.message}", e)
                }
            }

            Log.i("EMERGENCY_SOS_DEBUG", "All SMS messages queued for sending")
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("EMERGENCY_SOS_DEBUG", "Sos send failed with exception: ${e.message}")
            OperationResult.Error(
                OperationResult.ErrorType.Business(BusinessErrorCode.SOS_SEND_FAILED)
            )
        }
    }

    // Получаем текущую геопозицию с таймаутом 10 секунд
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location? {
        Log.i("EMERGENCY_SOS_DEBUG", "Starting getCurrentLocation")

        return withTimeoutOrNull(10000L) {
            suspendCancellableCoroutine { continuation ->
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                Log.i("EMERGENCY_SOS_DEBUG", "Got LocationManager: $locationManager")

                // Проверяем доступность провайдеров
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                Log.i("EMERGENCY_SOS_DEBUG", "GPS enabled: $isGpsEnabled, Network enabled: $isNetworkEnabled")

                // Если оба провайдера выключены - сразу возвращаем null
                if (!isGpsEnabled && !isNetworkEnabled) {
                    Log.e("EMERGENCY_SOS_DEBUG", "Both GPS and Network providers are disabled!")
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                    return@suspendCancellableCoroutine
                }

                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        Log.i("EMERGENCY_SOS_DEBUG", "New location received: lat=${location.latitude}, lon=${location.longitude}, provider=${location.provider}")
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                        Log.i("EMERGENCY_SOS_DEBUG", "Provider status changed: $provider, status: $status")
                    }

                    override fun onProviderEnabled(provider: String) {
                        Log.i("EMERGENCY_SOS_DEBUG", "Provider enabled: $provider")
                    }

                    override fun onProviderDisabled(provider: String) {
                        Log.i("EMERGENCY_SOS_DEBUG", "Provider disabled: $provider")
                    }
                }

                try {
                    // Получаем последние известные локации
                    val lastKnownGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val lastKnownNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    Log.i("EMERGENCY_SOS_DEBUG", "Last GPS location: ${lastKnownGps?.let { "lat=${it.latitude}, lon=${it.longitude}, time=${it.time}" } ?: "null"}")
                    Log.i("EMERGENCY_SOS_DEBUG", "Last Network location: ${lastKnownNetwork?.let { "lat=${it.latitude}, lon=${it.longitude}, time=${it.time}" } ?: "null"}")

                    // Выбираем самую свежую локацию
                    val lastKnown = listOfNotNull(lastKnownGps, lastKnownNetwork)
                        .maxByOrNull { it.time }

                    // Проверяем актуальность последней локации (не старше 2 минут)
                    if (lastKnown != null) {
                        val ageMillis = System.currentTimeMillis() - lastKnown.time
                        val ageSeconds = ageMillis / 1000

                        Log.i("EMERGENCY_SOS_DEBUG", "Last known location age: ${ageSeconds}s")

                        if (ageMillis < 120_000) { // 2 минуты
                            Log.i("EMERGENCY_SOS_DEBUG", "Using fresh last known location (age: ${ageSeconds}s)")
                            if (continuation.isActive) {
                                continuation.resume(lastKnown)
                            }
                            return@suspendCancellableCoroutine
                        } else {
                            Log.i("EMERGENCY_SOS_DEBUG", "Last known location is stale (age: ${ageSeconds}s), requesting fresh location")
                        }
                    } else {
                        Log.i("EMERGENCY_SOS_DEBUG", "No last known location available")
                    }

                    // Запрашиваем обновления локации
                    Log.i("EMERGENCY_SOS_DEBUG", "Requesting location updates")

                    // передаём Looper.getMainLooper(), чтобы колбэки выполнялись на главном потоке
                    val mainLooper = Looper.getMainLooper()

                    // Сначала Network (быстрее, работает в помещении)
                    if (isNetworkEnabled) {
                        Log.i("EMERGENCY_SOS_DEBUG", "Requesting Network location updates")
                        locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0L,
                            0f,
                            locationListener,
                            mainLooper
                        )
                    }

                    // Затем GPS (точнее, но медленнее)
                    if (isGpsEnabled) {
                        Log.i("EMERGENCY_SOS_DEBUG", "Requesting GPS location updates")
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0L,
                            0f,
                            locationListener,
                            mainLooper
                        )
                    }

                    // Отменяем подписку при отмене корутины
                    continuation.invokeOnCancellation {
                        Log.i("EMERGENCY_SOS_DEBUG", "Coroutine cancelled, removing location updates")
                        locationManager.removeUpdates(locationListener)
                    }
                } catch (e: Exception) {
                    Log.e("EMERGENCY_SOS_DEBUG", "Exception in location request: ${e.message}", e)
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }.also { result ->
            Log.i("EMERGENCY_SOS_DEBUG", "GetCurrentLocation completed: ${if (result != null) "SUCCESS (lat=${result.latitude}, lon=${result.longitude})" else "TIMEOUT/NULL"}")
        }
    }
}