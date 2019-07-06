package dev.zgmgmm.esls.interceptor

import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import dev.zgmgmm.esls.Constant
import dev.zgmgmm.esls.activity.LoginActivity
import dev.zgmgmm.esls.base.BaseActivity
import okhttp3.Interceptor
import okhttp3.Response
import org.jetbrains.anko.AnkoLogger

class TokenInterceptor : Interceptor, AnkoLogger {
    companion object {
        var token = ""
    }

    override fun intercept(chain: okhttp3.Interceptor.Chain): Response {
        val original = chain.request()
        // 添加token
        val requestBuilder = original.newBuilder()
            .header(Constant.HttpHeader.TOKEN, token)
        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // 更新token
        val token = response.header(Constant.HttpHeader.TOKEN)
        if (token != null)
            Companion.token = token

//        val code=response.code()
//        if (code==401) {
//            val context = BaseActivity.currentActivityPref.get()
//            if (context !is LoginActivity) {
//                context?.runOnUiThread {
//                    QMUIDialog.MessageDialogBuilder(context)
//                        .setCancelable(false)
//                        .setCanceledOnTouchOutside(false)
//                        .setMessage("认证失败,请重新登录")
//                        .addAction("确定") { dialog, _ ->
//                            dialog.dismiss()
//                            LoginActivity.start(context)
//                        }
//                        .show()
//                }
//            }
//        }
        return response
    }
}