package com.huya.rxjava2.schedulers.suppress.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.reactivex.Scheduler;

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
