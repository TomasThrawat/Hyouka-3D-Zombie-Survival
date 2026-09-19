package com.tomasthrawat.hyouka3dzombie

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private const val TAG = "Hyouka3D"

class MainActivity : ComponentActivity() {
    private var game by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "APP_ON_CREATE sdk=${android.os.Build.VERSION.SDK_INT} device=${android.os.Build.MODEL} manufacturer=${android.os.Build.MANUFACTURER}")
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "UNCAUGHT_EXCEPTION thread=${thread.name}", throwable)
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            if (previous != null && previous !== Thread.getDefaultUncaughtExceptionHandler()) {
                previous.uncaughtException(thread, throwable)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }

        window.decorView.systemUiVisibility = 5894
        Log.i(TAG, "WINDOW_CONFIG landscape immersive flags=5894")
        setContent {
            Log.d(TAG, "COMPOSE_CONTENT game=$game")
            ZombieGameScreen(
                playing = game,
                onStart = {
                    Log.i(TAG, "MENU_START clicked")
                    game = true
                },
                onExit = {
                    Log.i(TAG, "GAME_MENU clicked")
                    game = false
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "APP_ON_RESUME")
    }

    override fun onPause() {
        Log.i(TAG, "APP_ON_PAUSE")
        super.onPause()
    }

    override fun onDestroy() {
        Log.i(TAG, "APP_ON_DESTROY")
        super.onDestroy()
    }
}
