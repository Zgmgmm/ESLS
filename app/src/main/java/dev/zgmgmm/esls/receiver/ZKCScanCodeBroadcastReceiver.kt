package dev.zgmgmm.esls.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * 监听zkc手持终端扫码广播
 */
 class ZKCScanCodeBroadcastReceiver(var onScanCode: (String) -> Unit) : BroadcastReceiver() {
    companion object {
        fun register(context: Context, onScanCode: (String) -> Unit): ZKCScanCodeBroadcastReceiver {
            val scanBroadcastReceiver = ZKCScanCodeBroadcastReceiver(onScanCode)
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.zkc.scancode")
            context.registerReceiver(scanBroadcastReceiver, intentFilter)
            return scanBroadcastReceiver
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.extras.getString("code")
        if (text != null)
            onScanCode(text)
    }
}