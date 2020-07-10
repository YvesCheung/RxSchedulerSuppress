package com.huya.rxjava3.schedulers.suppress;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public final class ImmediateScheduler extends Scheduler {

    private final Scheduler actual;

    private final Predicate<Thread> runInCurrentThread;

    public ImmediateScheduler(@NonNull Scheduler schedulerImpl, @NonNull Predicate<Thread> shouldJustRunInCurrentThread) {
        actual = Objects.requireNonNull(schedulerImpl);
        runInCurrentThread = Objects.requireNonNull(shouldJustRunInCurrentThread);
    }

    @Override
    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable run) {
        if (predicate(runInCurrentThread)) {
            return TrampolineScheduler.instance().scheduleDirect(run);
        } else {
            return actual.scheduleDirect(run);
        }
    }

    @Override
    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
        if (predicate(runInCurrentThread)) {
            return TrampolineScheduler.instance().scheduleDirect(run, delay, unit);
        } else {
            return actual.scheduleDirect(run, delay, unit);
        }
    }

    @Override
    @NonNull
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable run, long initialDelay, long period, @NonNull TimeUnit unit) {
        if (predicate(runInCurrentThread)) {
            return TrampolineScheduler.instance().schedulePeriodicallyDirect(run, initialDelay, period, unit);
        } else {
            return actual.schedulePeriodicallyDirect(run, initialDelay, period, unit);
        }
    }

    @Override
    @NonNull
    public Worker createWorker() {
        return new ImmediateWorker(actual, runInCurrentThread);
    }

    //<editor-fold desc="Delegate method">
    @Override
    public void start() {
        actual.start();
    }

    @Override
    public void shutdown() {
        actual.shutdown();
    }

    @Override
    public long now(@NonNull TimeUnit unit) {
        return actual.now(unit);
    }

    @Override
    public <S extends Scheduler & Disposable> S when(@NonNull Function<Flowable<Flowable<Completable>>, Completable> combine) {
        return actual.when(combine);
    }
    //</editor-fold>

    private static final class ImmediateWorker extends Scheduler.Worker {

        private final Predicate<Thread> shouldJustRunInCurrentThread;
        private final Worker currentThreadWorker = TrampolineScheduler.instance().createWorker();
        private final Worker actualWorker;

        ImmediateWorker(Scheduler actual, Predicate<Thread> shouldJustRunInCurrentThread) {
            this.shouldJustRunInCurrentThread = shouldJustRunInCurrentThread;
            this.actualWorker = actual.createWorker();
        }

        @Override
        @NonNull
        public Disposable schedule(@NonNull Runnable run) {
            return predicate(shouldJustRunInCurrentThread)
                ? currentThreadWorker.schedule(run)
                : actualWorker.schedule(run);
        }

        @Override
        @NonNull
        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            return predicate(shouldJustRunInCurrentThread)
                ? currentThreadWorker.schedule(run, delay, unit)
                : actualWorker.schedule(run, delay, unit);
        }

        @Override
        @NonNull
        public Disposable schedulePeriodically(@NonNull Runnable run, long initialDelay, long period, @NonNull TimeUnit unit) {
            return predicate(shouldJustRunInCurrentThread)
                ? currentThreadWorker.schedulePeriodically(run, initialDelay, period, unit)
                : actualWorker.schedulePeriodically(run, initialDelay, period, unit);
        }

        @Override
        public void dispose() {
            currentThreadWorker.dispose();
            actualWorker.dispose();
        }

        @Override
        public boolean isDisposed() {
            return actualWorker.isDisposed();
        }

        @Override
        public long now(@NonNull TimeUnit unit) {
            return actualWorker.now(unit);
        }
    }

    static boolean predicate(@NonNull Predicate<Thread> predicate) {
        boolean inCurrent = false;
        try {
            inCurrent = predicate.test(Thread.currentThread());
        } catch (Throwable e) {
            RxJavaPlugins.onError(e);
        }
        return inCurrent;
    }
}
