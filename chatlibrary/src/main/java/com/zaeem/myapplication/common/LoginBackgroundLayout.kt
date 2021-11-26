package com.zaeem.myapplication.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.zaeem.myapplication.R
import com.zaeem.myapplication.ui.LoginBackgroundDrawable


class LoginBackgroundLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    val logoView: View by lazy { findViewById(R.id.logoView) }

    init { background = LoginBackgroundDrawable(context) }

    override fun getBehavior() = MoveUpwardBehavior()
}
