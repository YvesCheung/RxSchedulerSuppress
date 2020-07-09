package com.huya.rxjava2.schedulers.suppress;

import com.huya.rxjava2.schedulers.suppress.util.Pair;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import static com.huya.rxjava2.schedulers.suppress.util.Utils.forEachReactiveX;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.startsWith;

/**
 * @author YvesCheung
 * 2020/7/7
 */
@RunWith(Parameterized.class)
public class SuppressThreadFactoryTest {

    @Parameterized.Parameters
    public static Iterable<Runnable> dataSet() {
        return Arrays.asList(
            SchedulerSuppress::SuppressIo,
            SchedulerSuppress::SuppressCompute,
            SchedulerSuppress::SuppressBackground
        );
    }

    @Parameterized.Parameter
    public Runnable suppressScheduler;

    @Test
    public void testDoNotChangeTheThreadFactory() throws InterruptedException {

        assertThreadPoolIsRight();

        suppressScheduler.run();

        assertThreadPoolIsRight();
    }

    private void assertThreadPoolIsRight() throws InterruptedException {
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

            forEachReactiveX(scheduler, threadRecord -> {
                System.out.println("record = " + threadRecord);
                Assert.assertThat(threadRecord, everyItem(startsWith(name)));
            });
        }
    }
}
