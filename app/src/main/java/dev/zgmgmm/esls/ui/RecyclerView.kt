package dev.zgmgmm.esls.ui
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent

class RecyclerView(context: Context,attr: AttributeSet): android.support.v7.widget.RecyclerView(context,attr) {
    var gestureDetector: GestureDetector
    var itemClickListener: OnItemClickListener? =null
    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            //长按事件
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val childView = findChildViewUnder(e.x, e.y)
                if (childView != null) {
                    val position = getChildLayoutPosition(childView)
                    itemClickListener?.onItemClick(position)
                }
                return super.onSingleTapUp(e)
            }
        })
        val simpleOnItemTouchListener=object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(e)
            }
        }
        addOnItemTouchListener(simpleOnItemTouchListener)
    }

    fun setOnItemClickListener( listener:OnItemClickListener ) {
        itemClickListener=listener

    }

    interface OnItemClickListener{
        fun onItemClick(position:Int)
    }
}