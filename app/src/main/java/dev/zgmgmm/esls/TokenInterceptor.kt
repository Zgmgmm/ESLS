package dev.zgmgmm.esls

import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import dev.zgmgmm.esls.activity.LoginActivity
import dev.zgmgmm.esls.base.BaseActivity
import okhttp3.Interceptor
import okhttp3.Response
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class TokenInterceptor : Interceptor, AnkoLogger {
    var token = ""
    override fun intercept(chain: okhttp3.Interceptor.Chain): Response {
        info("附加 token: $token")
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("x-esls-token", token)
        val request = requestBuilder.build()
        val response = chain.proceed(request)
        val token = response.header("x-esls-token")
        info("返回 token: $token")
        this.token = token ?: "1111"
        if (response.code() == 403) {
            val context = BaseActivity.currentActivity.get()!!
            context.runOnUiThread {
                QMUIDialog.MessageDialogBuilder(context)
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setMessage("认证失败,请重新登录")
                    .addAction("确定") { dialog, _ ->
                        dialog.dismiss()
                        LoginActivity.start(context)
                    }
                    .show()
            }
        }
        return response
    }
}