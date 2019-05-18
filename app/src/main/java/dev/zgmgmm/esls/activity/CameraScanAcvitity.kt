package dev.zgmgmm.esls.activity

import android.app.Activity
import android.content.Intent
import com.king.zxing.CaptureActivity
import org.jetbrains.anko.doAsync


class CameraScanAcvitity: CaptureActivity() {
    companion object{
        var i=1
    }
    val data= listOf("6901826150266","134579060024")
    override fun onResume() {
        super.onResume()
        doAsync {
            Thread.sleep(2000)
            i=(1+i)%data.size
            val result=data[i]
            onResultCallback(result)
        }
    }
    override fun onResultCallback(result: String?): Boolean {
        val intent = Intent()
        intent.putExtra("result", result)
        setResult(Activity.RESULT_OK, intent)
        finish()
        return true
    }
}