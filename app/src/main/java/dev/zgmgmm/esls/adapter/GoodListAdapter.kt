package dev.zgmgmm.esls.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.activity.GoodInfoActivity
import dev.zgmgmm.esls.model.Good

class GoodListAdapter(private var data: List<Good>) : RecyclerView.Adapter<GoodListAdapter.ViewHolder>() {
    var onGoodItemClickListener: OnItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_good, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 绑定数据
        with(data[position]) {
            if (needReplenish) {//需要补货，红字提示
                holder.stock.setTextColor(Color.RED)
            }
            holder.name.text = "名称: $name"
            holder.stock.text = "库存: $stock"
            holder.provider.text = "供应商: $provider"
            holder.unit.text = "单位: $unit"
            holder.price.text = "原价: ${price.toString()}"
            holder.barCode.text="条形码: $barCode"
        }
        holder.view.setOnClickListener {
            GoodInfoActivity.start(
                it.context, data[position]
            )
        }
    }

    override fun getItemId(position: Int): Long {
        return data[position].id.toLong()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onGoodItemClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view: View = itemView
        var name: TextView = itemView.findViewById(R.id.name)
        var stock: TextView = itemView.findViewById(R.id.stock)
        var provider: TextView = itemView.findViewById(R.id.provider)
        var unit: TextView = itemView.findViewById(R.id.unit)
        var price: TextView = itemView.findViewById(R.id.price)
        var barCode: TextView = itemView.findViewById(R.id.barCode)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, good: Good)
    }
}