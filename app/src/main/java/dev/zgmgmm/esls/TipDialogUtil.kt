package dev.zgmgmm.esls

import android.content.Context
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import java.util.*
import kotlin.concurrent.timerTask

fun Context.createTipDialog(tipWord: String, iconType: Int): QMUITipDialog {
    val builder = QMUITipDialog.Builder(this)
    val dialog = builder.setTipWord(tipWord)
        .setIconType(iconType)
        .create()
    dialog.setCanceledOnTouchOutside(true)
    return dialog
}

fun Context.createLoadingTipDialog(tipWord: String): QMUITipDialog {
    val dialog = createTipDialog(tipWord, QMUITipDialog.Builder.ICON_TYPE_LOADING)
    dialog.setCanceledOnTouchOutside(false)
    return dialog
}

fun Context.showTipDialog(tipWord: String, iconType: Int, duration: Long = 1000) {
    val dialog = createTipDialog(tipWord, iconType)
    Timer("Dismiss", false).schedule(timerTask { dialog.dismiss() }, duration)
    dialog.show()
}

fun Context.showSuccessTipDialog(tipWord: String, duration: Long = 1000) {
    showTipDialog(tipWord, QMUITipDialog.Builder.ICON_TYPE_SUCCESS, duration)
}

fun Context.showFailTipDialog(tipWord: String, duration: Long = 1000) {
    showTipDialog(tipWord, QMUITipDialog.Builder.ICON_TYPE_FAIL, duration)
}

fun Context.showInfoTipDialog(tipWord: String, duration: Long = 1000) {
    showTipDialog(tipWord, QMUITipDialog.Builder.ICON_TYPE_INFO, duration)
}


