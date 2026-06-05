package ru.itis.feature.emergency.impl.domain.manager

// Управление воспроизведением аудио при фейковом звонке
interface FakeCallManager {
    fun startIncomingCall()
    fun startConversation()
    fun stopAll()
}