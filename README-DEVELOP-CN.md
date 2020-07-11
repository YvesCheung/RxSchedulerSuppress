# 减少RxJava中多余的线程调度

## 为什么要抑制线程调度

对于一次可观察序列中的多次 `subscribeOn` 或者 `observeOn` 操作，哪怕指定在相同的 `Schedulers.io` 调度器上，观察者操作也会在不同的线程上执行，即发生从io线程到io线程的切换。

这种线程调度是否可避免的呢？

假如我们有以下代码：

```kotlin
fun fetchItem(): Single<Item> {
    return download(itemId.getAndIncrement())
        .flatMap(::unZip)
        .flatMap(::checkMd5)
}

private fun download(id: Int): Single<Item> {
    return Single.just(id)
        //Simulate a long time operation
        .delay(300, TimeUnit.MILLISECONDS, Schedulers.io())
        .map { Item(it) }
}

private fun unZip(item: Item): Single<Item> {
    return Single.just(item)
        //Simulate a long time operation
        .delay(300, TimeUnit.MILLISECONDS, Schedulers.io())
}

private fun checkMd5(item: Item): Single<Item> {
    return Single.just(item)
        //Simulate a long time operation
        .delay(300, TimeUnit.MILLISECONDS, Schedulers.io())
}
```

上面模拟的操作中，`download` `unZip` `checkMd5` 都各自指定了调度器，导致 `fetchItem` 实际上发生了**三次线程切换**。
对于这种一系列的耗时操作来说，完全可以运行在同一条后台线程上。

因此就有了 **RxSchedulerSuppress** 的想法：通过 `RxJavaPlugins` 替换 `Schedulers.IO` 调度器的实现—— **若当前操作已经运行在io线程上，那么就不再执行io线程池的重复调度。**

## 实现

1. 通过 `RxJavaPlugins` 替换 `Schedulers.io()` 和 `Schedulers.compute()`
    
    RxJava中有五种默认实现的调度器：
    - Schedulers.io()
    - Schedulers.compute()
    - Schedulers.single()
    - Schedulers.trampoline()
    - Schedulers.newThread()

    其中， `single` 指定在特殊的共享线程上，`newThread` 指定要创建新线程， `trampoline` 不会切换线程。所以我们只能够替换 `io` 和 `compute` 这两个调度器。
    
    ```java
    RxJavaPlugins.setIoSchedulerHandler((scheduler)->scheduler);
    RxJavaPlugins.setComputationSchedulerHandler((scheduler)->scheduler);
    
    //或者
    RxJavaPlugins.setInitIoSchedulerHandler(...);
    RxJavaPlugins.setInitComputationSchedulerHandler(...);
    //但init这两个方法一定要在首次使用Schedulers之前调用，
    //而且一旦使用就无法改变，无法在应用使用过程中做对比实验。
    ```

2. 实现抑制多余线程切换的调度器
    
    以IO调度器为例，我们完全无须自行实现和维护线程池，而是应该在原有的调度器实现上做一个切面拦截。通过代理模式，如果当前线程已经是io线程，那么就直接执行，否则就调回原有调度器的实现，进行线程切换。
    
    ```java
    public final class ImmediateScheduler extends Scheduler {
        //...
        private final Scheduler delegate; //原调度器实现，即Schedulers.io()

        @Override
        @NonNull
        public Disposable scheduleDirect(@NonNull Runnable run) {
            if (predicate(runInCurrentThread)) { 
                //如果已经在io线程，则直接在当前线程运行
                return TrampolineScheduler.instance().scheduleDirect(run);
            } else {
                //否则就用原来的调度器调度
                return delegate.scheduleDirect(run);
            }
        }

        @Override
        @NonNull
        public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            if (predicate(runInCurrentThread)) {
                //如果已经在io线程，则直接在当前线程运行
                return TrampolineScheduler.instance().scheduleDirect(run, delay, unit);
            } else {
                //否则就用原来的调度器调度
                return delegate.scheduleDirect(run, delay, unit);
            }
        }
        
        //...
    }
    ```

3. 如何判断当前线程是否已经是io/compute线程

    因为io/compute线程其实是人为定义的，取决于实际使用的场景，本质上只是线程池配置的不同，线程本身没有这样的特性。所以我暂时只能依赖 `Thread.name` 来判断当前线程所属的调度器：io调度器的线程以 "RxCachedThreadScheduler" 前缀命名而 compute调度器以 "RxComputationThreadPool" 前缀命名。
    
    **如果项目中已经替换过RxJava默认的线程池，就需要根据项目正在使用的ThreadFactory为线程规范命名，并修改这个当前线程的判断条件！**


## 效果

#### 抑制 `IO` 到 `IO` 的线程切换

```java
Observable
    .create(emitter -> {
        System.out.println("create on " + Thread.currentThread().getName());
        emitter.onNext("Test");
        emitter.onComplete();
    })
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.io())
    .map(s -> {
        System.out.println("map on " + Thread.currentThread().getName());
        return s;
    })
    .observeOn(Schedulers.io())
    .flatMapCompletable(s -> {
        System.out.println("flatMap on " + Thread.currentThread().getName());
        return Completable.complete();
    })
    .subscribe();
```

|<div style="text-align:center">Before Suppress</div>|<div style="text-align:center">After Suppress</div>|
|----|----|
|<ul><li>create on RxCachedThreadScheduler-1</li><li>map on RxCachedThreadScheduler-2</li><li>flatMap on RxCachedThreadScheduler-3</li></ul>|<ul><li>create on RxCachedThreadScheduler-1</li><li>map on RxCachedThreadScheduler-1</li><li>flatMap on RxCachedThreadScheduler-1</li></ul>|

#### 抑制 `Compute` 到 `IO` 的线程切换

```java 
Observable.timer(1, TimeUnit.MILLISECONDS)
    .map(s -> {
        System.out.println("timer on " + Thread.currentThread().getName());
        return s;
    })
    .observeOn(Schedulers.io())
    .subscribe(s ->
        System.out.println("subscribe on " + Thread.currentThread().getName())
    );
```

|<div style="text-align:center">Before Suppress</div>|<div style="text-align:center">After Suppress</div>|
|----|----|
|<ul><li>timer on RxComputationThreadPool-1</li><li>subscribe on RxCachedThreadScheduler-1</li></ul>|<ul><li>timer on RxComputationThreadPool-1</li><li>subscribe on RxComputationThreadPool-1</li></ul>|

#### 抑制 `AndroidSchedulers.mainThread` 线程切换

```java
AndroidSchedulers.mainThread().scheduleDirect(runnable);
```

- Before Suppress
```java
//等同于以下代码
new Handler(Looper.getMainLooper()).post(runnable);
```
    
- After Suppress
```java
//等同于以下代码
if (Looper.myLooper() == Looper.getMainLooper()) {
    runnable.run();
} else {
    new Handler(Looper.getMainLooper()).post(runnable);
}
```

## 使用

1. build.gradle添加依赖
```groovy
dependencies {
    //targeting io.reactivex.schedulers.Schedulers
    implementation 'com.github.YvesCheung.RxSchedulerSuppress:scheduler-suppress:1.0.0'

    //targeting io.reactivex.android.schedulers.AndroidSchedulers
    implementation 'com.github.YvesCheung.RxSchedulerSuppress:scheduler-suppress-android:1.0.0'
}
```

2. 在使用RxJava之前初始化
```java 
public class App extends Application {

    public void onCreate() {
        //...
        
        //抑制通过Schedulers.io()从io线程切换到io线程
        //如果当前线程已经是io线程，会立即在当前线程执行
        SchedulerSuppress.SuppressIo();
        //抑制通过Schedulers.compute()从compute线程切换到compute线程
        //如果当前线程已经是io线程，会立即在当前线程执行
        SchedulerSuppress.SuppressCompute();
        
        //or
        //抑制通过Schedulers.io()或者Schedulers.compute()
        //从io线程切换到compute线程，或者从compute线程切换到io线程
        SchedulerSuppress.SuppressBackground();

        //抑制通过AndroidSchedulers.mainThread()从main线程抛到下一次Looper循环
        //如果当前线程已经是main线程，会立即在当前线程执行
        AndroidSchedulerSuppress.SuppressMain();
        
        //以上代码需要在 `RxJavaPlugins.lockDown` 前执行
        RxJavaPlugins.lockDown();
    }
}

```

## 项目源码
欢迎交流或者提交您的修改
[https://github.com/YvesCheung/RxSchedulerSuppress](https://github.com/YvesCheung/RxSchedulerSuppress)