package com.huya.rxjava2.schedulers.suppress.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

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

    public static <E> Matcher<List<E>> allElementIsTheSame() {
        return new BaseMatcher<List<E>>() {

            @SuppressWarnings("unchecked")
            @Override
            public boolean matches(Object item) {
                return item instanceof Collection &&
                    new HashSet<>((Collection<E>) item).size() == 1;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("all element is the same");
            }
        };
    }
}
