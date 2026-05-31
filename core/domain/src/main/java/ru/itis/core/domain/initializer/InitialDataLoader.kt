package ru.itis.core.domain.initializer

/**
 * Интерфейс для однократной инициализации локальных справочников.
 * Абстрагирует процесс загрузки от конкретной реализации в :data:impl.
 */
interface InitialDataLoader {
    suspend fun initialize()
}