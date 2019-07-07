package dev.zgmgmm.esls.activity

import RequestExceptionHandler
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import dev.zgmgmm.esls.*
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.exception.RequestException
import dev.zgmgmm.esls.model.Good
import dev.zgmgmm.esls.model.RequestBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_good_info.*
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
            showFloatInputDialog("输入新的价格", this::save)
        }

        disableEdit()
    }

    private fun disableEdit() {
        val inputs = listOf(
            name,
            provider,
            price,
            unit,
            barcode,
            promotionReason,
            promotePrice,
            labels,
            weightSpec,
            isComputeOpen,
            replenishNumber,
            computeNumber,
            shopName,
            shopNumber
        )
        inputs.forEach {
            it.inputType = InputType.TYPE_NULL
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
        weightSpec.setText(good.weightSpec)
        isComputeOpen.setText(good.isComputeOpen.toString())
        replenishNumber.setText(good.replenishNumber)
        computeNumber.setText(good.computeNumber)
        shopName.setText(good.shopName)
        shopNumber.setText(good.shopNumber)

        if (good.needReplenish) {
            computeNumber.setTextColor(Color.RED)
        }
    }

    @SuppressLint("CheckResult")
    fun save(newPrice: Float) {
        val modified = good.copy(price = newPrice.toString())
        val tipDialog = createLoadingTipDialog("正在改价")
//        val loadingTipDialog = createLoadingTipDialog("改价成功，正在刷新标签")

        ESLS.instance.service.good(modified)
            .subscribeOn(Schedulers.io())
            .doOnNext {
                if (!it.isSuccess())
                    throw RequestException("改价失败 ${it.data}")
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                render(it.data)
                tipDialog.dismiss()
                showSuccessTipDialog("改价成功")

//                loadingTipDialog.show()
            }
            .observeOn(Schedulers.io())
            .flatMap { return@flatMap ESLS.instance.service.goodUpdate(RequestBean("id", good.id.toString())) }
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
                //                val stat = it.data
//                showSuccessTipDialog(
//                    "改价成功，刷新标签成功${stat.successNumber}个，失败${stat.sum - stat.successNumber}个",
//                    duration = 30 * 1000
//                )
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }


}