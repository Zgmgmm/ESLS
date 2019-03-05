package dev.zgmgmm.esls

import android.app.Application
import android.content.Context
import android.support.v4.content.ContextCompat
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import dev.zgmgmm.esls.activity.LoginActivity
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.service.Service
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.info
import org.jetbrains.anko.runOnUiThread
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
                layout.setPrimaryColorsId(android.R.color.transparent, R.color.colorPrimary);//全局设置主题颜色
                ClassicsHeader.REFRESH_HEADER_PULLING = "下拉刷新"
                ClassicsHeader.REFRESH_HEADER_REFRESHING = "正在刷新..."
                ClassicsHeader.REFRESH_HEADER_LOADING = "正在加载..."
                ClassicsHeader.REFRESH_HEADER_RELEASE = "释放刷新"
                ClassicsHeader.REFRESH_HEADER_FINISH = "刷新完成"
                ClassicsHeader.REFRESH_HEADER_FAILED = "刷新失败"
                ClassicsHeader(context)
                    .setEnableLastTime(false)
            };

            //设置全局的Footer构建器
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context: Context, layout: RefreshLayout ->
                //指定为经典Footer，默认是 BallPulseFooter
                val color = ContextCompat.getColor(context, R.color.colorPrimary)
                BallPulseFooter(context).setAnimatingColor(color);
            }
        }
    }

    lateinit var retrofit: Retrofit
    lateinit var service: Service
    var token = ""
    var apiBaseUrl: String = ""
        set(value) {
            field = value
            val loggingInterceptor = HttpLoggingInterceptor { info(it) }
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            val tokenInterceptor = TokenInterceptor()
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(tokenInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
            retrofit = Retrofit.Builder()
                .baseUrl(value)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
            service = retrofit.create(Service::class.java)
        }


    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        val API_BASE_URL: String =getString(R.string.pref_api_base_url)
        var url = defaultSharedPreferences.getString(API_BASE_URL, "")
        if (url == "") {
            url = getString(R.string.default_api_base_url)
            defaultSharedPreferences.edit().putString("api_base_url", url).apply()
        }
        apiBaseUrl=url

    }

//    inner class TokenInterceptor : Interceptor {
//        override fun intercept(chain: okhttp3.Interceptor.Chain): Response {
//            info("Add token to header: $token")
//            val original = chain.request()
//            val requestBuilder = original.newBuilder()
//                .header("x-esls-token", token)
//            val request = requestBuilder.build()
//            val response = chain.proceed(request)
//            val token = response.header("x-esls-token")
//            info("Retrieve token from header: $token")
//            this@ESLS.token = token ?: "1111"
//            if (response.code() == 403) {
//                val context = BaseActivity.currentActivity.get()!!
//                runOnUiThread {
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
//            return response
//        }
//    }
}