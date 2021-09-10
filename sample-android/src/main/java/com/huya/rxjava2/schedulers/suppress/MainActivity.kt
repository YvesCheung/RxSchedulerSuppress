package com.huya.rxjava2.schedulers.suppress

import android.arch.convert.bindLifecycle
import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.huya.rxjava2.schedulers.suppress.android.sample.R
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mainModel: MainViewModel by viewModels()

    private val suppressModel: SuppressSettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = MainAdapter()

        group_suppress.setOnCheckedChangeListener { _, checkedId ->
            suppressModel.checkedId = checkedId
            adapter.clear()
        }
        group_suppress.check(suppressModel.checkedId)

        rv_fetch_item.adapter = adapter

        btn_fetch_item.setOnClickListener {
            mainModel.fetchItem()
                .observeOn(AndroidSchedulers.mainThread())
                .bindLifecycle(this)
                .subscribe { item ->
                    adapter.addRecord(item.threadRecord)
                    rv_fetch_item.smoothScrollToPosition(adapter.itemCount)
                }
        }
    }

    class MainAdapter : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

        class MainViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        private val records = mutableListOf<String>()

        fun addRecord(record: List<String>) {
            records.addAll(record)
            notifyDataSetChanged()
        }

        fun clear() {
            records.clear()
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MainViewHolder(TextView(parent.context).apply {
                setPadding(16.dp, 4.dp, 16.dp, 4.dp)
            })

        override fun getItemCount(): Int = records.size

        override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
            holder.textView.text = records[position]
        }

        private val Int.dp: Int
            get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    }
}

