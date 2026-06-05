package ru.itis.feature.emergency.impl.domain.usecase

import ru.itis.feature.emergency.impl.domain.manager.FakeCallManager
import javax.inject.Inject

internal class StopFakeCallUseCase @Inject constructor(private val fakeCallManager: FakeCallManager) {
    operator fun invoke() = fakeCallManager.stopAll()
}