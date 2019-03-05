package dev.zgmgmm.esls

import android.content.Context
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import java.util.*
import kotlin.concurrent.timerTask

object TipDialogUtil {
    fun createTipDialog(context: Context, tipWord: String, iconType: Int): QMUITipDialog {
        val builder = QMUITipDialog.Builder(context)
        val dialog = builder.setTipWord(tipWord)
            .setIconType(iconType)
            .create()
        return dialog
    }

    fun createLoadingTipDialog(context: Context, tipWord: String): QMUITipDialog {
        return createTipDialog(context, tipWord, QMUITipDialog.Builder.ICON_TYPE_LOADING)
    }

    fun showTipDialog(context: Context, tipWord: String, iconType: Int, duration: Long = 1000) {
        val dialog = createTipDialog(context, tipWord, iconType)
        Timer("Dismiss", false).schedule(timerTask { dialog.dismiss() }, duration)
        dialog.show()
    }

    fun showSuccessTipDialog(context: Context, tipWord: String, duration: Long = 1000) {
        showTipDialog(context, tipWord, QMUITipDialog.Builder.ICON_TYPE_SUCCESS, duration)
    }

    fun showFailTipDialog(context: Context, tipWord: String, duration: Long = 1000) {
        showTipDialog(context, tipWord, QMUITipDialog.Builder.ICON_TYPE_FAIL, duration)
    }

    fun showInfoTipDialog(context: Context, tipWord: String, duration: Long = 1000) {
        showTipDialog(context, tipWord, QMUITipDialog.Builder.ICON_TYPE_INFO, duration)
    }
}
