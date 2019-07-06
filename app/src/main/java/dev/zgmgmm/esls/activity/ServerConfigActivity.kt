package dev.zgmgmm.esls.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.webkit.URLUtil
import dev.zgmgmm.esls.*
import dev.zgmgmm.esls.base.BaseActivity
import kotlinx.android.synthetic.main.activity_server_config.*
import org.jetbrains.anko.defaultSharedPreferences

class ServerConfigActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ServerConfigActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_server_config)
        toolbar.setNavigationOnClickListener { finish() }
        url.setText(defaultSharedPreferences.getString(Constant.Pref.API_BASE_URL, Constant.Net.DEFAULT_API_BASE_URL))
        timeout.setText(
            defaultSharedPreferences.getLong(
                Constant.Pref.REQUEST_TIMEOUT,
                Constant.Net.DEFAULT_REQUEST_TIMEOUT
            ).toString()
        )
        save.setOnClickListener {
            var newUrl = URLUtil.guessUrl(url.text.toString())
            val timeout = timeout.text.toString().let {
                if (it.isBlank()) {
                    showFailTipDialog("网络超时应大于0秒")
                    return@setOnClickListener
                }
                it.toLong()
            }
            if (timeout <= 0) {
                showFailTipDialog("网络超时应大于0秒")
                return@setOnClickListener
            }
            if (!Patterns.WEB_URL.matcher(newUrl).matches()) {
                showFailTipDialog("请输入合法的地址")
                return@setOnClickListener
            }

            if (!newUrl.endsWith('/'))
                newUrl = "$newUrl/"
            url.setText(newUrl)
            val success =
                ESLS.instance.initService(newUrl, timeout)
                        &&
                        defaultSharedPreferences.edit()
                            .putString(Constant.Pref.API_BASE_URL, newUrl)
                            .putLong(Constant.Pref.REQUEST_TIMEOUT, timeout)
                            .commit()
            if (success) {
                showSuccessTipDialog("已保存")
            } else {
                showFailTipDialog("保存失败")
            }
        }

        cancel.setOnClickListener {
            finish()
        }
        super.onCreate(savedInstanceState)
    }

}