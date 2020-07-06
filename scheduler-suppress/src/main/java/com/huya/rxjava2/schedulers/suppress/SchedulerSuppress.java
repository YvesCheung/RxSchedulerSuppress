package com.huya.rxjava2.schedulers.suppress;

import io.reactivex.Scheduler;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public final class SchedulerSuppress {

    private SchedulerSuppress() {
        throw new IllegalStateException("No instances!");
    }

    public static final Function<? super Scheduler, ? extends Scheduler> SuppressIo =
        new IoSuppression();

    public static final Function<? super Scheduler, ? extends Scheduler> SuppressCompute =
        new ComputeSuppression();

    public static final Function<? super Scheduler, ? extends Scheduler> SuppressBackground =
        new BackgroundThreadSuppression();

    public static class BackgroundThreadSuppression extends AbstractSuppression {

        public BackgroundThreadSuppression() {
            super();
        }

        public BackgroundThreadSuppression(@Nullable Function<Scheduler, Scheduler> previousTransformer) {
            super(previousTransformer);
        }

        @Override
        boolean shouldJustRunInCurrentThread(Thread thread) {
            String threadName = thread.getName();
            return threadName != null &&
                (threadName.startsWith("RxComputation") || threadName.startsWith("RxCached"));
        }
    }

    public static class ComputeSuppression extends AbstractSuppression {

        public ComputeSuppression() {
            super();
        }

        public ComputeSuppression(@Nullable Function<Scheduler, Scheduler> previousTransformer) {
            super(previousTransformer);
        }

        @Override
        boolean shouldJustRunInCurrentThread(Thread thread) {
            String threadName = thread.getName();
            return threadName != null && threadName.startsWith("RxComputation");
        }
    }

    public static class IoSuppression extends AbstractSuppression {

        public IoSuppression() {
            super();
        }

        public IoSuppression(@Nullable Function<Scheduler, Scheduler> previousTransformer) {
            super(previousTransformer);
        }

        @Override
        boolean shouldJustRunInCurrentThread(Thread thread) {
            String threadName = thread.getName();
            return threadName != null && threadName.startsWith("RxCached");
        }
    }

    public static abstract class AbstractSuppression implements
        Function<Scheduler, Scheduler>, Predicate<Thread> {

        @Nullable
        private final Function<Scheduler, Scheduler> previousTransformer;

        public AbstractSuppression() {
            this(null);
        }

        public AbstractSuppression(@Nullable Function<Scheduler, Scheduler> previousTransformer) {
            this.previousTransformer = previousTransformer;
        }

        @Override
        public Scheduler apply(Scheduler scheduler) throws Exception {
            return previousTransformer != null
                ? new ImmediateScheduler(previousTransformer.apply(scheduler), this)
                : new ImmediateScheduler(scheduler, this);
        }

        @Override
        public final boolean test(Thread thread) {
            return shouldJustRunInCurrentThread(thread);
        }

        abstract boolean shouldJustRunInCurrentThread(Thread task);
    }
}
