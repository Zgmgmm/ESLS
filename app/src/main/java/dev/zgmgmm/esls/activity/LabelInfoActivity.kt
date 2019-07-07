package dev.zgmgmm.esls.activity

import RequestExceptionHandler
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import dev.zgmgmm.esls.*
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.exception.RequestException
import dev.zgmgmm.esls.model.Label
import dev.zgmgmm.esls.model.QueryItem
import dev.zgmgmm.esls.model.RequestBean
import dev.zgmgmm.esls.model.toBarcodeRequestBean
import dev.zgmgmm.esls.receiver.ZKCScanCodeBroadcastReceiver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_label_info.*

@SuppressLint("CheckResult")
class LabelInfoActivity : BaseActivity() {
    private lateinit var zkcScanCodeBroadcastReceiver: ZKCScanCodeBroadcastReceiver
    private lateinit var label: Label

    companion object {
        fun start(context: Context, label: Label) {
            val intent = Intent(context, LabelInfoActivity::class.java)
            intent.putExtra("label", label)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_info)

        // action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // disable inputs
        listOf(
            goodName,
            goodBarcode,
            labelBarcode,
            size,
            power,
            rssi,
            totalWeight,
            goodNumber,
            weigherPower
        ).forEach {
            it.inputType = InputType.TYPE_NULL
        }

        buttons = listOf(btn1, btn2, btn3, btn4, btn5, btn6)

        val onClick = View.OnClickListener {
            it as TextView
            when (val action = it.text.toString()) {
                "开灯", "关灯", "刷新", "巡检" -> operateTag(action)
                "人工盘点" -> showIntegerInputDialog("输入商品件数", this::manualCount)
                "获取计量", "置零", "获取衡器电量", "去皮" -> operateWeigher(action)
                "校准" -> showIntegerInputDialog("输入校准重量") { weight -> operateWeigher(action, weight) }
                "显示衡器菜单", "显示标签菜单" -> switchMenu()

            }
        }
        buttons.forEach { it.setOnClickListener(onClick) }


        render(intent.getSerializableExtra("label") as Label)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_label_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.unbind -> {
                if (!label.isBound)
                    showFailTipDialog("标签未绑定")
                else
                    unbind()
            }
            R.id.enable -> operateTag("启用")
            R.id.disable -> operateTag("禁用")
            R.id.remove -> operateTag("移除")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        zkcScanCodeBroadcastReceiver = ZKCScanCodeBroadcastReceiver.register(this) {
            query(it)
        }
    }


    override fun onStop() {
        unregisterReceiver(zkcScanCodeBroadcastReceiver)
        super.onStop()
    }


    lateinit var buttons: List<Button>
    private val switchMap = mapOf(
        "开灯" to "获取计量",
        "关灯" to "获取衡器电量",
        "刷新" to "去皮",
        "巡检" to "置零",
        "人工盘点" to "校准",
        "显示衡器菜单" to "显示标签菜单",

        "获取计量" to "开灯",
        "获取衡器电量" to "关灯",
        "去皮" to "刷新",
        "置零" to "巡检",
        "校准" to "人工盘点",
        "显示标签菜单" to "显示衡器菜单"
    )

    private fun switchMenu() {
        buttons.forEach {
            it.text = switchMap[it.text]
        }
    }

    // 查询标签
    @SuppressLint("CheckResult")
    private fun query(barCode: String) {
        ESLS.instance.service.searchTag("=", 0, 1, RequestBean(listOf(QueryItem("barCode", barCode))))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .subscribe({
                if (it.data.isNotEmpty()) {
                    render(it.data.first())
                }
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }


    private fun render(label: Label) {
        this.label = label
        labelBarcode.setText(label.barCode)
        size.setText("${label.resolutionWidth} x ${label.resolutionHeight}")
        power.setText(label.power)
        rssi.setText(label.tagRssi)
        totalWeight.setText("${label.totalWeight} g")
        goodNumber.setText(label.goodNumber.toString())
        weigherPower.setText(label.measurePower)
        if (label.isBound && label.needReplenish)
            goodNumber.setTextColor(Color.RED)
        else
            goodNumber.setTextColor(Color.BLACK)

        goodNumber.visibility = if (label.isBound) View.VISIBLE else View.GONE
        goodName.visibility = View.GONE
        goodBarcode.visibility = View.GONE

        listOf(btn3).forEach { it.visibility = if (label.isBound) View.VISIBLE else View.GONE }

        val isComputeOpen = label.isComputeOpen
        listOf(totalWeight, weigherPower, btn6).forEach {
            it.visibility = when (isComputeOpen) {
                true -> View.VISIBLE
                false -> View.GONE
            }
        }
        btn6.isEnabled = isComputeOpen
        if (!isComputeOpen) {
            if (btn6.text != "显示衡器菜单") {
                switchMenu()
            }
        }

        // good info
        if (!label.isBound)
            return
        goodName.setText(label.goodName)
        goodBarcode.setText(label.goodBarCode)
        goodName.visibility = View.VISIBLE
        goodBarcode.visibility = View.VISIBLE
    }


    private fun manualCount(count: Int) {
        val tipDialog = createLoadingTipDialog("正在保存")
        ESLS.instance.service.manualCount(count, label.toBarcodeRequestBean())
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
                if (!it.isSuccess())
                    throw RequestException(it.data)
                reload()
                showSuccessTipDialog("保存成功")
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }


    private fun reload() {
        query(label.barCode.toString())
    }

    private fun unbind() {
        val tipDialog = createLoadingTipDialog("正在解除绑定")
        ESLS.instance.service.bind("id", label.goodId.toString(), "id", label.id.toString(), 0)
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
                RequestExceptionHandler.handle(this, it)
            })
    }


    private fun operateWeigher(action: String, weight: Int = 0) {
        val tipDialog = createLoadingTipDialog("正在【$action】")
        val mode = when (action) {
            "获取计量" -> 0
            "置零" -> 1
            "去皮" -> 2
            "获取衡器电量" -> 3
            "校准" -> 5
            else -> -1
        }
        if (mode == -1) {
            return
        }
        val requestBean = label.toBarcodeRequestBean()
        val observable = ESLS.instance.service.weigher(mode, requestBean, weight)
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
                if (!it.isSuccess())
                    throw RequestException(it.data)
                val data = parseWeigherResp(it.data)
                val res: String = data["key"] ?: ""
                if (it.data != "成功" && res != "成功") {
                    throw RequestException(it.data)
                }
                if (data.contains("weight"))
                    totalWeight.setText(data["Weight"])
                if (data.contains("power")) {
                    val power = data["power"]
                    totalWeight.setText(power)
                }
                showSuccessTipDialog("【$action】成功")
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }

    private fun operateTag(action: String) {
        val tipDialog = createLoadingTipDialog("正在【$action】")
        val requestBean = RequestBean("id", label.id.toString())
        val observable =
            with(ESLS.instance.service) {
                when (action) {
                    "开灯" -> light(1, 0, requestBean)
                    "关灯" -> light(0, 0, requestBean)
                    "刷新" -> flush(0, requestBean)
                    "巡检" -> scan(0, requestBean)
                    "启用" -> status(1, requestBean)
                    "禁用" -> status(0, requestBean)
                    "移除" -> remove(0, requestBean)
                    else -> Observable.empty()
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
                    throw RequestException("【$action】失败")
                }
                showSuccessTipDialog("【$action】成功")
            }, {
                RequestExceptionHandler.handle(this, it)
            })
    }

}