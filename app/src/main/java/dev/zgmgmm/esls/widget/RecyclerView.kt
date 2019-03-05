package dev.zgmgmm.esls.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View


/**
 * This RecyclerView supports item click event
 */
class RecyclerView(context: Context, attr: AttributeSet) : android.support.v7.widget.RecyclerView(context, attr) {
    val itemClickListener = mutableListOf<OnItemClickListener>()

    init {
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            //长按事件
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val childView = findChildViewUnder(e.x, e.y)
                if (childView != null) {
                    val position = getChildLayoutPosition(childView)
                    itemClickListener.forEach {
                        it.onItemClick(
                            childView, position, adapter?.getItemId(position) ?: -1
                        )
                    }
                }

                return super.onSingleTapUp(e)
            }
        })
        val simpleOnItemTouchListener = object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(e)
            }
        }
        addOnItemTouchListener(simpleOnItemTouchListener)
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener.clear()
        itemClickListener.add(listener)
    }

    fun addOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener.add(listener)
    }

    interface OnItemClickListener {
        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         *
         *
         * Implementers can call getItemAtPosition(position) if they need
         * to access the labels associated with the selected item.
         *
         * @param view The view within the AdapterView that was clicked (this
         * will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id The row id of the item that was clicked.
         */
        fun onItemClick(view: View, position: Int, id: Long)
    }
}