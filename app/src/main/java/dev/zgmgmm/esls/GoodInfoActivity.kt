package dev.zgmgmm.esls

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.view.View
import dev.zgmgmm.esls.bean.Good
import dev.zgmgmm.esls.bean.QueryItem
import dev.zgmgmm.esls.bean.RequestBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_good_info.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import retrofit2.HttpException

class GoodInfoActivity : BaseActivity() {
    lateinit var inputs: List<TextInputEditText>
    val good = intent.getSerializableExtra("good") as Good

    companion object {
        fun start(context: Context, good: Good) {
            val intent = Intent(context, GoodInfoActivity::class.java)
            intent.putExtra("good", good)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_good_info)
        inputs = listOf(
            name,
            provider,
            price,
            unit,
            barcode,
            promotion,
            promotionReason,
            promotePrice
        )

        name.setText(good.name)
        provider.setText(good.provider)
        price.setText(good.price.toString())
        unit.setText(good.unit)
        barcode.setText(good.barcode)
        promotion.setText(good.promotion)
        promotionReason.setText(good.promotionReason)
        promotePrice.setText(good.promotePrice.toString())

        toggleEdit()

        save.visibility = View.GONE
        cancel.visibility = View.GONE

        light.setOnClickListener {
            toggleLight()
        }
        edit.setOnClickListener {
            //            TODO("save current info")
            save.visibility = View.VISIBLE
            cancel.visibility = View.VISIBLE
            light.visibility = View.GONE
            edit.visibility = View.GONE
            toggleEdit()
            toast("editing")
        }
        save.setOnClickListener {
            //            TODO("update info")
            save.visibility = View.GONE
            cancel.visibility = View.GONE
            light.visibility = View.VISIBLE
            edit.visibility = View.VISIBLE
            toggleEdit()
            toast("request update")
        }
        cancel.setOnClickListener {
            //            TODO("restore info")
            save.visibility = View.GONE
            cancel.visibility = View.GONE
            light.visibility = View.VISIBLE
            edit.visibility = View.VISIBLE
            toggleEdit()
            toast("cancel editing")
        }
    }

    // 开启编辑
    fun toggleEdit() {
        inputs.forEach {
            it.isEnabled = !it.isEnabled
        }
    }

    var lightStatus = false
    // 开启/关闭闪烁
    fun toggleLight() {
        val mode = if (lightStatus) 0 else 1
        val msg = if (lightStatus) "正在关闭闪烁" else "正在开启闪烁"
        var disposable: Disposable? = null
        val progressDialog = indeterminateProgressDialog(msg) {
            setOnDismissListener {
                disposable?.dispose()
            }
        }
        disposable = ESLS.instance.service.light(mode, RequestBean(listOf(QueryItem("goodid", good.id.toString()))))
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
                lightStatus = !lightStatus
                toast("请求${sum}个，成功${success}个")
            }, {
                val e = it as HttpException
                toast(e.response().errorBody().toString())
            })
    }

}