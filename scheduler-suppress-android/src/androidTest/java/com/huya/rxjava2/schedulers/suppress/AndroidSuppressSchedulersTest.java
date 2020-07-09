package com.huya.rxjava2.schedulers.suppress;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;

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
        final int[] a = {0};

        AndroidSchedulers.mainThread().scheduleDirect(() -> a[0] = 1);
        Assert.assertEquals(a[0], 0);

        AndroidSchedulerSuppress.SuppressMain();

        AndroidSchedulers.mainThread().scheduleDirect(() -> a[0] = 2);
        Assert.assertEquals(a[0], 2);
    }

    @Test
    @UiThreadTest
    public void testMainThreadSchedulerDelay() {
        final int[] a = {0};

        AndroidSchedulers.mainThread().scheduleDirect(() -> a[0] = 1, 1, TimeUnit.MILLISECONDS);
        Assert.assertEquals(a[0], 0);

        AndroidSchedulers.mainThread().schedulePeriodicallyDirect(() -> a[0] = 3, 0, 1, TimeUnit.MILLISECONDS);
        Assert.assertEquals(a[0], 0);

        AndroidSchedulerSuppress.SuppressMain();

        AndroidSchedulers.mainThread().scheduleDirect(() -> a[0] = 2, 1, TimeUnit.MILLISECONDS);
        Assert.assertEquals(a[0], 0);

        AndroidSchedulers.mainThread().schedulePeriodicallyDirect(() -> a[0] = 3, 0, 1, TimeUnit.MILLISECONDS);
        Assert.assertEquals(a[0], 0);
    }
}
