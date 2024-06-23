package com.yeloe.attentanceapp.ui.layout_manager

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yeloe.attentanceapp.utils.Constant

class WrapContentLinearLayoutManager : LinearLayoutManager {
    constructor(context: Context) : super(context)
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(
        context, orientation, reverseLayout
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    )

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            if (state.isPreLayout) {
                return  // Ignore layout on pre-layout
            }
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e(Constant.TEACHER_LOG, "Caught IndexOutOfBoundsException in RecyclerView")
        }
    }
}
