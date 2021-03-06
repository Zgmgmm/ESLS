package dev.zgmgmm.esls.activity

import RequestExceptionHandler
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.createLoadingTipDialog
import dev.zgmgmm.esls.exception.RequestException
import dev.zgmgmm.esls.model.QueryItem
import dev.zgmgmm.esls.model.RequestBean
import dev.zgmgmm.esls.receiver.ZKCScanCodeBroadcastReceiver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_label_manage.*
import org.jetbrains.anko.info

class LabelQueryActivity : BaseActivity() {
    private lateinit var zkcScanCodeBroadcastReceiver: ZKCScanCodeBroadcastReceiver
    private val SCAN_WITH_CAMERA: Int = 1
    private var target = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_manage)

        target = intent.extras?.getString("target", "") ?: ""


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
        camera.setOnClickListener {
            startActivityForResult(Intent(this, CameraScanActivity::class.java), SCAN_WITH_CAMERA)
        }
    }

    override fun onStart() {
        super.onStart()
        zkcScanCodeBroadcastReceiver = ZKCScanCodeBroadcastReceiver.register(this) {
            search.setQuery(it, true)
        }
    }

    override fun onStop() {
        unregisterReceiver(zkcScanCodeBroadcastReceiver)
        super.onStop()
    }

    // 查询标签
    @SuppressLint("CheckResult")
    private fun query(barCode: String) {
        val tipDialog = createLoadingTipDialog("正在查询标签")
        ESLS.instance.service.searchTag(
            "=",
            0,
            1,
            RequestBean(listOf(QueryItem("barCode", barCode)))
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe { disposable ->
                tipDialog.setOnCancelListener {
                    disposable.dispose()
                }
                tipDialog.show()
            }
            .doFinally {
                tipDialog.dismiss()
            }
            .subscribe({
                if (it.data.isEmpty()) {
                    throw RequestException("标签不存在")
                }
                if (target == "scale") {
                    LabelInfoActivity.start(this, it.data[0])
                } else {
                    LabelInfoActivity.start(this, it.data[0])
                }
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = data?.extras?.getString("result")
        info("scan with camera: $result")
        if (result == null)
            return
        search.setQuery(result, true)
    }
}

