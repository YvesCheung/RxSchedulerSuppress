package com.huya.rxjava2.schedulers.suppress;


import com.huya.rxjava2.schedulers.suppress.util.Pair;
import com.huya.rxjava2.schedulers.suppress.util.Utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

/**
 * @author YvesCheung
 * 2020/7/6
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SchedulerSuppressTest {

    @Test
    public void testNoSuppress() throws InterruptedException {
        assetThreadPoolIsRight();
    }

    private void assetThreadPoolIsRight() throws InterruptedException {
        List<Pair<String, Scheduler>> nameAndScheduler =
            Arrays.asList(
                new Pair<>("RxCachedThreadScheduler-", Schedulers.io()),
                new Pair<>("RxComputationThreadPool-", Schedulers.computation()),
                new Pair<>("RxNewThreadScheduler-", Schedulers.newThread()),
                new Pair<>("main", Schedulers.trampoline()),
                new Pair<>("RxSingleScheduler-", Schedulers.single())
            );
        for (Pair<String, Scheduler> p : nameAndScheduler) {
            String name = p.first;
            Scheduler scheduler = p.second;

            List<String> threadRecord =
                testObservableInSpecialScheduler(scheduler);
            System.out.println("record = " + threadRecord);
            Assert.assertTrue(Utils.all(threadRecord, s -> s.startsWith(name)));
        }
    }

    @Test
    public void testSuppressIo() throws InterruptedException {
        List<String> threadRecord;

        threadRecord = testObservableInSpecialScheduler(Schedulers.io());
        System.out.println("record = " + threadRecord);
        threadRecord = testFlowableInSpecialScheduler(Schedulers.io());
        System.out.println("record = " + threadRecord);

        Assert.assertTrue(new HashSet<>(threadRecord).size() > 1);

        RxJavaPlugins.setIoSchedulerHandler(SchedulerSuppress.SuppressIo);

        threadRecord =
            testObservableInSpecialScheduler(Schedulers.io());
        System.out.println("record = " + threadRecord);
        Assert.assertEquals(1, new HashSet<>(threadRecord).size());
        threadRecord = testFlowableInSpecialScheduler(Schedulers.io());
        System.out.println("record = " + threadRecord);
        Assert.assertEquals(1, new HashSet<>(threadRecord).size());
    }

    private List<String> testObservableInSpecialScheduler(Scheduler scheduler) throws InterruptedException {
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

    private List<String> testFlowableInSpecialScheduler(Scheduler scheduler) throws InterruptedException {
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
