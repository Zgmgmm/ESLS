package dev.zgmgmm.esls.activity

import RequestExceptionHandler
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import dev.zgmgmm.esls.*
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.bean.Good
import dev.zgmgmm.esls.bean.RequestBean
import dev.zgmgmm.esls.exception.RequestException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_good_info.*
import org.jetbrains.anko.info
import java.util.concurrent.TimeUnit


class GoodInfoActivity : BaseActivity() {
    lateinit var good: Good

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
        // action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val inputs = listOf(
            name,
            provider,
            price,
            unit,
            barcode,
            promotionReason,
            promotePrice,
            labels
        )
        inputs.forEach {
            it.inputType = InputType.TYPE_NULL
        }

        good = intent.getSerializableExtra("good") as Good
        render(good)
        // 寻找
        find.setOnClickListener {
            if (good.tagIdList.isEmpty()) {
                showTipDialog("该商品未绑定任何标签", QMUITipDialog.Builder.ICON_TYPE_FAIL)
            } else {
                LabelListActivity.start(this, good.id)
            }
        }

        // 改价
        edit.setOnClickListener {
            showInputDialog()
        }
    }

    private fun render(good: Good) {
        name.setText(good.name)
        provider.setText(good.provider)
        price.setText(good.price.toString())
        unit.setText(good.unit)
        barcode.setText(good.barCode)
        promotionReason.setText(good.promotionReason)
        promotePrice.setText(good.promotePrice.toString())
        labels.setText(good.tagIdList.size.toString())
    }

    @SuppressLint("CheckResult")
    fun save(newPrice: Double) {
        val modified = good.copy(price = newPrice)
        val tipDialog = createLoadingTipDialog("正在改价")
//        val loadingTipDialog = createLoadingTipDialog("改价成功，正在刷新标签")

        ESLS.instance.service.good(modified)
            .subscribeOn(Schedulers.io())
            .doOnNext {
                if (!it.isSuccess())
                    throw RequestException("改价失败 ${it.msg}")
                info("改价成功 $it.data")
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                render(it.data)
                tipDialog.dismiss()
//                loadingTipDialog.show()
            }
            .observeOn(Schedulers.io())
            .flatMap { return@flatMap ESLS.instance.service.goodUpdate(RequestBean("id", good.id)) }
            .doOnSubscribe {
                tipDialog.show()
            }
            .delay(1, TimeUnit.SECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .doFinally {
                tipDialog.dismiss()
//                loadingTipDialog.dismiss()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val stat = it.data
//                showSuccessTipDialog(
//                    "改价成功，刷新标签成功${stat.successNumber}个，失败${stat.sum - stat.successNumber}个",
//                    duration = 30 * 1000
//                )
            }, {
//                RequestExceptionHandler.handle(this, it)
            })
    }


    private fun showInputDialog() {
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        val dialog = builder
            .setTitle("输入新的价格")
            .addAction("确定") { dialog, _ ->
                dialog.dismiss()
                val newPrice = builder.editText.text.toString().toDouble()
                save(newPrice)
            }
            .addAction("取消") { dialog, _ ->
                dialog.cancel()
            }
            .create()
        builder.editText.keyListener = DigitsKeyListener.getInstance("123456789.")
        dialog.show()
    }
}