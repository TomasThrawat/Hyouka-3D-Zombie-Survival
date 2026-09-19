package com.tomasthrawat.hyouka3dzombie

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "Hyouka3D"

class MainActivity : ComponentActivity() {
    private var game by mutableStateOf(false)
    private var diagnosticUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diagnosticUri = createDiagnosticFile()
        writeLog("APP_ON_CREATE sdk=${android.os.Build.VERSION.SDK_INT} device=${android.os.Build.MODEL} manufacturer=${android.os.Build.MANUFACTURER}")
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            writeLog("UNCAUGHT_EXCEPTION thread=${thread.name}\n${Log.getStackTraceString(throwable)}")
            android.os.Process.killProcess(android.os.Process.myPid())
        }
        window.decorView.systemUiVisibility = 5894
        window.addFlags(Window.FEATURE_NO_TITLE)
        writeLog("WINDOW_CONFIG immersive_flags=5894")
        setContent {
            ZombieGameScreen(
                playing = game,
                onStart = {
                    if (!game) {
                        writeLog("MENU_START_ACCEPTED")
                        game = true
                    } else {
                        writeLog("MENU_START_IGNORED_ALREADY_PLAYING")
                    }
                },
                onExit = {
                    if (game) {
                        writeLog("GAME_MENU_ACCEPTED")
                        game = false
                    }
                },
                onLog = ::writeLog
            )
        }
    }

    override fun onResume() { super.onResume(); writeLog("APP_ON_RESUME") }

    override fun onPause() {
        writeLog("APP_ON_PAUSE isFinishing=$isFinishing")
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        writeLog("WINDOW_FOCUS hasFocus=$hasFocus isFinishing=$isFinishing")
    }

    override fun onDestroy() {
        writeLog("APP_ON_DESTROY isFinishing=$isFinishing")
        super.onDestroy()
    }

    private fun createDiagnosticFile(): Uri? {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "Hyouka3D-Zombie-Diagnostics.log")
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        } catch (t: Throwable) {
            Log.e(TAG, "DIAGNOSTIC_FILE_CREATE_FAILED", t)
            null
        }
    }

    fun writeLog(message: String) {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val line = "$stamp | $message\n"
        Log.i(TAG, message)
        try {
            diagnosticUri?.let { uri ->
                contentResolver.openOutputStream(uri, "wa")?.use {
                    it.write(line.toByteArray(Charsets.UTF_8))
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "DIAGNOSTIC_FILE_WRITE_FAILED", t)
        }
    }
}