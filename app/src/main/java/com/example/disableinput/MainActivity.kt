package com.example.disableinput

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import com.example.disableinput.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"

    private val clazz = DisableInputAccessibilityService::class.java
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.btnGoEnable?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        val available = checkAccessibilityAvailable()
        binding?.text?.text = getString(if (available) R.string.text_main else R.string.please_enable_accessibility_service)
        if (!available) {
            Toast.makeText(this, getString(R.string.please_enable_accessibility_service), Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAccessibilityAvailable(): Boolean {
        var accessibilityEnabled = false    // 判断设备的无障碍功能是否可用
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            ) == 1
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled) {
            // 获取启用的无障碍服务
            val settingValue: String? = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                // 遍历判断是否包含我们的服务
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(
                            "${packageName}/${clazz.canonicalName}",
                            ignoreCase = true
                        )
                    ) {
                        return true
                    }
                }
            }
        }
        return false
    }
}