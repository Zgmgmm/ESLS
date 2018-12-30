package dev.zgmgmm.esls

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        good_manage.setOnClickListener{
            //TODO
            Toast.makeText(this,"good_manage",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,GoodManageActivity::class.java))
        }
        label_manage.setOnClickListener{
            //TODO
            Toast.makeText(this,"label_manage",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,LabelManageActivity::class.java))
        }
        bind.setOnClickListener{
            //TODO
            Toast.makeText(this,"bind",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,BindActivity::class.java))
        }

        logout.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }

    }
}