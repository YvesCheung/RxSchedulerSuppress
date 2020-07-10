package com.huya.rxjava3.schedulers.suppress.reactive;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;


/**
 * @author YvesCheung
 * 2020/7/6
 */
public class FlowableHelper {

    public static List<String> testFlowableInSpecialScheduler(Scheduler scheduler) throws InterruptedException {
        List<String> threadNameRecord = new ArrayList<>(4);
        CountDownLatch runBlocking = new CountDownLatch(1);

        Flowable
            .create(emitter -> {
                threadNameRecord.add(Thread.currentThread().getName());
                emitter.onNext("Flowable");
            }, BackpressureStrategy.BUFFER)
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
                Assert.assertEquals("map map Flowable", s);
            }, throwable -> {
                runBlocking.countDown();
                Assert.fail(throwable.getMessage());
            });

        runBlocking.await();
        return threadNameRecord;
    }
}
