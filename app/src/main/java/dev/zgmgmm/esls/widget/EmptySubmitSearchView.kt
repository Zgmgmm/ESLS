package dev.zgmgmm.esls.widget

import android.content.Context
import android.support.v7.widget.SearchView
import android.util.AttributeSet
import android.view.View

/**
 * Created by: Jens Klingenberg (jensklingenberg.de)
 * GPLv3
 *
 *  This SearchView gets triggered even when the query submit is empty
 *
 * */
class EmptySubmitSearchView : SearchView {


    lateinit var mSearchSrcTextView: SearchView.SearchAutoComplete
    lateinit var listener: SearchView.OnQueryTextListener

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setOnQueryTextListener(listener: SearchView.OnQueryTextListener) {
        super.setOnQueryTextListener(listener)
        this.listener = listener
        mSearchSrcTextView = this.findViewById(android.support.v7.appcompat.R.id.search_src_text)
        this.findViewById<View>(android.support.v7.appcompat.R.id.search_go_btn)
            .setOnClickListener {
                listener.onQueryTextSubmit(query.toString())
            }
        mSearchSrcTextView.setOnEditorActionListener { _, _, _ ->
            listener.onQueryTextSubmit(query.toString())
            true
        }
    }

    fun setText(text: CharSequence) {
        mSearchSrcTextView.setText(text)
    }
}