package dev.zgmgmm.esls.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.TipDialogUtil
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.bean.*
import dev.zgmgmm.esls.receiver.ZKCScanCodeBroadcastReceiver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bind.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


class BindActivity : BaseActivity() {
    private lateinit var scanBroadcastReceiver: ZKCScanCodeBroadcastReceiver
    private lateinit var autoQueryGood: TextWatcher
    private lateinit var autoQueryLabel: TextWatcher
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind)

        // action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        bind.setOnClickListener {
            val goodBarCode = good_code.text.toString()
            val labelBarCode = label_code.text.toString()
            if (goodBarCode.isBlank() || labelBarCode.isBlank()) {
                TipDialogUtil.showFailTipDialog(this, "请输出商品条码和标签条码")
                return@setOnClickListener
            }
            val prompt = "确定绑定商品和标签？"
            QMUIDialog.MessageDialogBuilder(this)
                .setTitle(prompt)
                .addAction("确定") { dialog: QMUIDialog, _ ->
                    requestBind(goodBarCode, labelBarCode)
                    dialog.dismiss()
                }
                .addAction("取消") { dialog: QMUIDialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // 监听扫码广播
        scanBroadcastReceiver = ZKCScanCodeBroadcastReceiver.register(this) {
            when {
                // 若正在输入商品条码
                good_code.isFocused -> {
                    good_code.setText(it)
                    label_code.requestFocus()
                }
                // 若正在输入标签条码
                label_code.isFocused -> {
                    label_code.setText(it)
                    bind.requestFocus()
                }
                // 若未输入商品条码
                good_code.text.isNullOrBlank() -> {
                    good_code.setText(it)
                    label_code.requestFocus()
                }
                // 若未输入标签条码
                label_code.text.isNullOrBlank() -> {
                    label_code.setText(it)
                    bind.requestFocus()
                }
            }
        }

        // 输入后自动查询商品
        autoQueryGood = EditingCompletedWatcher { q ->
            info("query label $q")
            ESLS.instance.service.searchGood(
                "=",
                0,
                1,
                RequestBean(listOf(QueryItem("barCode", q)))
            )
                .timeout(15, TimeUnit.SECONDS)
                .doOnSubscribe {
                    showGoodInfoState("正在查询商品")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.from(mainLooper))
                .subscribe({
                    if (it.data.isEmpty()) {
                        showGoodInfoState("商品不存在")
                    } else {
                        showGoodInfo(it.data[0])
                    }
                }, {
                    val msg = it.toString()
                    showGoodInfoState(msg)
                })
        }

        // 输入后自动查询标签
        autoQueryLabel = EditingCompletedWatcher { q ->
            info("query label $q")
            ESLS.instance.service.searchTag(
                "=",
                0,
                1,
                RequestBean(QueryItem("barCode", q))
            )
                .timeout(15, TimeUnit.SECONDS)
                .doOnSubscribe {
                    showGoodInfoState("正在查询标签")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.from(mainLooper))
                .subscribe({
                    if (it.data.isEmpty()) {
                        showLabelInfoState("标签不存在")
                    } else {
                        showLabelInfo(it.data[0])
                    }
                }, {
                    val msg = it.toString()
                    showLabelInfoState(msg)
                })
        }

        // 开启自动查询商品/标签信息
        setAutoQuery(true)
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(scanBroadcastReceiver)
    }

    private fun showGoodInfoState(msg: String) {
        good_info_state.text = msg
        good_info_state.visibility = View.VISIBLE
        goodInfo.visibility = View.INVISIBLE
    }

    private fun showGoodInfo(good: Good) {
        renderGood(good)
        good_info_state.visibility = View.INVISIBLE
        goodInfo.visibility = View.VISIBLE
    }

    private fun showLabelInfoState(msg: String) {
        label_info_state.text = msg
        label_info_state.visibility = View.VISIBLE
        labelInfo.visibility = View.INVISIBLE
    }

    private fun showLabelInfo(label: Label) {
        renderLabel(label)
        label_info_state.visibility = View.INVISIBLE
        labelInfo.visibility = View.VISIBLE
    }

    // 设置是否开启自动查询商品/标签信息
    private fun setAutoQuery(autoQuery: Boolean) {
        if (!autoQuery) {
            good_code.removeTextChangedListener(autoQueryGood)
            label_code.removeTextChangedListener(autoQueryLabel)
        } else {
            good_code.addTextChangedListener(autoQueryGood)
            label_code.addTextChangedListener(autoQueryLabel)
        }
    }


    /**
     * 重新绑定已绑定标签
     * 1. 解除绑定旧商品
     * 2. 绑定新商品
     */
    @SuppressLint("CheckResult")
    private fun requestRebind(goodBarcode: String, oldGoodID: String, tagId: String) {
        val tipDialog = QMUITipDialog.Builder(this)
            .setTipWord("正在请求绑定")
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .create()
        ESLS.instance.service.bind(
            "id",
            oldGoodID,
            "id",
            tagId,
            0
        )
            .timeout(15, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                tipDialog.setOnCancelListener { disposable.dispose() }
                tipDialog.show()
            }
            .flatMap<Response<String>> {
                return@flatMap ESLS.instance.service.bind(
                    "barCode",
                    goodBarcode,
                    "id",
                    tagId,
                    1
                )
            }
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doFinally {
                tipDialog.dismiss()
            }
            .subscribe({
                if (it.isSuccess()) {
                    TipDialogUtil.showFailTipDialog(this, "绑定失败: ${it.msg}")
                } else {
                    TipDialogUtil.showSuccessTipDialog(this, "绑定成功")
                }
            }, {
                TipDialogUtil.showFailTipDialog(this, "绑定失败:$it")
            })
    }

    /**
     * 请求绑定
     * +查询标签信息获取绑定状态
     *      +已绑定    询问是否重新绑定
     *          -是    先解绑；再绑定
     *          -否    取消
     *      -未绑定    绑定
     * @param overwrite 是否覆盖已有绑定关系
     */
    @SuppressLint("CheckResult")
    private fun requestBind(goodBarcode: String, labelBarcode: String) {
        val tipDialog = QMUITipDialog.Builder(this)
            .setTipWord("正在请求绑定")
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .create()
        ESLS.instance.service.searchTag(
            "=",
            0,
            1,
            RequestBean(listOf(QueryItem("barCode", labelBarcode)))
        )
            .timeout(15, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                tipDialog.setOnCancelListener { disposable.dispose() }
                tipDialog.show()
            }
            .flatMap<Response<String>> {
                if (it.data.isEmpty()) {
                    runOnUiThread { toast("标签不存在") }
                    return@flatMap Observable.empty()
                }
                val tag = it.data[0]
                if (tag.isBound()) {
                    QMUIDialog.MessageDialogBuilder(this)
                        .setTitle("标签已被绑定，确定重新绑定")
                        .addAction("确定") { dialog: QMUIDialog, i: Int ->
                            requestRebind(goodBarcode, tag.goodId.toString(), tag.id!!)
                            dialog.dismiss()
                        }
                        .addAction("取消") { dialog: QMUIDialog, i: Int ->
                            dialog.dismiss()
                        }
                    return@flatMap Observable.empty()
                } else {
                    return@flatMap ESLS.instance.service.bind(
                        "barCode",
                        goodBarcode,
                        "barCode",
                        labelBarcode,
                        1
                    )
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doFinally {
                tipDialog.dismiss()
            }
            .subscribe({
                if (it.isSuccess()) {
                    TipDialogUtil.showSuccessTipDialog(this, "绑定成功")
                } else {
                    TipDialogUtil.showFailTipDialog(this, "绑定失败: ${it.msg}")
                }
            }, {
                TipDialogUtil.showFailTipDialog(this, it.toString())
            })
    }


    private fun renderLabel(label: Label) {
        labelInfo.run {
            find<TextView>(R.id.type).text = label.screenType
            find<TextView>(R.id.size).text = "${label.resolutionWidth} X ${label.resolutionHeight}"
            find<TextView>(R.id.state).text = label.state
            find<TextView>(R.id.power).text = "电量: ${label.power}%"
            visibility = View.VISIBLE
        }
    }

    private fun renderGood(data: Good) {
        goodInfo.run {
            find<TextView>(R.id.name).text = data.name
            find<TextView>(R.id.provider).text = data.provider
            find<TextView>(R.id.unit).text = data.unit
            find<TextView>(R.id.price).text = data.price.toString()
            visibility = View.VISIBLE
        }
    }


    class EditingCompletedWatcher(var delay: Long = 1000, val callback: (String) -> Unit) : TextWatcher {
        private var query: Future<Unit>? = null
        override fun afterTextChanged(s: Editable?) {
            System.currentTimeMillis()
            val q = s.toString()
            query?.cancel(true)
            if (!q.isBlank())
                query = doAsync {
                    try {
                        Thread.sleep(delay)
                    } catch (e: Exception) {
                        return@doAsync
                    }
                    callback(q)
                }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
}
