package com.manidigit.yadin.domain.util

import android.util.Log
import kotlin.coroutines.cancellation.CancellationException

inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

inline fun <reified T : Enum<T>> safeEnum(value: String?, fallback: T): T {
    if (value == null) return fallback
    return enumValues<T>().firstOrNull { it.name.equals(value, ignoreCase = true) }
        ?: fallback.also { Log.w("SafeEnum", "Unknown enum ${T::class.simpleName}=$value, using fallback $fallback") }
}
