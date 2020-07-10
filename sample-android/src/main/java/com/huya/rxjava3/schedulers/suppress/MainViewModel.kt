package com.huya.rxjava3.schedulers.suppress

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author YvesCheung
 * 2020/7/9
 */
class MainViewModel : ViewModel() {

    private val itemId = AtomicInteger(0)

    fun fetchItem(): Single<Item> {
        return download(itemId.getAndIncrement())
            .flatMap(::unZip)
            .flatMap(::checkMd5)
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun download(id: Int): Single<Item> {
        return Single.just(id)
            .delay(300, TimeUnit.MILLISECONDS, Schedulers.io())
            .map { Item(it) }
            .doOnSuccess { recordCurrentThread(it, "download") }
    }

    private fun unZip(item: Item): Single<Item> {
        return Single.just(item)
            .delay(300, TimeUnit.MILLISECONDS, Schedulers.io())
            .doOnSuccess { recordCurrentThread(it, "unZip") }
    }

    private fun checkMd5(item: Item): Single<Item> {
        return Single.just(item)
            .delay(300, TimeUnit.MILLISECONDS, Schedulers.computation())
            .doOnSuccess { recordCurrentThread(it, "checkMd5") }
    }

    private fun recordCurrentThread(item: Item, actionName: String) {
        item.threadRecord.add("item${item.id}: $actionName on ${Thread.currentThread().name}")
    }

    data class Item(val id: Int = 0, val threadRecord: MutableList<String> = mutableListOf())
}