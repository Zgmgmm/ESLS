package dev.zgmgmm.esls.activity

import RequestExceptionHandler
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import dev.zgmgmm.esls.*
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.exception.RequestException
import dev.zgmgmm.esls.interceptor.TokenInterceptor
import dev.zgmgmm.esls.model.User
import dev.zgmgmm.esls.model.UserInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.info

class LoginActivity : BaseActivity() {
    companion object {
        const val STATE_USERNAME = "STATE_USERNAME"
        const val STATE_PASSWORD = "STATE_PASSWORD"
        /**
         * 需要进行检测的权限数组
         */
        val PERMISSIONS = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA
        )

        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("may auto login", false)

            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)
        defaultSharedPreferences.run {
            user_input.setText(getString(Constant.Pref.REMEMBERED_USERNAME, null))
            password_input.setText(getString(Constant.Pref.REMEMBERED_PASSWORD, null))
            remember_me.isChecked = getBoolean(Constant.Pref.REMEMBER_ME, false)
            auto_login.isChecked = getBoolean(Constant.Pref.AUTO_LOGIN, false)
        }
        savedInstanceState?.getString(STATE_USERNAME).run {
            if (this != null) user_input.setText(this)
        }
        savedInstanceState?.getString(STATE_PASSWORD).run {
            if (this != null) password_input.setText(this)
        }
        login.setOnClickListener {
            login()
        }
        exit.setOnClickListener {
            finish()
        }

        remember_me.setOnClickListener {
            if (!remember_me.isChecked)
                auto_login.isChecked = false
        }

        val mayAutoLogin = intent.getBooleanExtra("may auto login", true)
        auto_login.setOnClickListener {
            if (auto_login.isChecked)
                remember_me.isChecked = true
        }
        if (mayAutoLogin && auto_login.isChecked) {
            runOnUiThread {
                login()
            }
        }
        checkPermissions(PERMISSIONS)

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
        outState.putString(STATE_USERNAME, user_input.text.toString())
        outState.putString(STATE_PASSWORD, password_input.text.toString())
        super.onSaveInstanceState(outState)
    }

    private fun onLoginSuccess(user: UserInfo) {
        // TODO
        info("login success: $user")
        defaultSharedPreferences.edit().run {
            putBoolean(Constant.Pref.REMEMBER_ME, remember_me.isChecked)
            putBoolean(Constant.Pref.AUTO_LOGIN, auto_login.isChecked)
            if (remember_me.isChecked) {
                putString(Constant.Pref.REMEMBERED_USERNAME, user_input.text.toString())
                putString(Constant.Pref.REMEMBERED_PASSWORD, password_input.text.toString())
            } else {
                remove(Constant.Pref.REMEMBERED_USERNAME)
                remove(Constant.Pref.REMEMBERED_PASSWORD)
            }
            apply()
        }
        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
    }

    @SuppressLint("CheckResult")
    private fun login() {
        TokenInterceptor.token = ""
        val user = user_input.text.toString()
        val password = password_input.text.toString()
        if (user.isBlank()) {
            showInfoTipDialog("请填写用户名")
            user_input.requestFocus()
            return
        }
        if (password.isBlank()) {
            showInfoTipDialog("请填写密码")
            password_input.requestFocus()
            return
        }
        val tipDialog = createLoadingTipDialog("正在登录")
        ESLS.instance.service.login(User(user, password))
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
                    throw RequestException("登录失败: ${it.msg}")
                }
            }) {
                RequestExceptionHandler.handle(this, it)
            }
    }


}