package dev.zgmgmm.esls.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.Mock
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.bean.Good
import dev.zgmgmm.esls.showSuccessTipDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.test.*
import org.apache.commons.lang3.RandomUtils
import java.util.*
import java.util.concurrent.TimeUnit


class TestActivity : AppCompatActivity(), OnLoadMoreListener {

    var page = 0
    var pageSize = 10
    var noMore = false

    lateinit var adapter: GoodAdapter
    lateinit var data: MutableList<Good>
    lateinit var tipDialog: QMUITipDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)
        // action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Tip dialog
        QMUITipDialog.Builder(this)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .setTipWord("正在查询商品")
            .create()
        // Recycler View
        refreshLayout.setOnLoadMoreListener(this)

        data = mutableListOf()
        adapter = GoodAdapter(data)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        refreshLayout.autoLoadMore()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
//        query("", false)
        recyclerView.postDelayed(object : TimerTask() {
            override fun run() {
                val positionStart = data.size
                val count = pageSize
                data.addAll(Mock.getGoods(pageSize))
                adapter.notifyItemRangeInserted(positionStart, count)
                refreshLayout.finishLoadMore(2000, false, false)
            }
        }, RandomUtils.nextLong(500, 1500))
    }


    @SuppressLint("CheckResult")
    private fun query(q: String, showTipDialog: Boolean = true) {
        ESLS.instance.service.goods("barcode name provider", q, page, pageSize)
            .timeout(10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe { disposable ->
                if (!showTipDialog)
                    return@doOnSubscribe
                tipDialog.setOnCancelListener {
                    disposable.dispose()
                }
                tipDialog.show()
            }
            .doFinally {
                tipDialog.hide()
            }
            .subscribe({
                val goods = it.data
                if (goods.size < pageSize)
                    noMore = true
                page++
                data.clear()
                data.addAll(goods)
                adapter.notifyDataSetChanged()
                showSuccessTipDialog("查询成功")
            }, {
                RequestExceptionHandler.handle(this, it)
            })

    }

    class GoodAdapter(private var data: List<Good>) : RecyclerView.Adapter<GoodAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_good, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // 绑定数据
            with(data[position]) {
                holder.name.text = name
                holder.provider.text = provider
                holder.unit.text = "单位: $unit"
                holder.price.text = "单价: ${price.toString()}"
            }

        }

        override fun getItemCount(): Int {
            return data.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var name: TextView = itemView.findViewById(R.id.name)
            var provider: TextView = itemView.findViewById(R.id.provider)
            var unit: TextView = itemView.findViewById(R.id.unit)
            var price: TextView = itemView.findViewById(R.id.price)
        }

    }

}
