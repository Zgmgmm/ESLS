package dev.zgmgmm.esls.activity

import android.content.Intent
import android.os.Bundle
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import dev.zgmgmm.esls.Constant
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.base.BaseActivity
import kotlinx.android.synthetic.main.activity_home.*
import org.jetbrains.anko.defaultSharedPreferences


class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // action bar
        setSupportActionBar(toolbar)

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
            showLogoutDialog()
        }
    }

    override fun onBackPressed() {
        //实现Home键效果 
        val intent=Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }
    private fun showLogoutDialog(){
        QMUIDialog.MessageDialogBuilder(this)
            .setMessage("点击确认将注销登录")
            .addAction("确定"){ dialog, _ ->
                dialog.dismiss()
                logout()
            }
            .addAction("取消"){ dialog: QMUIDialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logout(){
        //清空任务栈，跳转至登录页面
        ESLS.instance.token = ""
        defaultSharedPreferences.edit().putBoolean(Constant.Pref.AUTO_LOGIN,false).apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}