# RxSchedulerSuppress

**RxSchedulerSuppress** 是用于抑制 `RxJava` 在同一个线程池内重复调度的工具。
通过 `RxjavaPlugins` 替换调度器的实现：如果当前线程已经符合调度器指定的线程池，那么就不再进行线程切换，直接执行。

以Android的主线程调度器为例，对于代码：
```java 
Observable.just("Test")
    .observeOn(AndroidSchedulers.mainThread())
    .observeOn(AndroidSchedulers.mainThread())
    .observeOn(AndroidSchedulers.mainThread())
    .observeOn(AndroidSchedulers.mainThread())
    .observeOn(AndroidSchedulers.mainThread())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe((s) -> println(s));
```
完全等价于以下代码，订阅逻辑会在6个消息循环后再执行：
```java 
Handler mainThread = new Handler(Looper.getMainLooper());
mainThread.post(() ->
    mainThread.post(() ->
        mainThread.post(() ->
            mainThread.post(() ->
                mainThread.post(() ->
                    mainThread.post(() -> println("Test"))
                )
            )
        )
    )
);
```

通过 **RxSchedulerSuppress** 减少线程切换后，相当于以下代码，若当前为调度器的目标线程，会立即执行：
```java 
if(Looper.myLooper() == Looper.mainLooper()){
    println("Test");
} else {
    Handler mainThread = new Handler(Looper.getMainLooper());
    mainThread.post(() -> println("Test"));
}
```

## 使用
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
