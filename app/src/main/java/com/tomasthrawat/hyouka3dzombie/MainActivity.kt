package com.tomasthrawat.hyouka3dzombie

import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
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
        writeLog("DIAGNOSTIC_FILE uri=" + diagnosticUri)
        writeLog(
            "APP_ON_CREATE sdk=" + Build.VERSION.SDK_INT +
                " release=" + Build.VERSION.RELEASE +
                " device=" + Build.MODEL +
                " manufacturer=" + Build.MANUFACTURER +
                " brand=" + Build.BRAND +
                " hardware=" + Build.HARDWARE +
                " product=" + Build.PRODUCT
        )
        writeLog("DEVICE_ABI supported=" + Build.SUPPORTED_ABIS.joinToString(","))
        writeSystemDiagnostics()
        writeNetworkDiagnostics()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            writeLog(
                "UNCAUGHT_EXCEPTION thread=" + thread.name +
                    " type=" + throwable::class.java.name + "\n" +
                    Log.getStackTraceString(throwable)
            )
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

    override fun onResume() {
        super.onResume()
        writeLog("APP_ON_RESUME")
        writeSystemDiagnostics()
        writeNetworkDiagnostics()
    }

    override fun onPause() {
        writeLog("APP_ON_PAUSE isFinishing=" + isFinishing)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        writeLog(
            "WINDOW_FOCUS hasFocus=" + hasFocus +
                " isFinishing=" + isFinishing +
                " visibility=" + window.decorView.visibility
        )
    }

    override fun onDestroy() {
        writeLog("APP_ON_DESTROY isFinishing=" + isFinishing)
        super.onDestroy()
    }

    private fun writeSystemDiagnostics() {
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            val glEs = am.deviceConfigurationInfo.glEsVersion
            writeLog(
                "SYSTEM_DIAGNOSTICS glEs=" + glEs +
                    " ramTotal=" + info.totalMem +
                    " ramAvailable=" + info.availMem +
                    " lowMemory=" + info.lowMemory +
                    " threshold=" + info.threshold
            )
            writeLog(
                "DISPLAY_DIAGNOSTICS size=" + resources.displayMetrics.widthPixels + "x" +
                    resources.displayMetrics.heightPixels +
                    " density=" + resources.displayMetrics.density +
                    " densityDpi=" + resources.displayMetrics.densityDpi
            )
        } catch (t: Throwable) {
            writeLog("SYSTEM_DIAGNOSTICS_FAILED type=" + t::class.java.name + "\n" + Log.getStackTraceString(t))
        }
    }

    private fun writeNetworkDiagnostics() {
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork
            val caps = network?.let(cm::getNetworkCapabilities)
            val transports = buildList {
                if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) add("WIFI")
                if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) add("CELLULAR")
                if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) add("ETHERNET")
                if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) add("VPN")
            }
            writeLog(
                "NETWORK_DIAGNOSTICS connected=" + (caps != null) +
                    " validated=" + (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) +
                    " internet=" + (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) +
                    " transports=" + transports.joinToString(",")
            )
        } catch (t: Throwable) {
            writeLog("NETWORK_DIAGNOSTICS_FAILED type=" + t::class.java.name + "\n" + Log.getStackTraceString(t))
        }
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

    @Synchronized
    fun writeLog(message: String) {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val line = stamp + " | " + message + "\n"
        Log.i(TAG, message)
        try {
            diagnosticUri?.let { uri ->
                contentResolver.openOutputStream(uri, "wa")?.use {
                    it.write(line.toByteArray(Charsets.UTF_8))
                    it.flush()
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "DIAGNOSTIC_FILE_WRITE_FAILED", t)
        }
    }
}