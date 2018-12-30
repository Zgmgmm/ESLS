package dev.zgmgmm.esls.ui

import android.content.Context
import android.support.v7.widget.SearchView
import android.util.AttributeSet

class EmptySubmitSearchView : SearchView {

    /*
* Created by: Jens Klingenberg (jensklingenberg.de)
* GPLv3
*
*   //This SearchView gets triggered even when the query submit is empty
*
* */

    lateinit var mSearchSrcTextView: SearchView.SearchAutoComplete
    lateinit var listener: SearchView.OnQueryTextListener

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun setOnQueryTextListener(listener: SearchView.OnQueryTextListener) {
        super.setOnQueryTextListener(listener)
        this.listener = listener
        mSearchSrcTextView = this.findViewById(android.support.v7.appcompat.R.id.search_src_text)
        mSearchSrcTextView.setOnEditorActionListener { textView, i, keyEvent ->
            listener.onQueryTextSubmit(query.toString())
            true
        }
    }
}