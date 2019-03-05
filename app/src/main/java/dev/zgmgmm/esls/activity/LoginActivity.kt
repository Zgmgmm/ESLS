package dev.zgmgmm.esls.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.TipDialogUtil
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.bean.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast

class LoginActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)
        user_input.setText(savedInstanceState?.getString("user"))
        password_input.setText(savedInstanceState?.getString("password"))
        login.setOnClickListener {
            //            startActivity(Intent(this@LoginActivity, TestLabelListActivity::class.java))

//            val label= Label(id="12123323",tagRssi= "-226.6",state="1",power = "23%")
//            LabelInfoActivity.start(this,label)
//            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            login();
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
        }
        exit.setOnClickListener {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.setting -> {
                ServerConfigActivity.start(this)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu_login, menu)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("user", user_input.text.toString())
        outState.putString("password", password_input.text.toString())
        super.onSaveInstanceState(outState)
    }

    private fun onLoginSuccess(user: User) {
        // TODO
        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
    }

    @SuppressLint("CheckResult")
    private fun login() {
        val user = user_input.text.toString()
        val password = password_input.text.toString()
        if (user.isBlank()) {
            toast("请填写用户名")
            user_input.requestFocus()
            return
        }
        if (password.isBlank()) {
            toast("请填写密码")
            password_input.requestFocus()
            return
        }
        val tipDialog = TipDialogUtil.createLoadingTipDialog(this, "正在登录")

        ESLS.instance.service.login(user, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe { disposable ->
                tipDialog.setOnCancelListener {
                    disposable.dispose()
                }
                tipDialog.show()
            }
            .doFinally {
                tipDialog.dismiss()
            }
            .subscribe({
                if (it.isSuccess()) {
                    onLoginSuccess(it.data)
                } else {
                    TipDialogUtil.showFailTipDialog(this, "登录失败: ${it.msg}")
                }
            }) {
                TipDialogUtil.showFailTipDialog(this, "登录失败: ${it}")
            }
    }


}