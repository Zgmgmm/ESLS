package dev.zgmgmm.esls

import android.os.Bundle
import android.widget.SearchView
import dev.zgmgmm.esls.bean.Label
import dev.zgmgmm.esls.bean.QueryItem
import dev.zgmgmm.esls.bean.RequestBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.view.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import retrofit2.HttpException

class LabelManageActivity : BaseActivity() {
    lateinit var scanBroadcastReceiver: ScanBroadcastReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_manage)
        val search=findViewById<SearchView>(R.id.search)
        search.isSubmitButtonEnabled = true
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                query(query)
                return true
            }
            override fun onQueryTextChange(newText: String) = false
        })

        scanBroadcastReceiver = ScanBroadcastReceiver.register(this) {
            search.user_input.setText(it)
            query(it)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(scanBroadcastReceiver)
        super.onDestroy()
    }

    // 查询标签
    fun query(barCode: String) {
        var disposable: Disposable? = null
        val progressDialog = indeterminateProgressDialog("正在查询标签") {
            setOnDismissListener {
                disposable?.dispose()
            }
        }
        disposable = ESLS.instance.service.searchTag("=", 0, 1, RequestBean(listOf(QueryItem("barCode", barCode))))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe {
                progressDialog.show()
            }
            .doAfterTerminate {
                progressDialog.dismiss()
            }
            .subscribe({
                if(it.data.isEmpty()){
                    toast("标签不存在")
                }else{
                    LabelInfoActivity.start(this@LabelManageActivity, it.data[0])
                }
            }, {
                val e = it as HttpException
                toast(e.response().errorBody().toString())
            })
    }
}


val label = Label(
    "",
    "",
    "21234124",
    1,
    1,
    4,
    1.6,
    false,
    "1",
    "",
    1,
    2,
    "",
    "",
    1,
    "",
    1,
    1,
    1,
    1
)