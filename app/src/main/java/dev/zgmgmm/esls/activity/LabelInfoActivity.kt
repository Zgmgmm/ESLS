package dev.zgmgmm.esls.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.TipDialogUtil
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.bean.Label
import dev.zgmgmm.esls.bean.RequestBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_label_info.*
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class LabelInfoActivity : BaseActivity() {
    companion object {
        fun start(context: Context, label: Label) {
            val intent = Intent(context, LabelInfoActivity::class.java)
            intent.putExtra("label", label)
            context.startActivity(intent)
        }
    }

    enum class Action(val desc: String) {
        FLICK_ON("开启闪烁"),
        FLICK_OFF("关闭闪烁"),
        UPDATE("刷新"),
        SCAN("巡检"),
        REMOVE("移除"),
        ENABLE("启用"),
        DISABLE("禁用"),
    }

    private lateinit var label: Label
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_info)

        // action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        render(intent.getSerializableExtra("label") as Label)
        listOf(id, power, rssi, state).forEach { it.inputType = InputType.TYPE_NULL }

        update.setOnClickListener { request(Action.UPDATE) }
        scan.setOnClickListener { request(Action.SCAN) }
        lightOn.setOnClickListener { request(Action.FLICK_ON) }
        lightOff.setOnClickListener { request(Action.FLICK_OFF) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_label_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.unbind -> unbind()
            R.id.enable -> request(Action.ENABLE)
            R.id.disable -> request(Action.DISABLE)
            R.id.remove -> request(Action.REMOVE)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun render(label: Label) {
        this.label = label
        id.setText(label.id)
        power.setText(label.power.toString())
        rssi.setText(label.tagRssi.toString())
        val bound = label.state
        state.setText(bound)
    }

    private fun request(action: Action) {
        val actionName = action.desc
        val tipDialog = TipDialogUtil.createLoadingTipDialog(this, "正在$actionName")
        val observable =
            with(ESLS.instance.service) {
                when (action) {
                    Action.FLICK_ON -> light(1, 0, RequestBean("id", label.id))
                    Action.FLICK_OFF -> light(0, 0, RequestBean("id", label.id))
                    Action.UPDATE -> updateTag(0, RequestBean("id", label.id))
                    Action.SCAN -> scan(0, RequestBean("id", label.id))
                    Action.ENABLE -> status(1, RequestBean("id", label.id))
                    Action.DISABLE -> status(0, RequestBean("id", label.id))
                    Action.REMOVE -> remove(0, RequestBean("id", label.id))
                }
            }
        observable
            .timeout(30, TimeUnit.SECONDS)
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
                val stat = it.data
                val success = stat.successNumber
                if (success == 0) {
                    TipDialogUtil.showFailTipDialog(this, "${actionName}失败: $it")
                } else {
                    TipDialogUtil.showSuccessTipDialog(this, "${actionName}成功")
                }
            }, {
                TipDialogUtil.showFailTipDialog(this, "${actionName}失败: $it")
            })
    }

    private fun unbind() {
        val tipDialog = TipDialogUtil.createLoadingTipDialog(this, "正在解除绑定")
        ESLS.instance.service.bind("id", label.goodId!!, "id", label.id, 0)
            .timeout(30, TimeUnit.SECONDS)
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
                    TipDialogUtil.showFailTipDialog(this, "解除绑定失败: $it")
                } else {
                    TipDialogUtil.showSuccessTipDialog(this, "解除绑定成功")
                }
            }, {
                TipDialogUtil.showFailTipDialog(this, "解除绑定失败: $it")
            })
    }
}