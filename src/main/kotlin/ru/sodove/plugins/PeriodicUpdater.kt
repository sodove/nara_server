package ru.sodove.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction0

fun Application.startPeriodicTask(func: KSuspendFunction0<Unit>, delay: Long, delay_start: Boolean = false, delay_start_length: Long = 0) {
    log.info("Starting periodic task (delay: $delay ms, task: $func)")
    CoroutineScope(Dispatchers.IO).launch {
        if (delay_start)
            delay(delay_start_length)
        func()
        delay(delay)
        startPeriodicTask(func, delay, delay_start)
    }
}
