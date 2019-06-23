package dev.zgmgmm.esls.activity

import RequestExceptionHandler
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import dev.zgmgmm.esls.*
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.bean.Good
import dev.zgmgmm.esls.bean.Label
import dev.zgmgmm.esls.bean.RequestBean
import dev.zgmgmm.esls.exception.RequestException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_label_info.*
import kotlinx.android.synthetic.main.list_item_good.*

@SuppressLint("CheckResult")
class LabelInfoActivity : BaseActivity(),OnRefreshListener {

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
        listOf(barcode, power, rssi).forEach { it.inputType = InputType.TYPE_NULL }

        update.setOnClickListener { request(Action.UPDATE) }
        scan.setOnClickListener { request(Action.SCAN) }
        lightOn.setOnClickListener { request(Action.FLICK_ON) }
        lightOff.setOnClickListener { request(Action.FLICK_OFF) }
        getGood()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_label_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.unbind -> {
                if (!label.isBound())
                    showFailTipDialog("标签未绑定")
                else
                    unbind()
            }
            R.id.enable -> request(Action.ENABLE)
            R.id.disable -> request(Action.DISABLE)
            R.id.remove -> request(Action.REMOVE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        ESLS.instance.service.tag(label.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(!it.isSuccess())
                    throw RequestException(it.msg)
                if(it.data.isEmpty())
                    throw RequestException("标签不存在")
                render(it.data.first())
            },{
                RequestExceptionHandler.handle(this,it)
            })
    }

    private fun getGood() {
        if (!label.isBound()) {
            good.visibility= View.GONE
            return
        }
        ESLS.instance.service.good(label.goodId!!)
            .subscribeOn(Schedulers.io())
            .doOnNext {
                if (!it.isSuccess())
                    throw RequestException(it.msg)
            }
            .map { it.data.first() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                renderGood(it)
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }


    private fun renderGood(good: Good) {
        name.text = "名称: ${good.name}"
        stock.text = "库存: ${good.stock}"
        provider.text = "供应商: ${good.provider}"
        unit.text = "单位: ${good.unit}"
        price.text = "原价: ${good.price}"
    }

    private fun render(label: Label) {
        this.label = label
        barcode.setText(label.barCode)
        size.setText("${label.resolutionWidth} x ${label.resolutionHeight}")
        power.setText(label.power.toString())
        rssi.setText(label.tagRssi.toString())
    }
    private fun unbind(){
        val tipDialog = createLoadingTipDialog("正在解除绑定")
        ESLS.instance.service.bind("id", label.goodId!!, "id", label.id, 0)
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
                if (!it.isSuccess()) {
                    throw RequestException(it.data)
                }
                showSuccessTipDialog(it.data)
            }, {
                RequestExceptionHandler.handle(this,it)
            })
    }


    private fun request(action: Action) {
        val actionName = action.desc
        val tipDialog = createLoadingTipDialog("正在$actionName")
        val observable =
            with(ESLS.instance.service) {
                when (action) {
                    Action.FLICK_ON -> light(1, 0, RequestBean("id", label.id))
                    Action.FLICK_OFF -> light(0, 0, RequestBean("id", label.id))
                    Action.UPDATE -> flush(0, RequestBean("id", label.id))
                    Action.SCAN -> scan(0, RequestBean("id", label.id))
                    Action.ENABLE -> status(1, RequestBean("id", label.id))
                    Action.DISABLE -> status(0, RequestBean("id", label.id))
                    Action.REMOVE -> remove(0, RequestBean("id", label.id))
                }
            }
        observable
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
                if (stat.error) {
                    throw RequestException("${actionName}失败")
                }
                showSuccessTipDialog("${actionName}成功")
            }, {
                RequestExceptionHandler.handle(this, RequestException("${actionName}失败", it))
            })
    }

}