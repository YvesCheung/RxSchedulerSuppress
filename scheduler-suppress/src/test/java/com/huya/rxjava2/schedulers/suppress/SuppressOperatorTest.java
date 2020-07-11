package com.huya.rxjava2.schedulers.suppress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static com.huya.rxjava2.schedulers.suppress.util.Utils.allElementIsTheSame;
import static org.hamcrest.Matchers.not;

/**
 * @author YvesCheung
 * 2020/7/10
 */
public class SuppressOperatorTest {

    @Before
    public void clearRxJavaPlugins(){
        RxJavaPlugins.reset();
    }

    @Test
    public void testSubscribeOnAndObserveOn() throws Throwable {
        runSuppressBackground(
            testCase ->
                Observable
                    .create(emitter -> {
                        testCase.add(Thread.currentThread().getName());
                        emitter.onNext("Test");
                        emitter.onComplete();
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map(s -> {
                        testCase.add(Thread.currentThread().getName());
                        return s;
                    })
                    .observeOn(Schedulers.io())
                    .flatMapCompletable(s -> {
                        testCase.add(Thread.currentThread().getName());
                        return Completable.complete();
                    })
                    .subscribe(),
            before -> Assert.assertThat(before, not(allElementIsTheSame())),
            after -> Assert.assertThat(after, allElementIsTheSame()));
    }

    @Test
    public void testTimer() throws Throwable {
        runSuppressBackground(
            testCase ->
                Observable.timer(1, TimeUnit.MILLISECONDS)
                    .map(s -> {
                        testCase.add(Thread.currentThread().getName());
                        return s;
                    })
                    .observeOn(Schedulers.io())
                    .subscribe(s ->
                        testCase.add(Thread.currentThread().getName())
                    ),
            before -> Assert.assertThat(before, not(allElementIsTheSame())),
            after -> Assert.assertThat(after, allElementIsTheSame()));
    }

    private static void runSuppressBackground(
        Function<List<String>, Disposable> testCase,
        Consumer<List<String>> before,
        Consumer<List<String>> after
    ) throws Throwable {
        runTest(SchedulerSuppress::SuppressBackground, testCase, before, after);
    }

    private static void runTest(
        Runnable suppressOperation,
        Function<List<String>, Disposable> testCase,
        Consumer<List<String>> before,
        Consumer<List<String>> after
    ) throws Throwable {

        List<String> resultBefore = Collections.synchronizedList(new ArrayList<>());
        Disposable taskBefore = testCase.apply(resultBefore);
        while (!taskBefore.isDisposed()) {
            Thread.sleep(100);
        }
        before.accept(resultBefore);

        suppressOperation.run();

        List<String> resultAfter = Collections.synchronizedList(new ArrayList<>());
        Disposable taskAfter = testCase.apply(resultAfter);
        while (!taskAfter.isDisposed()) {
            Thread.sleep(100);
        }
        after.accept(resultAfter);
    }
}
