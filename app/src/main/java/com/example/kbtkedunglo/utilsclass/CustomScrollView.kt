package com.example.kbtkedunglo.utilsclass

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView


class CustomScrollView(context:Context, attrs:AttributeSet) : ScrollView(context, attrs)
{
    var isScrollable = true

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return isScrollable && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return isScrollable && super.onTouchEvent(ev)
    }
}