package com.huya.rxjava3.schedulers.suppress;

import android.os.Looper;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;


/**
 * @author YvesCheung
 * 2020/7/9
 */
@RunWith(AndroidJUnit4.class)
public class AndroidSuppressSchedulersTest {

    @Before
    public void clearRxAndroidPlugins() {
        RxAndroidPlugins.setMainThreadSchedulerHandler(null);
    }

    @Test
    @UiThreadTest
    public void testMainThreadScheduler() {
        final AtomicInteger a = new AtomicInteger(0);

        AndroidSchedulers.mainThread().scheduleDirect(() -> a.set(1));
        Assert.assertEquals(a.get(), 0);

        AndroidSchedulerSuppress.SuppressMain();

        AndroidSchedulers.mainThread().scheduleDirect(() -> a.set(2));
        Assert.assertEquals(a.get(), 2);
    }

    @Test
    @UiThreadTest
    public void testMainThreadSchedulerDelay() {
        final AtomicInteger a = new AtomicInteger(0);

        AndroidSchedulers.mainThread().scheduleDirect(() -> a.set(1), 1, TimeUnit.MILLISECONDS);
        Assert.assertEquals(a.get(), 0);

        AndroidSchedulers.mainThread().schedulePeriodicallyDirect(() -> a.set(2), 0, 1, TimeUnit.MILLISECONDS);
        Assert.assertEquals(a.get(), 0);

        AndroidSchedulerSuppress.SuppressMain();

        AndroidSchedulers.mainThread().scheduleDirect(() -> a.set(3), 1, TimeUnit.MILLISECONDS);
        Assert.assertEquals(a.get(), 0);

        AndroidSchedulers.mainThread().schedulePeriodicallyDirect(() -> a.set(4), 0, 1, TimeUnit.MILLISECONDS);
        Assert.assertEquals(a.get(), 0);
    }

    @Test
    @UiThreadTest
    public void testNestedMainThreadScheduler() {
        final AtomicInteger a = new AtomicInteger(0);

        AndroidSchedulerSuppress.SuppressMain();

        AndroidSchedulers.mainThread().scheduleDirect(() ->
            AndroidSchedulers.mainThread().scheduleDirect(() ->
                AndroidSchedulers.mainThread().scheduleDirect(() ->
                    AndroidSchedulers.mainThread().scheduleDirect(() ->
                        AndroidSchedulers.mainThread().scheduleDirect(() ->
                            AndroidSchedulers.mainThread().scheduleDirect(() ->
                                AndroidSchedulers.mainThread().scheduleDirect(() -> a.set(1))
                            )
                        )
                    )
                )
            )
        );

        Assert.assertEquals(a.get(), 1);
    }

    @Test
    public void testIoToMainThread() throws InterruptedException {
        final AtomicInteger a = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(1);

        AndroidSchedulerSuppress.SuppressMain();

        Assert.assertNotEquals(Looper.myLooper(), Looper.getMainLooper());

        AndroidSchedulers.mainThread().scheduleDirect(() -> {
            Assert.assertEquals(Looper.myLooper(), Looper.getMainLooper());
            a.set(1);
            latch.countDown();
        });

        latch.await();

        Assert.assertEquals(a.get(), 1);
    }
}
