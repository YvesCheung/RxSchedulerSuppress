package com.huya.rxjava2.schedulers.suppress.reactive;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.reactivex.Scheduler;
import io.reactivex.Single;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public class SingleHelper {

    public static List<String> testSingleInSpecialScheduler(Scheduler scheduler) throws InterruptedException {
        List<String> threadNameRecord = new ArrayList<>(4);
        CountDownLatch runBlocking = new CountDownLatch(1);

        Single
            .create(emitter -> {
                threadNameRecord.add(Thread.currentThread().getName());
                emitter.onSuccess("Single");
            })
            .subscribeOn(scheduler)
            .map(s -> {
                threadNameRecord.add(Thread.currentThread().getName());
                return "map " + s;
            })
            .observeOn(scheduler)
            .doOnSuccess(s -> threadNameRecord.add(Thread.currentThread().getName()))
            .observeOn(scheduler)
            .map(s -> {
                threadNameRecord.add(Thread.currentThread().getName());
                return "map " + s;
            })
            .observeOn(scheduler)
            .subscribe(s -> {
                runBlocking.countDown();
                Assert.assertEquals("map map Single", s);
            }, throwable -> {
                runBlocking.countDown();
                Assert.fail(throwable.getMessage());
            });

        runBlocking.await();
        return threadNameRecord;
    }
}
