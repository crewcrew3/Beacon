package ru.itis.data.impl.initializer

import ru.itis.core.domain.initializer.InitialDataLoader
import ru.itis.data.impl.local.importer.BsonDataImporter
import javax.inject.Inject

internal class DataInitializerImpl @Inject constructor(
    private val bsonDataImporter: BsonDataImporter
) : InitialDataLoader {

    override suspend fun initialize() {
        bsonDataImporter.importIfNeeded()
    }
}