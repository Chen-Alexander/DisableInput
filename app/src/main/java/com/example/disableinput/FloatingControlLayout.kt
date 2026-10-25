package com.example.disableinput

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

class FloatingControlLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val handler = Handler(Looper.getMainLooper())
    private var consecutiveTouchCount = 0
    private var lastTouchTime = 0L
    private val restoreAppearance = Runnable { isActivated = false }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // 只观察每次触碰的开始，仍按原有流程分发给按钮。
        if (event.actionMasked == MotionEvent.ACTION_DOWN ||
            event.actionMasked == MotionEvent.ACTION_OUTSIDE
        ) {
            recordTouch(event.eventTime)
        }
        return super.dispatchTouchEvent(event)
    }

    private fun recordTouch(eventTime: Long) {
        consecutiveTouchCount = if (
            consecutiveTouchCount > 0 && eventTime - lastTouchTime in 0..MAX_TOUCH_INTERVAL_MS
        ) {
            consecutiveTouchCount + 1
        } else {
            1
        }
        lastTouchTime = eventTime

        if (consecutiveTouchCount == 3) {
            consecutiveTouchCount = 0
            // ViewGroup 会把 activated 状态同步到两个按钮。
            isActivated = true
            handler.removeCallbacks(restoreAppearance)
            handler.postDelayed(restoreAppearance, REVEAL_DURATION_MS)
        }
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(restoreAppearance)
        consecutiveTouchCount = 0
        lastTouchTime = 0L
        isActivated = false
        super.onDetachedFromWindow()
    }

    private companion object {
        const val MAX_TOUCH_INTERVAL_MS = 1_000L
        const val REVEAL_DURATION_MS = 2_000L
    }
}
