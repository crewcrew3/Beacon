package ru.itis.core.utils

import kotlin.coroutines.cancellation.CancellationException

inline fun <T, R> T.runCatchingNonCancellable(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
