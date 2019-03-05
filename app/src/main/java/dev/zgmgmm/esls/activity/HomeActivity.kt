package dev.zgmgmm.esls.activity

import android.content.Intent
import android.os.Bundle
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.base.BaseActivity
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        good_manage.setOnClickListener {
            startActivity(GoodQueryActivity::class.java)
        }
        label_manage.setOnClickListener {
            startActivity(LabelQueryActivity::class.java)
        }
        bind.setOnClickListener {
            startActivity(BindActivity::class.java)
        }

        logout.setOnClickListener {
            //清空任务栈，跳转至登录页面
            ESLS.instance.token = ""
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

    }
}