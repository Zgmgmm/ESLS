//package dev.zgmgmm.esls.ui
//
//import android.content.Context
//import android.view.View.MeasureSpec
//import dev.zgmgmm.esls.ui.FixedAspectRatioFrameLayout
//import android.content.res.TypedArray
//import android.util.AttributeSet
//import android.widget.FrameLayout
//import dev.zgmgmm.esls.R
//
//
//class FixedAspectRatioFrameLayout : FrameLayout {
//    private var mAspectRatioWidth: Int = 0
//    private var mAspectRatioHeight: Int = 0
//
//    constructor(context: Context) : super(context) {}
//
//    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
//
//        init(context, attrs)
//    }
//
//    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
//
//        init(context, attrs)
//    }
//
//    private fun init(context: Context, attrs: AttributeSet) {
//        val a = context.obtainStyledAttributes(attrs, R.styleable.FixedAspectRatioFrameLayout)
//
//        mAspectRatioWidth = a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspectRatioWidth, 4)
//        mAspectRatioHeight = a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspectRatioHeight, 3)
//
//        a.recycle()
//    }
//    // **overrides**
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val originalWidth = MeasureSpec.getSize(widthMeasureSpec)
//
//        val originalHeight = MeasureSpec.getSize(heightMeasureSpec)
//
//        val calculatedHeight = originalWidth * mAspectRatioHeight / mAspectRatioWidth
//
//        val finalWidth: Int
//        val finalHeight: Int
//
//        if (calculatedHeight > originalHeight) {
//            finalWidth = originalHeight * mAspectRatioWidth / mAspectRatioHeight
//            finalHeight = originalHeight
//        } else {
//            finalWidth = originalWidth
//            finalHeight = calculatedHeight
//        }
//
//        super.onMeasure(
//            MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
//            MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)
//        )
//    }
//}