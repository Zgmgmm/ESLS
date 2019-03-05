package dev.zgmgmm.esls.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.bean.Good

class GoodListAdapter(private var data: List<Good>) : RecyclerView.Adapter<GoodListAdapter.ViewHolder>(){
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

    override fun getItemId(position: Int): Long {
        return data[position].id.toLong()
    }
    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view: View = itemView
        var name: TextView = itemView.findViewById(R.id.name)
        var provider: TextView = itemView.findViewById(R.id.provider)
        var unit: TextView = itemView.findViewById(R.id.unit)
        var price: TextView = itemView.findViewById(R.id.price)
    }

}