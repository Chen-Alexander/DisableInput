package com.example.disableinput

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Process
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
import android.view.WindowManager.LayoutParams.WRAP_CONTENT
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.core.app.NotificationCompat


class DisableInputAccessibilityService : AccessibilityService() {
    private val tag = "DisableInputAccessibilityService"

    private var disableInput = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (disableInput) {
//            event?.eventType = 0
        }
    }

    override fun onInterrupt() {
        Log.i(tag, "onInterrupted!")
        Toast.makeText(this, getString(R.string.onInterrupt_tip), Toast.LENGTH_LONG).show()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(tag, "onServiceConnected() called")
        val serviceInfo = AccessibilityServiceInfo().apply {
            // 监听所有事件
            this.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
            // 监听的应用包名，支持多个
//            packageNames = arrayOf("com.tencent.mm")
            notificationTimeout = 10
        }
        setServiceInfo(serviceInfo)
        initView()
    }

    private fun initView() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val layoutParams = WindowManager.LayoutParams().apply {
            type = TYPE_ACCESSIBILITY_OVERLAY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            format = PixelFormat.TRANSLUCENT
            flags = FLAG_LAYOUT_NO_LIMITS or
                    FLAG_LAYOUT_IN_SCREEN
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        val rootView =
            LayoutInflater.from(this).inflate(R.layout.layout_floating_control_view, null, false)
        val btnToggle = rootView.findViewById<AppCompatToggleButton>(R.id.btnToggle)
        btnToggle.setOnCheckedChangeListener { _, isChecked ->
            disableInput = isChecked
            wm?.removeView(rootView)
            (getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager)?.interrupt()
        }
        wm?.addView(rootView, layoutParams)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Toast.makeText(this, getString(R.string.onUnbind_tip), Toast.LENGTH_LONG).show()
        Log.d(tag, "onUnbind() called with: intent = $intent")
        return super.onUnbind(intent)
    }
}