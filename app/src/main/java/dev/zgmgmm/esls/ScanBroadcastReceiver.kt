package dev.zgmgmm.esls

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * 监听扫码广播
 */
class ScanBroadcastReceiver(var consumer: (String) -> Unit) : BroadcastReceiver() {
    companion object {
        fun register(context: Context, consumer: (String) -> Unit): ScanBroadcastReceiver {
            val scanBroadcastReceiver = ScanBroadcastReceiver(consumer)
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.zkc.scancode")
            context.registerReceiver(scanBroadcastReceiver, intentFilter)
            return scanBroadcastReceiver
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.extras.getString("code")
        if (text != null)
            consumer(text)
    }
}