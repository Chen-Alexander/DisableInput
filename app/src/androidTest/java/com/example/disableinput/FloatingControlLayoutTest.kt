package com.example.disableinput

import android.graphics.drawable.GradientDrawable
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.disableinput.databinding.LayoutFloatingControlViewBinding
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FloatingControlLayoutTest {
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var binding: LayoutFloatingControlViewBinding

    @Before
    fun attachControls() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            binding = LayoutFloatingControlViewBinding.inflate(LayoutInflater.from(activity))
            activity.setContentView(binding.root)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @After
    fun closeActivity() {
        scenario.close()
    }

    @Test
    fun thirdOutsideTouchRevealsBothButtonsForTwoSeconds() {
        assertAppearance(revealed = false)
        outsideTouches(0, 100)
        assertAppearance(revealed = false)
        outsideTouches(200)
        assertAppearance(revealed = true)

        Thread.sleep(1_600)
        assertAppearance(revealed = true)
        Thread.sleep(650)
        assertAppearance(revealed = false)
    }

    @Test
    fun gapOverOneSecondStartsANewSequence() {
        outsideTouches(0, 1_001, 1_400)
        assertAppearance(revealed = false)
    }

    @Test
    fun gapOfExactlyOneSecondIsAllowed() {
        outsideTouches(0, 1_000, 2_000)
        assertAppearance(revealed = true)
    }

    @Test
    fun anotherTripleTouchRestartsTheTwoSecondTimer() {
        outsideTouches(0, 100, 200)
        Thread.sleep(1_200)
        outsideTouches(0, 100, 200)
        Thread.sleep(1_100)
        assertAppearance(revealed = true)
        Thread.sleep(1_100)
        assertAppearance(revealed = false)
    }

    @Test
    fun buttonPressAndMovesDoNotRevealOrPreventTheClick() {
        var clicks = 0
        scenario.onActivity {
            binding.btnToggle.setOnClickListener { clicks++ }
            val button = binding.btnToggle
            val x = button.x + button.width / 2f
            val y = button.y + button.height / 2f
            val time = SystemClock.uptimeMillis()
            dispatchTouch(MotionEvent.ACTION_DOWN, time, x, y)
            repeat(5) { dispatchTouch(MotionEvent.ACTION_MOVE, time + it + 1, x, y) }
            assertEquals(
                ContextCompat.getColor(it, R.color.floating_control_text),
                button.currentTextColor
            )
            dispatchTouch(MotionEvent.ACTION_UP, time + 10, x, y)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertEquals(1, clicks)
        assertAppearance(revealed = false)
    }

    @Test
    fun detachingResetsTheSequenceAndTheAppearance() {
        outsideTouches(0, 100)
        scenario.onActivity { activity ->
            activity.setContentView(FrameLayout(activity))
            activity.setContentView(binding.root)
        }
        outsideTouches(200)
        assertAppearance(revealed = false)
        outsideTouches(300, 400)
        assertAppearance(revealed = true)

        scenario.onActivity { activity ->
            activity.setContentView(FrameLayout(activity))
        }
        assertAppearance(revealed = false)
    }

    private fun outsideTouches(vararg offsets: Long) {
        scenario.onActivity {
            val start = SystemClock.uptimeMillis()
            offsets.forEach { offset ->
                dispatchTouch(MotionEvent.ACTION_OUTSIDE, start + offset)
            }
        }
    }

    private fun dispatchTouch(action: Int, time: Long, x: Float = -1f, y: Float = -1f) {
        val event = MotionEvent.obtain(time, time, action, x, y, 0)
        try {
            binding.root.dispatchTouchEvent(event)
        } finally {
            event.recycle()
        }
    }

    private fun assertAppearance(revealed: Boolean) {
        scenario.onActivity { activity ->
            val textColor = ContextCompat.getColor(
                activity,
                if (revealed) R.color.floating_control_text_visible else R.color.floating_control_text
            )
            val backgroundColor = ContextCompat.getColor(
                activity,
                if (revealed) {
                    R.color.floating_control_background_visible
                } else {
                    R.color.floating_control_background
                }
            )
            for (button in listOf(binding.btnExit, binding.btnToggle)) {
                assertEquals(textColor, button.currentTextColor)
                val background = button.background.current as GradientDrawable
                assertEquals(backgroundColor, background.color?.defaultColor)
            }
        }
    }
}
