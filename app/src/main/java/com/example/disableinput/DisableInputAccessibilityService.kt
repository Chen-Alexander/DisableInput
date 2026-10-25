package com.example.disableinput

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
import android.view.WindowManager.LayoutParams.WRAP_CONTENT
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.disableinput.databinding.LayoutFloatingControlViewBinding


class DisableInputAccessibilityService : AccessibilityService() {
    private val windowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private var floatingBinding: LayoutFloatingControlViewBinding? = null

    private var disableInput = true

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 输入屏蔽由悬浮窗口控制，不修改系统交付的只读无障碍事件。
    }

    override fun onInterrupt() {
        Toast.makeText(this, getString(R.string.onInterrupt_tip), Toast.LENGTH_LONG).show()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val serviceInfo = AccessibilityServiceInfo().apply {
            // 监听所有事件
            this.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
            notificationTimeout = 10
        }
        setServiceInfo(serviceInfo)
        initView()
    }

    private fun initView() {
        if (floatingBinding != null) return

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
        val binding = LayoutFloatingControlViewBinding.inflate(LayoutInflater.from(this))
        binding.btnExit.setOnClickListener {
            removeFloatingView()
            disableSelf()
        }
        binding.btnToggle.setOnClickListener {
            disableInput = !disableInput
            updateFloatingView()
        }
        windowManager.addView(binding.root, layoutParams)
        floatingBinding = binding
        updateFloatingView()
    }

    private fun updateFloatingView() {
        val binding = floatingBinding ?: return
        val layoutParams = binding.root.layoutParams as WindowManager.LayoutParams
        layoutParams.flags = FLAG_LAYOUT_NO_LIMITS or FLAG_LAYOUT_IN_SCREEN or
                if (disableInput) 0 else FLAG_NOT_FOCUSABLE
        windowManager.updateViewLayout(binding.root, layoutParams)
        binding.btnToggle.setText(
            if (disableInput) R.string.disable_immerse else R.string.enable_immerse
        )
        binding.btnExit.isVisible = !disableInput
    }

    private fun removeFloatingView() {
        val binding = floatingBinding ?: return
        floatingBinding = null
        windowManager.removeView(binding.root)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        removeFloatingView()
        Toast.makeText(this, getString(R.string.onUnbind_tip), Toast.LENGTH_LONG).show()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        removeFloatingView()
        super.onDestroy()
    }
}
