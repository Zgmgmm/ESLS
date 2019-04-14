package dev.zgmgmm.esls.activity

import RequestExceptionHandler
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import dev.zgmgmm.esls.*
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.bean.Good
import dev.zgmgmm.esls.bean.Label
import dev.zgmgmm.esls.bean.QueryItem
import dev.zgmgmm.esls.bean.RequestBean
import dev.zgmgmm.esls.exception.RequestException
import dev.zgmgmm.esls.receiver.ZKCScanCodeBroadcastReceiver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bind.*
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import java.util.concurrent.TimeUnit


class BindActivity : BaseActivity() {
    private lateinit var zkcScanCodeBroadcastReceiver: ZKCScanCodeBroadcastReceiver
    private var autoQueryGood: Disposable? = null
    private var autoQueryLabel: Disposable? = null
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
                showFailTipDialog("请输出商品条码和标签条码")
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


        autoQueryLabel()
        autoQueryGood()
    }



    override fun onStart() {
        super.onStart()
        // 监听扫码广播
        zkcScanCodeBroadcastReceiver = ZKCScanCodeBroadcastReceiver.register(this) {
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
    }

    override fun onStop() {
        unregisterReceiver(zkcScanCodeBroadcastReceiver)
        super.onStop()
    }


    @SuppressLint("CheckResult")
    private fun autoQueryGood() {
        RxTextView.textChanges(good_code)
            .skip(1)
            .debounce(500, TimeUnit.MILLISECONDS)
            .filter(CharSequence::isNotBlank)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                showGoodInfoState("正在查询商品..")
            }
            .observeOn(Schedulers.io())
            .flatMap {
                ESLS.instance.service.searchGood(
                    "=",
                    0,
                    1,
                    RequestBean(listOf(QueryItem("barCode", it.toString())))
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe{
                if(autoQueryGood!=null)
                    compositeDisposable.remove(autoQueryGood!!)
                autoQueryGood=it
                compositeDisposable.add(it)
            }
            .subscribe({
                if (it.data.isEmpty()) {
                    showGoodInfoState("商品不存在")
                } else {
                    showGoodInfo(it.data[0])
                }
            }, {
                RequestExceptionHandler.handle(this, it)
                val msg = it.toString()
                showGoodInfoState(msg)
                autoQueryGood()
            })
    }


    @SuppressLint("CheckResult")
    private fun autoQueryLabel() {
        autoQueryLabel = RxTextView.textChanges(label_code)
            .skip(1)
            .debounce(500, TimeUnit.MILLISECONDS)
            .filter(CharSequence::isNotBlank)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                showLabelInfoState("正在查询标签..")
            }
            .observeOn(Schedulers.io())
            .flatMap {
                val code = it.toString()
                ESLS.instance.service.searchTag(
                    "=",
                    0,
                    1,
                    RequestBean(listOf(QueryItem("barCode", code)))
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe{
                if(autoQueryLabel!=null)
                    compositeDisposable.remove(autoQueryLabel!!)
                autoQueryLabel=it
                compositeDisposable.add(it)
            }
            .subscribe({
                if (it.data.isEmpty()) {
                    showLabelInfoState("标签不存在")
                } else {
                    showLabelInfo(it.data[0])
                }
            }, {
                RequestExceptionHandler.handle(this, it)
                val msg = it.toString()
                showLabelInfoState(msg)
                autoQueryLabel()
            })

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



    @SuppressLint("CheckResult")
    private fun requestBind(goodBarcode: String, labelId: String, mode: Int) {
        val tipDialog = createLoadingTipDialog("正在请求绑定")
        ESLS.instance.service.bind(
            "barCode",
            goodBarcode,
            "id",
            labelId,
            mode
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
                if (it.isSuccess()) {
                    showSuccessTipDialog("绑定成功")
                } else {
                    throw RequestException("绑定失败 ${it.data}")
                }
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }

    /**
     * 请求绑定
     * +查询标签信息获取绑定状态
     *      +已绑定    询问是否重新绑定
     *          -是    覆盖绑定
     *          -否    取消
     *      -未绑定    绑定
     */
    @SuppressLint("CheckResult")
    private fun requestBind(goodBarcode: String, labelBarcode: String) {
        val tipDialog = createLoadingTipDialog("正在请求绑定")
        ESLS.instance.service.searchTag(
            "=",
            0,
            1,
            RequestBean(listOf(QueryItem("barCode", labelBarcode)))
        )
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable ->
                tipDialog.setOnCancelListener { disposable.dispose() }
                tipDialog.show()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (it.data.isEmpty()) {
                    throw RequestException("标签不存在")
                }
                val tag = it.data[0]
                info("isBound? ${tag.isBound()}")
                if (tag.isBound()) {
                    QMUIDialog.MessageDialogBuilder(this)
                        .setTitle("标签已被绑定，确定重新绑定")
                        .addAction("确定") { dialog: QMUIDialog, _: Int ->
                            requestBind(goodBarcode, tag.id, 2)
                            dialog.dismiss()
                        }
                        .addAction("取消") { dialog: QMUIDialog, _: Int ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            .map { it.data.first() }
            .filter { !it.isBound() }
            .doFinally {
                tipDialog.dismiss()
            }
            .subscribe({
                requestBind(goodBarcode, it.id, 1)
            }, {
                RequestExceptionHandler.handle(this, it)
            })

    }


    private fun renderLabel(label: Label) {
        labelInfo.run {
            find<TextView>(R.id.type).text = "屏幕类型: ${label.screenType}"
            find<TextView>(R.id.size).text = "宽x高: ${label.resolutionWidth} X ${label.resolutionHeight}"
            find<TextView>(R.id.state).text ="状态: ${label.state}"
            find<TextView>(R.id.power).text = "电量: ${label.power}"
            visibility = View.VISIBLE
        }
    }

    private fun renderGood(data: Good) {
        goodInfo.run {
            find<TextView>(R.id.name).text = "商品名称: ${data.name}"
            find<TextView>(R.id.provider).text = "供应商: ${data.provider}"
            find<TextView>(R.id.unit).text = "单位: ${data.unit}"
            find<TextView>(R.id.price).text = "单价: ${data.price.toString()}"
            visibility = View.VISIBLE
        }
    }

}
