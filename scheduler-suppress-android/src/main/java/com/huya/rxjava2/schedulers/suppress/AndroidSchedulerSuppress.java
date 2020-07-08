package com.huya.rxjava2.schedulers.suppress;

import android.os.Handler;
import android.os.Looper;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.HandlerImmediateScheduler;
import io.reactivex.functions.Function;

/**
 * @author YvesCheung
 * 2020/7/8
 */
public class AndroidSchedulerSuppress {

    private AndroidSchedulerSuppress() {
        throw new IllegalStateException("No instances!");
    }

    public static void SuppressMain() {
        RxAndroidPlugins.setMainThreadSchedulerHandler(ReplaceMainScheduler);
    }

    private static Function<Scheduler, Scheduler> ReplaceMainScheduler =
        scheduler -> new HandlerImmediateScheduler(new Handler(Looper.getMainLooper()), false);
}
