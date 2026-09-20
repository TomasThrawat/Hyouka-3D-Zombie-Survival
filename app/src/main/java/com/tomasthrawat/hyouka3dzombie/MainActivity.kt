package com.tomasthrawat.hyouka3dzombie

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    private var game by mutableStateOf(false)
    private var lastInteractionUptime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        applyFullscreen()

        setContent {
            ZombieGameScreen(
                playing = game,
                onStart = {
                    writeLog("MENU_START_ACCEPTED")
                    game = true
                },
                onExit = {
                    writeLog("GAME_MENU_ACCEPTED")
                    game = false
                },
                onLog = ::writeLog
            )
        }
    }

    private fun applyFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        writeLog("WINDOW_FOCUS hasFocus=" + hasFocus + " game=" + game)
        if (hasFocus) applyFullscreen()
    }

    override fun onUserInteraction() {
        lastInteractionUptime = android.os.SystemClock.uptimeMillis()
        writeLog("APP_ON_USER_INTERACTION uptimeMs=" + lastInteractionUptime + " game=" + game)
        super.onUserInteraction()
    }

    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        writeLog(
            "APP_TOP_RESUMED_CHANGED top=" + isTopResumedActivity +
                " game=" + game +
                " focus=" + window.decorView.hasWindowFocus()
        )
        super.onTopResumedActivityChanged(isTopResumedActivity)
    }

    override fun onUserLeaveHint() {
        val now = android.os.SystemClock.uptimeMillis()
        val sinceInteraction =
            if (lastInteractionUptime == 0L) -1L else now - lastInteractionUptime
        writeLog(
            "APP_ON_USER_LEAVE_HINT game=" + game +
                " sinceInteractionMs=" + sinceInteraction
        )
        super.onUserLeaveHint()
    }

    override fun onPause() {
        writeLog("APP_ON_PAUSE game=" + game)
        super.onPause()
    }

    override fun onStop() {
        writeLog("APP_ON_STOP game=" + game)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        applyFullscreen()
        writeLog("APP_ON_RESUME game=" + game)
    }

    private fun writeLog(message: String) {
        android.util.Log.i("Hyouka3D", message)
    }
}