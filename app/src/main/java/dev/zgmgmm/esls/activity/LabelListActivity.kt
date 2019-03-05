package dev.zgmgmm.esls.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.TipDialogUtil
import dev.zgmgmm.esls.adapter.LabelAdapter
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.bean.Label
import dev.zgmgmm.esls.bean.QueryItem
import dev.zgmgmm.esls.bean.RequestBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_label_list.*
import java.util.*
import java.util.concurrent.TimeUnit

class LabelListActivity : BaseActivity() {
    companion object {
        fun start(context: Context, goodId: String) {
            val intent = Intent(context, LabelListActivity::class.java)
            intent.putExtra("goodId", goodId)
            context.startActivity(intent)
        }
    }

    private lateinit var adapter: LabelAdapter
    private lateinit var goodId: String

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_list)
        // action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        goodId = intent.getStringExtra("goodId")
        listView.adapter = LabelAdapter(
            this@LabelListActivity,
            R.layout.list_item_label_find,
            mutableListOf()

        )
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
        val tipDialog = TipDialogUtil.createLoadingTipDialog(this, "正在获取标签列表")
        ESLS.instance.service.searchTag("=", 0, Integer.MAX_VALUE, RequestBean(listOf(QueryItem("goodId", goodId))))
            .timeout(5000, TimeUnit.MILLISECONDS)
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
                if (it.isSuccess()) {
                    TipDialogUtil.showFailTipDialog(this, "查询失败: $it")
                } else {
                    adapter.labels.addAll(it.data)
                    adapter.notifyDataSetChanged()
                }
            }, {
                TipDialogUtil.showFailTipDialog(this, "查询失败: $it")
            })
    }

    @SuppressLint("CheckResult")
    private fun toggleFlick(on: Boolean) {
        val action = if (on) "开启" else "关闭"
        val tipDialog = TipDialogUtil.createLoadingTipDialog(this, "正在${action}闪灯")


        val queryItems = adapter.checked.map { QueryItem("id", it.id) }
        val requestBean = RequestBean(queryItems)
        val mode = if (on) 1 else 0
        ESLS.instance.service.light(mode, 0, requestBean)
            .timeout(5000, TimeUnit.MILLISECONDS)
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
                TipDialogUtil.showFailTipDialog(this, "共计${sum}个，成功${successNumber}个")
            }, {
                TipDialogUtil.showFailTipDialog(this, "操作失败: $it")
            })
    }


}

