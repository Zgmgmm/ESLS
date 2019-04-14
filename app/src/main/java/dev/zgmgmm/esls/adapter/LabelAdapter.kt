package dev.zgmgmm.esls.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.bean.Label
import org.jetbrains.anko.find

class LabelAdapter(context: Context, val resource: Int, val labels: MutableList<Label>) :
    ArrayAdapter<Label>(context, resource, labels) {
    val checked = mutableListOf<Label>()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = labels[position]
        val view = LayoutInflater.from(context).inflate(resource, null)
        val size = "${label.resolutionWidth} X ${label.resolutionHeight}"
        view.find<TextView>(R.id.barcode).text = "条码: ${label.barCode}"
        view.find<TextView>(R.id.type).text = "类型: ${label.screenType}"
        view.find<TextView>(R.id.size).text = "宽x高: $size"
        view.find<TextView>(R.id.power).text = "电量: ${label.power}"
        view.find<TextView>(R.id.state).text = "状态: ${label.state}"
        val checkbox = view.find<CheckBox>(R.id.checkbox)
        view.setOnClickListener {
            checkbox.toggle()
            checkbox.callOnClick()
        }
        checkbox.setOnClickListener {
            if (checkbox.isChecked)
                checked.add(label)
            else
                checked.remove(label)
        }
        view.tag = label
        return view
    }
}