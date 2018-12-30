package dev.zgmgmm.esls

import android.app.Application
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ESLS : Application(), AnkoLogger {
    lateinit var BASE_URL: String

    companion object {
        internal lateinit var instance: ESLS
    }

    lateinit var retrofit: Retrofit
    lateinit var service: Service
    init{
        instance=this
    }

    override fun onCreate() {
        super.onCreate()
//        Service.BASE_URL = defaultSharedPreferences.getString("SERVICE BASE URL", "http://127.0.0.1:8080")
        BASE_URL = "http://localhost"

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(Util.getOkHttpClientWithLoggingInterceptor(HttpLoggingInterceptor.Level.BODY) {
                info(it)
            })
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        service = retrofit.create(Service::class.java)

    }

}