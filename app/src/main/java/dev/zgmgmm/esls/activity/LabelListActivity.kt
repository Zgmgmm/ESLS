package dev.zgmgmm.esls.activity

import RequestExceptionHandler
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.zgmgmm.esls.*
import dev.zgmgmm.esls.adapter.LabelAdapter
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.model.QueryItem
import dev.zgmgmm.esls.model.RequestBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_label_list.*

class LabelListActivity : BaseActivity() {
    companion object {
        fun start(context: Context, goodId: Int) {
            val intent = Intent(context, LabelListActivity::class.java)
            intent.putExtra("goodId", goodId)
            context.startActivity(intent)
        }
    }

    private lateinit var adapter: LabelAdapter
    private var goodId: Int = 0

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_list)
        // action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        goodId = intent.getIntExtra("goodId", 0)
        adapter = LabelAdapter(
            this@LabelListActivity,
            R.layout.list_item_label_find,
            mutableListOf()

        )
        listView.adapter = adapter
        on.setOnClickListener {
            toggleFlick(true)
        }
        off.setOnClickListener {
            toggleFlick(false)
        }

        load()
    }

    @SuppressLint("CheckResult")
    private fun load() {
        adapter.labels.clear()
        adapter.notifyDataSetChanged()
        val tipDialog = createLoadingTipDialog("正在获取标签列表")
        ESLS.instance.service.searchTag(
            "=",
            0,
            Integer.MAX_VALUE,
            RequestBean(listOf(QueryItem("goodid", goodId.toString())))
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe { disposable ->
                tipDialog.setOnCancelListener { disposable.dispose() }
                tipDialog.show()
            }
            .doFinally {
                tipDialog.dismiss()
            }
            .subscribe({
                adapter.labels.addAll(it.data)
                adapter.notifyDataSetChanged()
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }

    @SuppressLint("CheckResult")
    private fun toggleFlick(on: Boolean) {
        if (adapter.checked.isEmpty()) {
            showInfoTipDialog("请点击选取标签")
            return
        }
        val action = if (on) "开启" else "关闭"
        val tipDialog = createLoadingTipDialog("正在${action}闪灯")
        val queryItems = adapter.checked.map { QueryItem("id", it.id.toString()) }
        val requestBean = RequestBean(queryItems)
        val mode = if (on) 1 else 0
        ESLS.instance.service.light(mode, 0, requestBean)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe { disposable ->
                tipDialog.setOnCancelListener { disposable.dispose() }
                tipDialog.show()
            }
            .doFinally {
                tipDialog.dismiss()
            }
            .subscribe({
                val sum = it.data.sum
                val successNumber = it.data.successNumber
                showSuccessTipDialog("共计${sum}个，成功${successNumber}个")
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }


}

