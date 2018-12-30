package dev.zgmgmm.esls

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import dev.zgmgmm.esls.bean.Label
import dev.zgmgmm.esls.bean.QueryItem
import dev.zgmgmm.esls.bean.RequestBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_label_info.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import retrofit2.HttpException

class LabelInfoActivity : BaseActivity() {
    companion object {
        fun start(context: Context, label: Label) {
            val intent = Intent(context, LabelInfoActivity::class.java)
            intent.putExtra("label", label)
            context.startActivity(intent)
        }
    }

    lateinit var label: Label
    lateinit var inputs: List<TextInputEditText>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_info)
        label = intent.getSerializableExtra("label") as Label
        id.setText(label.id.toString())
        power.setText(label.power.toString())
        rssi.setText(label.rssi.toString())
        val bound = if (label.state) "已绑定" else "未绑定"
        state.setText(bound)

        inputs = listOf(
            id,
            power,
            rssi,
            state
        )

        update.setOnClickListener {
            update()
        }
        scan.setOnClickListener {
            scan();
        }
        bind.setOnClickListener {
            toast("绑定")
        }
        light.setOnClickListener {
            toggleLight()
        }

        toggleEdit()
        bottomLayout.requestLayout()
    }

    // 刷新标签
    fun update() {
        var disposable: Disposable? = null
        val progressDialog = indeterminateProgressDialog("正在刷新标签") {
            setOnDismissListener {
                disposable?.dispose()
            }
        }
        disposable = ESLS.instance.service.updateTag(0, RequestBean(listOf(QueryItem("id", label.id))))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe {
                progressDialog.show()
            }
            .doAfterTerminate {
                progressDialog.dismiss()
            }
            .subscribe({
                val sum = it.msg.toInt()
                val success = it.code
                toast("请求${sum}个，成功${success}个")
            }, {
                val e = it as HttpException
                toast(e.response().errorBody().toString())
            })
    }

    // 巡检
    fun scan() {
        var disposable: Disposable? = null
        val progressDialog = indeterminateProgressDialog("正在巡检") {
            setOnDismissListener {
                disposable?.dispose()
            }
        }
        disposable = ESLS.instance.service.scanTag(0, RequestBean(listOf(QueryItem("id", label.id))))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe {
                progressDialog.show()
            }
            .doAfterTerminate {
                progressDialog.dismiss()
            }
            .subscribe({
                val sum = it.msg.toInt()
                val success = it.code
                toast("请求${sum}个，成功${success}个")
            }, {
                val e = it as HttpException
                toast(e.response().errorBody().toString())
            })
    }

    var lightStatus=false
    // 开启/关闭闪烁
    fun toggleLight() {
        var disposable: Disposable? = null
        val mode=if(lightStatus)0 else 1
        val msg=if(lightStatus)"正在开启闪烁" else "正在关闭闪烁"
        val progressDialog = indeterminateProgressDialog(msg) {
            setOnDismissListener {
                disposable?.dispose()
            }
        }
        disposable = ESLS.instance.service.scanTag(mode, RequestBean(listOf(QueryItem("id", label.id))))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe {
                progressDialog.show()
            }
            .doAfterTerminate {
                progressDialog.dismiss()
            }
            .subscribe({
                val sum = it.msg.toInt()
                val success = it.code
                toast("请求${sum}个，成功${success}个")
            }, {
                val e = it as HttpException
                toast(e.response().errorBody().toString())
            })
    }

    // 编辑
    fun toggleEdit() {
        inputs.forEach {
            it.isEnabled = !it.isEnabled
        }
    }
}