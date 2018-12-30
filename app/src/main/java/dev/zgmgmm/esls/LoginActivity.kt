package dev.zgmgmm.esls

import android.content.*
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast


class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        user_input.setText(savedInstanceState?.getString("user"))
        password_input.setText(savedInstanceState?.getString("password"))
        login.setOnClickListener {
            startActivity(Intent(this@LoginActivity,HomeActivity::class.java))

//            login();
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
        }
    }

    fun login(){
        val user=user_input.text.toString()
        val password=password_input.text.toString()
        if(user.isBlank()){
            toast("请填写用户名")
            user_input.requestFocus()
            return
        }
        if(password.isBlank()){
            toast("请填写密码")
            password_input.requestFocus()
            return
        }
        var disposable:Disposable?=null
        var progressDialog=indeterminateProgressDialog("正在登录"){
                setButton(DialogInterface.BUTTON_NEGATIVE,"取消"){dialog,which->
                    if(disposable?.isDisposed == false){
                        disposable!!.dispose()
                    }
                    dialog.dismiss()
                }
        }
        var msg=""
        disposable=ESLS.instance.service.login(user,password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doFinally{
                progressDialog.dismiss()
                if(msg.isNotBlank())
                    toast(msg);
            }
            .subscribe({
                when(it.code){
                    0->{
                        startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
                    }
                    else->{
                        msg=it.msg
                    }
                }
            },{
                msg=it.toString()
            })
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("user",user_input.text.toString())
        outState.putString("password",password_input.text.toString())
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        super.onDestroy()
    }


}