package dev.zgmgmm.esls.activity

import RequestExceptionHandler
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.model.Response
import dev.zgmgmm.esls.model.SMS
import dev.zgmgmm.esls.showFailTipDialog
import dev.zgmgmm.esls.showInfoTipDialog
import dev.zgmgmm.esls.showSuccessTipDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_captcha.*
import retrofit2.HttpException
import java.util.*
import kotlin.math.roundToLong


class CaptchaActivity : BaseActivity() {
    lateinit var sms: SMS
    lateinit var user: String
    lateinit var pwd: String

    var availableTime: Long = 0
    val sendDuration: Long = 15 * 1000

    companion object {
        fun start(context: Context, user: String, pwd: String, tel: String) {
            val intent = Intent(context, CaptchaActivity::class.java)
            intent.putExtra("user", user).putExtra("pwd", pwd).putExtra("tel", tel)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(dev.zgmgmm.esls.R.layout.activity_captcha)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        with(intent) {
            user = getStringExtra("user")
            pwd = getStringExtra("user")
            sms = SMS(getStringExtra("tel"), "AUTH")
        }

        phone.text = decoratedPhone(sms.phoneNumber)



        verify.setOnClickListener {
            val code = captcha.text.toString()
            if (code.isBlank()) {
                showFailTipDialog("请输入验证码")
                return@setOnClickListener
            }
            identify(code)
        }

        send.setOnClickListener { sendCaptcha() }
        reset()
    }

    fun decoratedPhone(phone: String): String {
        if (phone.length != 11) {
            return phone
        }
        return phone.substring(0, 3) + "****" + phone.substring(7, 11)
    }

    lateinit var timer: Timer

    override fun onResume() {
        super.onResume()
        timer = Timer(true)
        timer.schedule(object : TimerTask() {
            override fun run() {
                val ttw =
                    ((availableTime - System.currentTimeMillis()) / 1e3).roundToLong()
                if (ttw > 0) {
                    runOnUiThread { send.text = "$ttw s" }
                } else {
                    runOnUiThread { send.text = "获取" }
                }
            }
        }, Date(), 1e3.toLong())
    }

    override fun onPause() {
        timer.cancel()
        super.onPause()
    }


    @SuppressLint("CheckResult")
    fun identify(code: String) {
        sms.userCode = code
        ESLS.instance.service.identify(sms)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .subscribe({
                if (it.code == 1) {
                    showSuccessTipDialog("验证成功")
                    startActivity(Intent(this@CaptchaActivity, HomeActivity::class.java))
                } else {
                    showFailTipDialog(it.msg)
                }
            }) {
                if (it is HttpException) {
                    try {
                        val resp = Gson().fromJson(
                            it.response()?.errorBody()?.string(),
                            Response::class.java
                        )
                        if (resp.code > 0) {
                            showFailTipDialog(resp.data.toString())
                            return@subscribe
                        }
                    } catch (e: Exception) {
                    }
                }
                RequestExceptionHandler.handle(this, it)
            }
    }

    fun reset() {
        captcha.setText("")
    }

    @SuppressLint("CheckResult")
    fun sendCaptcha() {
        val ttw = ((availableTime - System.currentTimeMillis()) / 1e3).roundToLong()
        if (ttw > 0) {
            return
        }
        reset()
        ESLS.instance.service.sendCaptcha(sms)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .subscribe({
                showInfoTipDialog(it.data)
                availableTime = System.currentTimeMillis() + sendDuration
            }) {
                showFailTipDialog("发送失败")
            }

    }
}