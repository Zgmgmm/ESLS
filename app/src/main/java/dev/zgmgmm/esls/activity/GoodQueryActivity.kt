package dev.zgmgmm.esls.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import dev.zgmgmm.esls.ESLS
import dev.zgmgmm.esls.R
import dev.zgmgmm.esls.TipDialogUtil
import dev.zgmgmm.esls.adapter.GoodListAdapter
import dev.zgmgmm.esls.base.BaseActivity
import dev.zgmgmm.esls.bean.Good
import dev.zgmgmm.esls.receiver.ZKCScanCodeBroadcastReceiver
import dev.zgmgmm.esls.widget.RecyclerView.OnItemClickListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_good_manage.*
import java.util.concurrent.TimeUnit

class GoodQueryActivity : BaseActivity(), OnLoadMoreListener, OnRefreshListener {


    private val data = ArrayList<Good>()
    private lateinit var adapter: GoodListAdapter
    private lateinit var zkcScanCodeBroadcastReceiver: ZKCScanCodeBroadcastReceiver
    private lateinit var loadingTipDialog: QMUITipDialog

    private var currentQuery = ""
    private var pageSize = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_good_manage)
        // set action bar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // init tip dialog
        loadingTipDialog = QMUITipDialog.Builder(this)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .setTipWord("正在查询商品")
            .create()

        // init search view
        search.isSubmitButtonEnabled = true
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String): Boolean {
                newQuery(q,true)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean = false
        })
        // init recycler view
        adapter = GoodListAdapter(data)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int, id: Long) {
                GoodInfoActivity.start(this@GoodQueryActivity, data[position])
            }
        })

        // init refresh layout
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.finishLoadMoreWithNoMoreData()
        // register zkc scan code broadcastReceiver
        zkcScanCodeBroadcastReceiver = ZKCScanCodeBroadcastReceiver.register(this) {
            search.setText(it)
            newQuery(it,true)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(zkcScanCodeBroadcastReceiver)
        loadingTipDialog.dismiss()
        super.onDestroy()
    }


    @SuppressLint("CheckResult")
    override fun onLoadMore(refreshLayout: RefreshLayout) {
        query(false)
//        val delay = RandomUtils.nextLong(500, 1500)
//        recyclerView.postDelayed({
//            val positionStart = labels.size
//            var count = pageSize
//            var success = true
//            var noMore = false
//            if (delay > 1000) {
//                count = 0
//                success = false
//            }
//            labels.addAll(Mock.getGoods(pageSize))
//            adapter.notifyItemRangeInserted(positionStart, count)
//            refreshLayout.finishLoadMore(500, success, noMore)
//        }, delay)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        newQuery(currentQuery,false)
    }

    private fun newQuery(q: String,showLoadingTipDialog: Boolean = true) {
        currentQuery = q
        data.clear()
        adapter.notifyDataSetChanged()
        refreshLayout.resetNoMoreData()
        query(showLoadingTipDialog)
    }

    @SuppressLint("CheckResult")
    private fun query(showLoadingTipDialog: Boolean) {
        val page = data.size / pageSize
        ESLS.instance.service.goods("barcode name provider", currentQuery, page, pageSize)
            .timeout(10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.from(mainLooper))
            .doOnSubscribe { disposable ->
                if (!showLoadingTipDialog)
                    return@doOnSubscribe
                loadingTipDialog.setOnCancelListener {
                    disposable.dispose()
                }
                loadingTipDialog.show()
            }
            .doFinally {
                loadingTipDialog.hide()
            }
            .subscribe({
                val goods = it.data
                val positionStart = data.size
                val count = goods.size
                val noMore = count < pageSize
                if (count == 0) {
                    refreshLayout.finishLoadMoreWithNoMoreData()
                    TipDialogUtil.showInfoTipDialog(this, "没有更多了")
                    return@subscribe
                }
                data.addAll(goods)
                adapter.notifyItemRangeInserted(positionStart, count)
                refreshLayout.finishLoadMore(500, true, noMore)
                refreshLayout.finishRefresh()
//                TipDialogUtil.showTipDialog(this, "查询成功", QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
            }, {
                refreshLayout.finishLoadMore(false)
                refreshLayout.finishRefresh(false)
                TipDialogUtil.showFailTipDialog(this, "查询失败")
            })
    }
}

