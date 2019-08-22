package dev.zgmgmm.esls

import android.content.Context
import android.text.method.DigitsKeyListener
import com.qmuiteam.qmui.widget.dialog.QMUIDialog

fun parseWeigherResp(s: String?): Map<String, String> {
    if (s == null) {
        return emptyMap()
    }
    val begin = s.indexOf("{")
    val end = s.lastIndexOf("}")
    if (begin == -1 || end == -1)
        return emptyMap()

    val data = s.substring(begin + 1, end)
    val map = hashMapOf<String, String>()
    data.split(",")
        .map(String::trim)
        .map { rawKV ->
            rawKV.split("=")
        }
        .forEach { item ->
            val k = item[0]
            val v = item[1]
            map[k] = v
        }
//    if(map.containsKey("key")){
//        val res=map["key"]!!
//        if(res!="成功"){
//            map["key"]=res.substring(res.indexOf(" ")+1)
//        }
//    }
    return map
}


fun Context.showIntegerInputDialog(title: String, action: (Int) -> Unit) {
    val builder = QMUIDialog.EditTextDialogBuilder(this)
    val dialog = builder
        .setTitle(title)
        .addAction("确定") { dialog, _ ->
            dialog.dismiss()
            val i = builder.editText.text.toString().toIntOrNull()
            if (i == null)
                this.showFailTipDialog("格式错误")
            else
                action(i)
        }
        .addAction("取消") { dialog, _ ->
            dialog.cancel()
        }
        .create()
    builder.editText.keyListener = DigitsKeyListener.getInstance("1234567890")
    dialog.show()
}


fun Context.showFloatInputDialog(title: String, action: (Float) -> Unit, placeholder: String = "") {
    val builder = QMUIDialog.EditTextDialogBuilder(this)
    val dialog = builder
        .setTitle(title)
        .setPlaceholder(placeholder)
        .addAction("确定") { dialog, _ ->
            dialog.dismiss()
            val f = builder.editText.text.toString().toFloatOrNull()
            if (f == null)
                this.showFailTipDialog("格式错误")
            else
                action(f)
        }
        .addAction("取消") { dialog, _ ->
            dialog.cancel()
        }
        .create()
    builder.editText.keyListener = DigitsKeyListener.getInstance("1234567890.")
    dialog.show()
}