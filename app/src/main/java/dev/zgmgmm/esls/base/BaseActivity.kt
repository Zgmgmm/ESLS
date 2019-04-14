package dev.zgmgmm.esls.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import dev.zgmgmm.esls.showInfoTipDialog
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.AnkoLogger
import java.lang.ref.WeakReference


@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), AnkoLogger {
    companion object {
        lateinit var currentActivityPref: WeakReference<BaseActivity>
        val compositeDisposable = CompositeDisposable()
    }


    fun startActivity(activityClass: Class<out Activity>) {
        startActivity(Intent(this, activityClass))
    }

    override fun onResume() {
        currentActivityPref = WeakReference(this)
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    var needCheck = true
    val PERMISSON_REQUESTCODE = 110

    fun checkPermissions(permissions: Array<String>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return
        val needRequestPermissonList = findDeniedPermissions(permissions)
        if (needRequestPermissonList.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                needRequestPermissonList.toTypedArray(),
                PERMISSON_REQUESTCODE
            )
        }

    }

    /**
     * 获取权限集中需要申请权限的列表
     */
    fun findDeniedPermissions(permissions: Array<String>): List<String> {
        val needRequestPermissonList = ArrayList<String>()
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    perm
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm
                )
            ) {
                needRequestPermissonList.add(perm)
            }
        }
        return needRequestPermissonList
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, paramArrayOfInt: IntArray) {
        when (requestCode) {
            PERMISSON_REQUESTCODE -> if (!verifyPermissions(paramArrayOfInt)) {
                showInfoTipDialog("请开启网络权限，否则应用将无法正常使用")
            } else {
                needCheck = false
            }
        }

    }


    /**
     * 检测是否说有的权限都已经授权
     */
    fun verifyPermissions(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

}
