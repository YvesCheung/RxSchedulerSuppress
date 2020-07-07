package com.huya.rxjava2.schedulers.suppress.reactive;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.reactivex.Completable;
import io.reactivex.Scheduler;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public class CompletableHelper {

    public static List<String> testCompletableInSpecialScheduler(Scheduler scheduler) throws InterruptedException {
        List<String> threadNameRecord = new ArrayList<>(4);
        CountDownLatch runBlocking = new CountDownLatch(1);

        Completable
            .complete()
            .subscribeOn(scheduler)
            .doOnComplete(() -> threadNameRecord.add(Thread.currentThread().getName()))
            .observeOn(scheduler)
            .doOnComplete(() -> threadNameRecord.add(Thread.currentThread().getName()))
            .observeOn(scheduler)
            .doOnComplete(() -> threadNameRecord.add(Thread.currentThread().getName()))
            .observeOn(scheduler)
            .subscribe(runBlocking::countDown, throwable -> {
                runBlocking.countDown();
                Assert.fail(throwable.getMessage());
            });

        runBlocking.await();
        return threadNameRecord;
    }
}
