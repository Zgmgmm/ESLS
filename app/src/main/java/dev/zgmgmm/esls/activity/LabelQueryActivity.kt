package dev.zgmgmm.esls.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.SearchView
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.TipDialogUtil
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.bean.QueryItem
import dev.zgmgmm.esls.bean.RequestBean
import dev.zgmgmm.esls.receiver.ZKCScanCodeBroadcastReceiver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_label_manage.*
import kotlinx.android.synthetic.main.activity_login.view.*

class LabelQueryActivity : BaseActivity() {
    private lateinit var scanBroadcastReceiver: ZKCScanCodeBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_manage)

        // action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        search.isSubmitButtonEnabled = true
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                query(query)
                return true
            }

            override fun onQueryTextChange(newText: String) = false
        })

        scanBroadcastReceiver = ZKCScanCodeBroadcastReceiver.register(this) {
            search.user_input.setText(it)
            query(it)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(scanBroadcastReceiver)
        super.onDestroy()
    }

    // 查询标签
    @SuppressLint("CheckResult")
    private fun query(barCode: String) {
        val tipDialog = TipDialogUtil.createLoadingTipDialog(this, "正在查询标签")
        ESLS.instance.service.searchTag("=", 0, 1, RequestBean(listOf(QueryItem("barCode", barCode))))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe { disposable ->
                tipDialog.setOnCancelListener {
                    disposable.dispose()
                }
                tipDialog.show()
            }
            .doAfterTerminate {
                tipDialog.dismiss()
            }
            .subscribe({
                if (it.data.isEmpty()) {
                    TipDialogUtil.showFailTipDialog(this, "标签不存在")
                } else {
                    LabelInfoActivity.start(this, it.data[0])
                }
            }, {
                TipDialogUtil.showFailTipDialog(this, "查询失败: $it")
            })
    }
}

