package io.reactivex.android.schedulers;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;

public final class HandlerImmediateScheduler extends Scheduler {

    private final Handler handler;
    private final HandlerScheduler impl;

    public HandlerImmediateScheduler(@NonNull Handler handler, boolean async) {
        this.handler = Objects.requireNonNull(handler);
        this.impl = new HandlerScheduler(handler, async);
    }

    @Override
    public Disposable scheduleDirect(@NonNull Runnable run) {
        if (handler.getLooper() == Looper.myLooper()) {
            RxJavaPlugins.onSchedule(run).run();
            return Disposables.disposed();
        }
        return impl.scheduleDirect(run);
    }

    @Override
    public void start() {
        impl.start();
    }

    @Override
    public void shutdown() {
        impl.shutdown();
    }

    @Override
    public long now(@NonNull TimeUnit unit) {
        return impl.now(unit);
    }

    @Override
    public <S extends Scheduler & Disposable> S when(@NonNull Function<Flowable<Flowable<Completable>>, Completable> combine) {
        return impl.when(combine);
    }

    @Override
    @SuppressLint("NewApi") // Async will only be true when the API is available to call.
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
        return impl.scheduleDirect(run, delay, unit);
    }

    @Override
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable run, long initialDelay, long period, @NonNull TimeUnit unit) {
        return impl.schedulePeriodicallyDirect(run, initialDelay, period, unit);
    }

    @Override
    public Worker createWorker() {
        return new HandlerImmediateWorker(handler, impl.createWorker());
    }

    private static final class HandlerImmediateWorker extends Worker {
        private final Handler handler;
        private final Worker actual;

        HandlerImmediateWorker(Handler handler, Worker actual) {
            this.handler = handler;
            this.actual = actual;
        }

        @Override
        public Disposable schedule(@NonNull Runnable run) {
            if (!actual.isDisposed() && handler.getLooper() == Looper.myLooper()) {
                RxJavaPlugins.onSchedule(run).run();
                return Disposables.disposed();
            }
            return actual.schedule(run);
        }

        @Override
        @SuppressLint("NewApi") // Async will only be true when the API is available to call.
        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            return actual.schedule(run, delay, unit);
        }

        @Override
        public Disposable schedulePeriodically(@NonNull Runnable run, long initialDelay, long period, @NonNull TimeUnit unit) {
            return actual.schedulePeriodically(run, initialDelay, period, unit);
        }

        @Override
        public void dispose() {
            actual.dispose();
        }

        @Override
        public boolean isDisposed() {
            return actual.isDisposed();
        }

        @Override
        public long now(@NonNull TimeUnit unit) {
            return actual.now(unit);
        }
    }
}