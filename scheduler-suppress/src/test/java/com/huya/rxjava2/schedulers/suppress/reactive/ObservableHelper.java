package com.huya.rxjava2.schedulers.suppress.reactive;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public class ObservableHelper {

    public static List<String> testObservableInSpecialScheduler(Scheduler scheduler) throws InterruptedException {
        List<String> threadNameRecord = new ArrayList<>(5);
        CountDownLatch runBlocking = new CountDownLatch(1);
        Observable
            .create(emitter -> {
                threadNameRecord.add(Thread.currentThread().getName());
                emitter.onNext("Observable");
            })
            .subscribeOn(scheduler)
            .map(s -> {
                threadNameRecord.add(Thread.currentThread().getName());
                return "map " + s;
            })
            .observeOn(scheduler)
            .doOnNext(s -> threadNameRecord.add(Thread.currentThread().getName()))
            .observeOn(scheduler)
            .map(s -> {
                threadNameRecord.add(Thread.currentThread().getName());
                return "map " + s;
            })
            .observeOn(scheduler)
            .subscribe(s -> {
                runBlocking.countDown();
                Assert.assertEquals("map map Observable", s);
            }, throwable -> {
                runBlocking.countDown();
                Assert.fail(throwable.getMessage());
            });

        runBlocking.await();
        return threadNameRecord;
    }
}
