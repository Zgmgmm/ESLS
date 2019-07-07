package dev.zgmgmm.esls.interceptor

import dev.zgmgmm.esls.Constant
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

        return response
    }
}