package dev.zgmgmm.esls

import android.app.Application
import android.content.Context
import android.support.v4.content.ContextCompat
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import dev.zgmgmm.esls.interceptor.TokenInterceptor
import dev.zgmgmm.esls.service.Service
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.getStackTraceString
import org.jetbrains.anko.info
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ESLS : Application(), AnkoLogger {

    companion object {
        internal lateinit var instance: ESLS

        init {
            // 设置SmartRefreshLayout
            // 设置全局的Header构建器
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context: Context, layout: RefreshLayout ->
                layout.setPrimaryColorsId(android.R.color.transparent, R.color.colorPrimary)//全局设置主题颜色
                ClassicsHeader.REFRESH_HEADER_PULLING = "下拉刷新"
                ClassicsHeader.REFRESH_HEADER_REFRESHING = "正在刷新..."
                ClassicsHeader.REFRESH_HEADER_LOADING = "正在加载..."
                ClassicsHeader.REFRESH_HEADER_RELEASE = "释放刷新"
                ClassicsHeader.REFRESH_HEADER_FINISH = "刷新完成"
                ClassicsHeader.REFRESH_HEADER_FAILED = "刷新失败"
                ClassicsHeader(context)
                    .setEnableLastTime(false)
            }

            //设置全局的Footer构建器
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context: Context, _: RefreshLayout ->
                //指定为经典Footer，默认是 BallPulseFooter
                val color = ContextCompat.getColor(context, R.color.colorPrimary)
                BallPulseFooter(context).setAnimatingColor(color)
            }
        }
    }

    lateinit var retrofit: Retrofit
    lateinit var service: Service

    var token = ""

    init {
        instance = this
    }

    fun initService(baseUrl:String,requestTimeout:Long): Boolean {
        info("init service: baseUrl=$baseUrl timeout=$requestTimeout s")
        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor { info(it) }.apply { level = HttpLoggingInterceptor.Level.BODY })
                .addInterceptor(TokenInterceptor())
                .connectTimeout(requestTimeout, TimeUnit.SECONDS)
                .readTimeout(requestTimeout, TimeUnit.SECONDS)
                .writeTimeout(requestTimeout, TimeUnit.SECONDS)
                .build()
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
            service = retrofit.create(Service::class.java)
            return true
        } catch (e: Exception) {
            info("init service failed: ${e.getStackTraceString()}")
            return false
        }
    }

    override fun onCreate() {
        super.onCreate()
        val baseUrl=defaultSharedPreferences.getString(Constant.Pref.API_BASE_URL, Constant.Net.DEFAULT_API_BASE_URL)!!
        val requestTimeout=defaultSharedPreferences.getLong(Constant.Pref.REQUEST_TIMEOUT, Constant.Net.DEFAULT_REQUEST_TIMEOUT)
        initService(baseUrl,requestTimeout)
    }
}