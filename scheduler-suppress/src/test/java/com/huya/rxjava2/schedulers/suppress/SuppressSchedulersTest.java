package com.huya.rxjava2.schedulers.suppress;


import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static com.huya.rxjava2.schedulers.suppress.util.Utils.forEachReactiveX;
import static org.hamcrest.Matchers.not;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public class SuppressSchedulersTest {

    @Before
    public void cleanAllPlugins() {
        RxJavaPlugins.setIoSchedulerHandler(null);
        RxJavaPlugins.setComputationSchedulerHandler(null);
    }

    @Test
    public void testSuppressIo() throws InterruptedException {

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setIoSchedulerHandler(SchedulerSuppress.SuppressIo);

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, allElementIsTheSame()));
    }

    @Test
    public void testSuppressBackgroundIo() throws InterruptedException {

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setIoSchedulerHandler(SchedulerSuppress.SuppressBackground);

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, allElementIsTheSame()));
    }

    @Test
    public void testSuppressCompute() throws InterruptedException {

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setComputationSchedulerHandler(SchedulerSuppress.SuppressCompute);

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, allElementIsTheSame()));
    }

    @Test
    public void testSuppressBackgroundCompute() throws InterruptedException {

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setComputationSchedulerHandler(SchedulerSuppress.SuppressBackground);

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, allElementIsTheSame()));
    }

    @Test
    public void testComputeNotSuppressIo() throws InterruptedException {

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setComputationSchedulerHandler(SchedulerSuppress.SuppressIo);

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setComputationSchedulerHandler(SchedulerSuppress.SuppressCompute);

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setComputationSchedulerHandler(SchedulerSuppress.SuppressBackground);

        forEachReactiveX(Schedulers.io(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));
    }

    @Test
    public void testIoNotSuppressCompute() throws InterruptedException {

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setIoSchedulerHandler(SchedulerSuppress.SuppressIo);

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setIoSchedulerHandler(SchedulerSuppress.SuppressCompute);

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));

        RxJavaPlugins.setIoSchedulerHandler(SchedulerSuppress.SuppressBackground);

        forEachReactiveX(Schedulers.computation(), threadRecord ->
            Assert.assertThat(threadRecord, not(allElementIsTheSame())));
    }

    private static <E> Matcher<List<E>> allElementIsTheSame() {
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
