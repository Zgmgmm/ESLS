package dev.zgmgmm.esls.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.AnkoLogger
import java.lang.ref.WeakReference


@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), AnkoLogger {
    companion object {
        lateinit var currentActivity: WeakReference<BaseActivity>
    }

    fun startActivity(activityClass: Class<out Activity>) {
        startActivity(Intent(this, activityClass))
    }

    override fun onResume() {
        currentActivity = WeakReference(this)
        super.onResume()
    }
}