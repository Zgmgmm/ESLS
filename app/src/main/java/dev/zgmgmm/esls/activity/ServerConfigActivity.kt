package dev.zgmgmm.esls.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.TipDialogUtil
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
        val API_BASE_URL: String =getString(R.string.pref_api_base_url)
        url.setText(defaultSharedPreferences.getString(API_BASE_URL,""))
        save.setOnClickListener {
            val url = url.text.toString()
            ESLS.instance.apiBaseUrl = url
            val success = defaultSharedPreferences.edit().putString(API_BASE_URL, url).commit()
            if (success) {
                TipDialogUtil.showSuccessTipDialog(this, "已保存")
            } else {
                TipDialogUtil.showFailTipDialog(this, "保存失败")
            }

        }
        cancel.setOnClickListener {
            finish()
        }
        super.onCreate(savedInstanceState)
    }

}