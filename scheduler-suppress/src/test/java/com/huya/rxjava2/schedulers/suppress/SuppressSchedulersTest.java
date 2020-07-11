package com.huya.rxjava2.schedulers.suppress;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static com.huya.rxjava2.schedulers.suppress.util.Utils.allElementIsTheSame;
import static com.huya.rxjava2.schedulers.suppress.util.Utils.forEachReactiveX;
import static org.hamcrest.Matchers.not;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public class SuppressSchedulersTest {

    @Before
    public void cleanAllPlugins() {
        RxJavaPlugins.reset();
    }

    @Test
    public void testSuppressIo() throws InterruptedException {

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        SchedulerSuppress.SuppressIo();

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, allElementIsTheSame()));
    }

    @Test
    public void testSuppressBackgroundIo() throws InterruptedException {

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        SchedulerSuppress.SuppressBackground();

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, allElementIsTheSame()));
    }

    @Test
    public void testSuppressCompute() throws InterruptedException {

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        SchedulerSuppress.SuppressCompute();

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, allElementIsTheSame()));
    }

    @Test
    public void testSuppressBackgroundCompute() throws InterruptedException {

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        SchedulerSuppress.SuppressBackground();

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, allElementIsTheSame()));
    }

    @Test
    public void testComputeNotSuppressIo() throws InterruptedException {

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setIoSchedulerHandler(new SchedulerSuppress.ComputeSuppression());

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));
    }

    @Test
    public void testIoNotSuppressCompute() throws InterruptedException {

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setComputationSchedulerHandler(new SchedulerSuppress.IoSuppression());

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));
    }
}
