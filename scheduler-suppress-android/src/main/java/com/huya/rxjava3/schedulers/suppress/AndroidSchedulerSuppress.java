package com.huya.rxjava3.schedulers.suppress;

import android.os.Handler;
import android.os.Looper;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.android.schedulers.HandlerImmediateScheduler;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Function;

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
