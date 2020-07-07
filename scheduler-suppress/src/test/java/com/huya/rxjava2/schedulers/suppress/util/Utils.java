package com.huya.rxjava2.schedulers.suppress.util;

import com.huya.rxjava2.schedulers.suppress.SchedulerSuppress;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.reactivex.Scheduler;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static com.huya.rxjava2.schedulers.suppress.reactive.CompletableHelper.testCompletableInSpecialScheduler;
import static com.huya.rxjava2.schedulers.suppress.reactive.FlowableHelper.testFlowableInSpecialScheduler;
import static com.huya.rxjava2.schedulers.suppress.reactive.MaybeHelper.testMaybeInSpecialScheduler;
import static com.huya.rxjava2.schedulers.suppress.reactive.ObservableHelper.testObservableInSpecialScheduler;
import static com.huya.rxjava2.schedulers.suppress.reactive.SingleHelper.testSingleInSpecialScheduler;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public class Utils {

    public static final List<Scheduler> AffectedSchedulers =
        Arrays.asList(Schedulers.io(), Schedulers.computation());

    public static final List<Scheduler> UnAffectedSchedulers =
        Arrays.asList(Schedulers.newThread(), Schedulers.single());

    public static final Runnable SuppressIo =
        () -> RxJavaPlugins.setIoSchedulerHandler(SchedulerSuppress.SuppressIo);

    public static final Runnable SuppressCompute =
        () -> RxJavaPlugins.setComputationSchedulerHandler(SchedulerSuppress.SuppressCompute);

    public static final Runnable SuppressBackground =
        () -> {
            RxJavaPlugins.setIoSchedulerHandler(SchedulerSuppress.SuppressBackground);
            RxJavaPlugins.setComputationSchedulerHandler(SchedulerSuppress.SuppressBackground);
        };

    public static final List<Runnable> SuppressHandler =
        Arrays.asList(SuppressIo, SuppressCompute, SuppressBackground);

    public static <E> boolean all(List<E> list, Predicate<E> predicate) {
        for (E e : list) {
            if (!predicate.test(e)) {
                return false;
            }
        }
        return true;
    }

    public static void forEachReactiveX(
        Scheduler scheduler,
        Consumer<List<String>> forEachActon
    ) throws InterruptedException {
        for (List<String> threadRecord : Arrays.asList(
            testObservableInSpecialScheduler(scheduler),
            testFlowableInSpecialScheduler(scheduler),
            testMaybeInSpecialScheduler(scheduler),
            testSingleInSpecialScheduler(scheduler),
            testCompletableInSpecialScheduler(scheduler))
        ) {
            forEachActon.accept(threadRecord);
        }
    }
}
