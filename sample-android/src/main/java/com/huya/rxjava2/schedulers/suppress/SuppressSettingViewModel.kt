package com.huya.rxjava2.schedulers.suppress

import android.os.Handler
import android.os.Looper
import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.huya.rxjava2.schedulers.suppress.android.sample.R
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.HandlerImmediateScheduler
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.Dispatchers
import kotlin.properties.Delegates

/**
 * @author YvesCheung
 * 2020/7/9
 */
class SuppressSettingViewModel(defaultSwitch: Int = R.id.suppress_coroutine) : ViewModel() {

    @get:IdRes
    var checkedId: Int by Delegates.observable(defaultSwitch) { _, _, new ->
        /**
         * 这里为了展示效果切换，所以用了 set..SchedulerHandler
         * 实际项目中应该使用 setInit..SchedulerHandler
         */
        when (new) {
            R.id.suppress_coroutine -> {
                RxJavaPlugins.setComputationSchedulerHandler {
                    CoroutineScheduler(Dispatchers.Default)
                }
                RxJavaPlugins.setIoSchedulerHandler {
                    CoroutineScheduler(Dispatchers.IO)
                }
                RxAndroidPlugins.setMainThreadSchedulerHandler {
                    CoroutineScheduler(Dispatchers.Main)
                }
            }
            R.id.suppress_immediate -> {
                RxJavaPlugins.setComputationSchedulerHandler {
                    ImmediateScheduler(it, SchedulerSuppress.ComputeSuppression())
                }
                RxJavaPlugins.setIoSchedulerHandler {
                    ImmediateScheduler(it, SchedulerSuppress.IoSuppression())
                }
                RxAndroidPlugins.setMainThreadSchedulerHandler {
                    HandlerImmediateScheduler(Handler(Looper.getMainLooper()), false)
                }
            }
            else -> {
                RxJavaPlugins.setComputationSchedulerHandler(null)
                RxJavaPlugins.setIoSchedulerHandler(null)
                RxAndroidPlugins.setMainThreadSchedulerHandler(null)
            }
        }
    }
}