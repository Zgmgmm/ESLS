package dev.zgmgmm.esls

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import dev.zgmgmm.esls.bean.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bind.*
import org.jetbrains.anko.*
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


class BindActivity : BaseActivity() {
    lateinit var scanBroadcastReceiver: ScanBroadcastReceiver
    lateinit var autoQueryGood: TextWatcher
    lateinit var autoQueryLabel: TextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind)

        bind.setOnClickListener {
            val prompt = "确定绑定商品和标签？"
            val goodBarCode = good_code.text.toString()
            val labelBarCode = label_code.text.toString()
            alert {
                title = prompt
                okButton {
                    requestBind(goodBarCode, labelBarCode)
                }
                noButton {}
            }.show()
        }

        // 输入后自动查询商品
        autoQueryGood = EditingCompletedWatcher { q ->
            info("query label $q")
            ESLS.instance.service.searchGood("=", 0, 1, RequestBean(listOf(QueryItem("", "", "barCode", q))))
                .timeout(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.from(mainLooper))
                .subscribe({
                    if (it.data.isEmpty()) {
                        good_info_error.text = "商品不存在"
                        good_info_error.visibility = View.VISIBLE
                        goodInfo.visibility = View.INVISIBLE
                    } else {
                        info("query good $q success: $it")
                        renderGood(it.data[0])
                    }
                }, {
                    info("query good $q fail: $it")
                    val msg = when (it) {
                        is SocketTimeoutException -> "网络超时";
                        else -> "查询失败"
                    }
                    good_info_error.text = msg
                    good_info_error.visibility = View.VISIBLE
                    goodInfo.visibility = View.INVISIBLE
                })
        }

        // 输入后自动查询标签
        autoQueryLabel = EditingCompletedWatcher { q ->
            info("query label $q")
            ESLS.instance.service.searchTag("=", 0, 1, RequestBean(listOf(QueryItem("", "", "barCode", q))))
                .timeout(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.from(mainLooper))
                .subscribe({
                    if (it.data.isEmpty()) {
                        label_info_error.text = "标签不存在"
                        label_info_error.visibility = View.VISIBLE
                        labelInfo.visibility = View.INVISIBLE
                    } else {
                        info("query good $q success: $it")
                        renderLabel(it.data[0])
                    }
                }, {
                    info("query label $q fail: $it")
                    val msg = when (it) {
                        is SocketTimeoutException -> "网络超时";
                        else -> "查询失败"
                    }
                    label_info_error.text = msg
                    label_info_error.visibility = View.VISIBLE
                    labelInfo.visibility = View.INVISIBLE
                })
        }

        // 开启自动查询商品/标签信息
        setAutoQuery(true)

        // 监听扫码广播
        scanBroadcastReceiver = ScanBroadcastReceiver.register(this) {
            if (good_code.isFocused) {
                good_code.setText(it)
                label_code.requestFocus()
            } else if (label_code.isFocused) {
                label_code.setText(it)
                bind.requestFocus()
            } else if (good_code.text.isNullOrBlank()) {
                good_code.setText(it)
                label_code.requestFocus()
            } else if (label_code.text.isNullOrBlank()) {
                label_code.setText(it)
                bind.requestFocus()
            }
        }
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
    fun requestRebind(goodBarcode: String, tag: Label) {
        val tagId = tag.id
        val oldGoodID = tag.goodid.toString()
        var bindingRequest: Disposable? = null
        val progressDialog = indeterminateProgressDialog("正在请求绑定") {
            setCanceledOnTouchOutside(false)
            setOnDismissListener {
                if (bindingRequest?.isDisposed != false)
                    return@setOnDismissListener
                bindingRequest?.dispose()
            }
        }
        bindingRequest =
                ESLS.instance.service.bind(
                    "id",
                    oldGoodID,
                    "id",
                    tagId,
                    0
                )
                    .timeout(5000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.from(mainLooper))
                    .doOnSubscribe {
                        progressDialog.show()
                    }
                    .doAfterTerminate{
                        progressDialog.dismiss()
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
                    .subscribe({
                        if(it.code==0){
                            toast("绑定失败: ${it.msg}")
                        }else {
                            toast("绑定成功")
                        }
                    }, {
                        val e = it as HttpException
                        when(e.code()){
                            400->{
                                val resp =
                                    Gson().fromJson<Response<String>>(e.response().errorBody()!!.string(), Response::class.java)
                                toast(resp.msg)
                            }
                            else->{}
                        }
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
    fun requestBind(goodBarcode: String, labelBarcode: String) {
        var bindingRequest: Disposable? = null
        val progressDialog = indeterminateProgressDialog("正在请求绑定") {
            setCanceledOnTouchOutside(false)
            setOnDismissListener {
                if (bindingRequest?.isDisposed != false)
                    return@setOnDismissListener
                bindingRequest?.dispose()
            }
        }

        bindingRequest = ESLS.instance.service.searchTag(
            "=",
            0,
            1,
            RequestBean(listOf(QueryItem("barCode", labelBarcode)))
        )
            .timeout(5000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe {
                progressDialog.show()
            }
            .doAfterTerminate {
                progressDialog.dismiss()
            }
            .flatMap<Response<String>> {
                if (it.data.isEmpty()) {//tag does not exist
                    toast("标签不存在")
                    return@flatMap Observable.empty()
                }
                val tag = it.data[0]
                if (tag.state) {
                    alert("标签已被绑定，是否重新绑定") {
                        positiveButton("是") {
                            requestRebind(goodBarcode, tag)
                        }
                        negativeButton("否") {}
                    }.show()
                    return@flatMap Observable.empty()
                }
                return@flatMap ESLS.instance.service.bind(
                    "barCode",
                    goodBarcode,
                    "barCode",
                    labelBarcode,
                    1
                )
            }
            .subscribe({
                when(it.code){
                    1->{
                        toast("绑定成功")
                    };
                    else->{
                        toast("绑定失败: it.msg")
                    }
                }
            }, {
                val e = it as HttpException
                when (it.code()) {
                    400 -> {
                        val resp =
                            Gson().fromJson<Response<String>>(e.response().errorBody()!!.string(), Response::class.java)
                        toast(resp.data)
                    };
                    else -> {
                        toast(e.response().errorBody().toString())
                    }
                }
            })


    }


    fun renderLabel(data: Label) {
        labelInfo.run {
            find<TextView>(R.id.type).text = data.type
            find<TextView>(R.id.size).text = "${data.width} X ${data.height}"
            find<TextView>(R.id.state).text = if (data.state) "已绑定" else "未绑定"
            find<TextView>(R.id.power).text = "电量: ${data.power}%"
            visibility = View.VISIBLE
        }
    }

    fun renderGood(data: Good) {
        goodInfo.run {
            find<TextView>(R.id.name).text = data.name
            find<TextView>(R.id.provider).text = data.provider
            find<TextView>(R.id.unit).text = data.unit
            find<TextView>(R.id.price).text = data.price.toString()
            visibility = View.VISIBLE
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(scanBroadcastReceiver)
    }


    class EditingCompletedWatcher(var delay: Long = 1000, val callback: (String) -> Unit) : TextWatcher {
        var lastChanged = System.currentTimeMillis()
        var query = doAsync { }
        override fun afterTextChanged(s: Editable?) {
            val current = System.currentTimeMillis()
            val q = s.toString()
            query.cancel(true)
            if (!q.isBlank())
                query = doAsync {
                    // TODO
                    try {
                        Thread.sleep(delay)
                    } catch (e: Exception) {
                        return@doAsync
                    }
                    callback(q)
                }

            lastChanged = current
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
}
