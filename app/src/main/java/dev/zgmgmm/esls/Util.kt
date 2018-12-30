package dev.zgmgmm.esls

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object Util {
    fun getOkHttpClientWithLoggingInterceptor(
        level: HttpLoggingInterceptor.Level,
        logger: (String) -> Unit
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor(logger)
        loggingInterceptor.level = level
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(loggingInterceptor)
        return httpClientBuilder.build()
    }
}