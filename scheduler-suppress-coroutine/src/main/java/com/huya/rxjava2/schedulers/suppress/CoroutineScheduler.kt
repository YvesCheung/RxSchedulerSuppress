package com.huya.rxjava2.schedulers.suppress

import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * @author YvesCheung
 * 2021/9/10
 */
class CoroutineScheduler(val dispatcher: CoroutineDispatcher) : Scheduler() {

    override fun createWorker(): Worker = CoroutineWorker()

    inner class CoroutineWorker : Worker() {

        private val coroutineScope = DisposableCoroutineScope(SupervisorJob() + dispatcher)

        override fun dispose() = coroutineScope.dispose()

        override fun isDisposed(): Boolean = coroutineScope.isDisposed

        override fun schedule(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
            coroutineScope.launch {
                delay(unit.toMillis(delay))
                run.run()
            }
            return coroutineScope
        }
    }

    class DisposableCoroutineScope(context: CoroutineContext) : Disposable, CoroutineScope {

        override val coroutineContext: CoroutineContext = context

        override fun isDisposed(): Boolean = !coroutineContext.isActive

        override fun dispose() {
            coroutineContext.cancel()
        }
    }
}