# RxSchedulerSuppress

**RxSchedulerSuppress** 是用于抑制 `RxJava` 在同线程池内调度的工具。

对于一次可观察序列中的多次 `subscribeOn` 或者 `observeOn` 操作，哪怕在相同的 `Schedulers.io` 调度器上，观察者操作也会在不同的线程上执行，即发生从io线程到io线程的调度。

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

上面模拟的操作中，`download` `unZip` `checkMd5` 都各自指定了调度器，导致 `fetchItem` 实际上发生了三次线程切换。
对于这种一系列的耗时操作来说，完全可以运行在同一条后台线程上。

因此就有了 **RxSchedulerSuppress** 的想法：通过 `RxJavaPlugins` 装饰 `Schedulers.IO` 调度器—— **若当前操作已经运行在io线程上，那么就不再执行切换到io线程的重复调度。**

<table border="1">
    <tr>
        <th style="text-align:center">Code</th>
        <th style="text-align:center">Output</th>
    </tr>
    <tr>
        <td rowspan="4">
        <div class="highlight highlight-source-kotlin"><pre><span class="pl-en">Observable</span>
    .create<span class="pl-k">&lt;</span><span class="pl-c1">String</span><span class="pl-k">&gt;</span> { emitter <span class="pl-k">-</span><span class="pl-k">&gt;</span>
        <span class="pl-c1">println</span>(<span class="pl-s"><span class="pl-pds">"</span>create on <span class="pl-pds">"</span></span> <span class="pl-k">+</span> 
            <span class="pl-en">Thread</span>.currentThread().name)
        emitter.onNext(<span class="pl-s"><span class="pl-pds">"</span>Test<span class="pl-pds">"</span></span>)
        emitter.onComplete()
    }
    .subscribeOn(<span class="pl-en">Schedulers</span>.io())
    .observeOn(<span class="pl-en">Schedulers</span>.io())
    .map { result <span class="pl-k">-</span><span class="pl-k">&gt;</span>
        <span class="pl-c1">println</span>(<span class="pl-s"><span class="pl-pds">"</span>map on <span class="pl-pds">"</span></span> <span class="pl-k">+</span> 
            <span class="pl-en">Thread</span>.currentThread().name)
        result
    }
    .observeOn(<span class="pl-en">Schedulers</span>.io())
    .flatMapCompletable {
        <span class="pl-c1">println</span>(<span class="pl-s"><span class="pl-pds">"</span>flatMap on <span class="pl-pds">"</span></span> <span class="pl-k">+</span> 
            <span class="pl-en">Thread</span>.currentThread().name)
        <span class="pl-en">Completable</span>.complete()
    }
    .subscribe()</pre></div>
        </td>
        <th>Before</th>
    </tr>
    <tr>
        </td>
         <td>
            <ul>
            <li>create on RxCachedThreadScheduler-1</li>
            <li>map on RxCachedThreadScheduler-2</li>
            <li>flatMap on RxCachedThreadScheduler-3</li>
           </ul>
        </td>
    </tr>
    <tr>
        <th>After</th>
    </tr>
    <tr>
        <td>
            <ul>
            <li>create on RxCachedThreadScheduler-1</li>
            <li>map on RxCachedThreadScheduler-1</li>
            <li>flatMap on RxCachedThreadScheduler-1</li>
           </ul>
        </td>
    </tr>
</table>
