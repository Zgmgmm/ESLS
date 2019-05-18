package dev.zgmgmm.esls.activity

import android.app.Activity
import android.content.Intent
import com.king.zxing.CaptureActivity


class CameraScanAcvitity: CaptureActivity() {
    override fun onResultCallback(result: String?): Boolean {
        val intent = Intent()
        intent.putExtra("result", result)
        setResult(Activity.RESULT_OK, intent)
        finish()
        return true
    }
}