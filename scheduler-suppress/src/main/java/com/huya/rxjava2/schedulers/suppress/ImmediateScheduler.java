package com.huya.rxjava2.schedulers.suppress;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.schedulers.TrampolineScheduler;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * @author YvesCheung
 * 2020/7/6
 */
public final class ImmediateScheduler extends Scheduler {

    private final Scheduler actual;

    private final Predicate<Thread> runInCurrentThread;

    private final Worker worker;

    public ImmediateScheduler(@NonNull Scheduler schedulerImpl, @NonNull Predicate<Thread> shouldJustRunInCurrentThread) {
        actual = Objects.requireNonNull(schedulerImpl);
        runInCurrentThread = Objects.requireNonNull(shouldJustRunInCurrentThread);
        worker = new ImmediateWorker(actual, runInCurrentThread);
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
        return worker;
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
    //</editor-fold>

    private static class ImmediateWorker extends Scheduler.Worker {

        private final CompositeDisposable tasks = new CompositeDisposable();
        private final Predicate<Thread> shouldJustRunInCurrentThread;
        private final Scheduler actual;
        private final Worker currentThreadWorker =
            TrampolineScheduler.instance().createWorker();

        ImmediateWorker(Scheduler actual, Predicate<Thread> shouldJustRunInCurrentThread) {
            this.actual = actual;
            this.shouldJustRunInCurrentThread = shouldJustRunInCurrentThread;
        }

        @Override
        @NonNull
        public Disposable schedule(@NonNull Runnable run) {
            Disposable task = predicate(shouldJustRunInCurrentThread)
                ? currentThreadWorker.schedule(run)
                : actual.createWorker().schedule(run);
            tasks.add(task);
            return task;
        }

        @Override
        @NonNull
        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            Disposable task = predicate(shouldJustRunInCurrentThread)
                ? currentThreadWorker.schedule(run, delay, unit)
                : actual.createWorker().schedule(run, delay, unit);
            tasks.add(task);
            return task;
        }

        @Override
        @NonNull
        public Disposable schedulePeriodically(@NonNull Runnable run, long initialDelay, long period, @NonNull TimeUnit unit) {
            Disposable task = predicate(shouldJustRunInCurrentThread)
                ? currentThreadWorker.schedulePeriodically(run, initialDelay, period, unit)
                : actual.createWorker().schedulePeriodically(run, initialDelay, period, unit);
            tasks.add(task);
            return task;
        }

        @Override
        public void dispose() {
            tasks.dispose();
        }

        @Override
        public boolean isDisposed() {
            return tasks.isDisposed();
        }

        @Override
        public long now(@NonNull TimeUnit unit) {
            return actual.now(unit);
        }
    }

    static boolean predicate(@NonNull Predicate<Thread> predicate) {
        boolean inCurrent = false;
        try {
            inCurrent = predicate.test(Thread.currentThread());
        } catch (Exception e) {
            RxJavaPlugins.onError(e);
        }
        return inCurrent;
    }
}
