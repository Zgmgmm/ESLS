package dev.zgmgmm.esls

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dev.zgmgmm.esls.bean.Good
import dev.zgmgmm.esls.ui.RecyclerView.OnItemClickListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_good_manage.*
import kotlinx.android.synthetic.main.activity_login.view.*
import org.jetbrains.anko.toast

class GoodManageActivity : BaseActivity() {
    val data = ArrayList<Good>()
    lateinit var adapter: ListAdapter
    lateinit var scanBroadcastReceiver: ScanBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_good_manage)
        adapter = ListAdapter(data)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String): Boolean {
                query(q)
                return false;
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false;
            }
        });
        list.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                GoodInfoActivity.start(this@GoodManageActivity, data[position])
            }
        })

        scanBroadcastReceiver = ScanBroadcastReceiver.register(this) {
            search.user_input.setText(it)
            query(it)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(scanBroadcastReceiver)
        super.onDestroy()
    }

    // 查询商品
    fun query(q: String) {
        var disposable: Disposable? = null
        disposable = ESLS.instance.service.goods("barcode name provider",q)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .subscribe({
                val goods = it.data
                data.clear()
                data.addAll(goods)
                adapter.notifyDataSetChanged()
                toast("查询成功")
            }, {
                toast("查询失败 $it")
            })
    }

    inner class ListAdapter(private var data: List<Good>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_good, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // 绑定数据
            with(data[position]) {
                holder.name.text = name
                holder.provider.text = provider
                holder.unit.text = unit
                holder.price.text = price.toString()
            }

        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var view: View = itemView
            var name: TextView = itemView.findViewById(R.id.name)
            var provider: TextView = itemView.findViewById(R.id.provider)
            var unit: TextView = itemView.findViewById(R.id.unit)
            var price: TextView = itemView.findViewById(R.id.price)
        }
    }
}


var sample = Good(
    123,
    "农夫三拳矿泉水",
    "Shenzhen",
    "农夫山",
    "瓶",
    "336261432",
    "",
    "",
    "",
    1,
    "儿童节",
    1,
    2.33,
    0.23,
    "330ml"
)