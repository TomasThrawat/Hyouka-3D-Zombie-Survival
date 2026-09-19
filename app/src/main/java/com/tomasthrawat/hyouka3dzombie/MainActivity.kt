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
import android.view.WindowManager
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
    private var lastFocus: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diagnosticUri = createDiagnosticFile()
        writeLog("DIAGNOSTIC_FILE uri=" + diagnosticUri)
        writeLog("APP_ON_CREATE sdk=" + Build.VERSION.SDK_INT + " release=" + Build.VERSION.RELEASE + " device=" + Build.MODEL + " manufacturer=" + Build.MANUFACTURER + " product=" + Build.PRODUCT)
        writeLog("DEVICE_ABI supported=" + Build.SUPPORTED_ABIS.joinToString(","))
        writeSystemDiagnostics()
        writeNetworkDiagnostics()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            writeLog("UNCAUGHT_EXCEPTION thread=" + thread.name + " type=" + throwable.javaClass.name + "\n" + Log.getStackTraceString(throwable))
            android.os.Process.killProcess(android.os.Process.myPid())
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = 5894
        writeLog("WINDOW_CONFIG immersive_flags=" + window.decorView.systemUiVisibility + " flags=" + window.attributes.flags + " keepScreenOn=true")
        setContent {
            ZombieGameScreen(
                playing = game,
                onStart = { if (!game) { writeLog("MENU_START_ACCEPTED"); game = true } },
                onExit = { if (game) { writeLog("GAME_MENU_ACCEPTED"); game = false } },
                onLog = ::writeLog
            )
        }
    }

    override fun onStart() { super.onStart(); writeLog("APP_ON_START isFinishing=" + isFinishing + " changing=" + isChangingConfigurations); writeWindowDiagnostics("ON_START") }
    override fun onResume() { super.onResume(); writeLog("APP_ON_RESUME isFinishing=" + isFinishing + " game=" + game); writeWindowDiagnostics("ON_RESUME"); writeSystemDiagnostics() }
    override fun onPause() { writeLog("APP_ON_PAUSE isFinishing=" + isFinishing + " changing=" + isChangingConfigurations + " game=" + game); writeWindowDiagnostics("ON_PAUSE"); super.onPause() }
    override fun onStop() { writeLog("APP_ON_STOP isFinishing=" + isFinishing + " changing=" + isChangingConfigurations + " game=" + game); writeWindowDiagnostics("ON_STOP"); super.onStop() }
    override fun onDestroy() { writeLog("APP_ON_DESTROY isFinishing=" + isFinishing + " changing=" + isChangingConfigurations + " game=" + game); super.onDestroy() }
    override fun onUserLeaveHint() { writeLog("APP_ON_USER_LEAVE_HINT game=" + game); super.onUserLeaveHint() }
    override fun onTrimMemory(level: Int) { writeLog("APP_ON_TRIM_MEMORY level=" + level + " game=" + game); writeSystemDiagnostics(); super.onTrimMemory(level) }
    override fun onSaveInstanceState(outState: Bundle) { writeLog("APP_ON_SAVE_INSTANCE_STATE game=" + game); super.onSaveInstanceState(outState) }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val changed = lastFocus == null || lastFocus != hasFocus
        lastFocus = hasFocus
        writeLog("WINDOW_FOCUS hasFocus=" + hasFocus + " changed=" + changed + " game=" + game)
        if (changed) writeWindowDiagnostics("FOCUS_CHANGED")
    }

    override fun onWindowAttributesChanged(params: WindowManager.LayoutParams) {
        super.onWindowAttributesChanged(params)
        writeLog("WINDOW_ATTRIBUTES_CHANGED flags=" + params.flags + " format=" + params.format + " alpha=" + params.alpha + " dim=" + params.dimAmount + " size=" + params.width + "x" + params.height)
    }

    private fun writeWindowDiagnostics(reason: String) {
        try {
            val decor = window.decorView
            val root = decor.rootView
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mem = ActivityManager.MemoryInfo().also(am::getMemoryInfo)
            val proc = ActivityManager.RunningAppProcessInfo().also(ActivityManager::getMyMemoryState)
            writeLog("WINDOW_DIAGNOSTICS reason=" + reason + " focus=" + decor.hasWindowFocus() + " attached=" + decor.isAttachedToWindow + " shown=" + decor.isShown + " size=" + decor.width + "x" + decor.height + " root=" + root.width + "x" + root.height + " ui=" + decor.systemUiVisibility + " flags=" + window.attributes.flags + " importance=" + proc.importance + " memAvail=" + mem.availMem + " low=" + mem.lowMemory)
        } catch (t: Throwable) { writeLog("WINDOW_DIAGNOSTICS_FAILED reason=" + reason + " type=" + t.javaClass.name + "\n" + Log.getStackTraceString(t)) }
    }

    private fun writeSystemDiagnostics() {
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = ActivityManager.MemoryInfo().also(am::getMemoryInfo)
            writeLog("SYSTEM_DIAGNOSTICS glEs=" + am.deviceConfigurationInfo.glEsVersion + " ramTotal=" + info.totalMem + " ramAvailable=" + info.availMem + " lowMemory=" + info.lowMemory + " threshold=" + info.threshold + " pssKb=" + android.os.Debug.getPss() + " nativeHeapAllocated=" + android.os.Debug.getNativeHeapAllocatedSize() + " nativeHeapFree=" + android.os.Debug.getNativeHeapFreeSize())
            writeLog("DISPLAY_DIAGNOSTICS size=" + resources.displayMetrics.widthPixels + "x" + resources.displayMetrics.heightPixels + " density=" + resources.displayMetrics.density + " dpi=" + resources.displayMetrics.densityDpi)
        } catch (t: Throwable) { writeLog("SYSTEM_DIAGNOSTICS_FAILED type=" + t.javaClass.name + "\n" + Log.getStackTraceString(t)) }
    }

    private fun writeNetworkDiagnostics() {
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val n = cm.activeNetwork
            val c = n?.let(cm::getNetworkCapabilities)
            writeLog("NETWORK_DIAGNOSTICS connected=" + (c != null) + " validated=" + c?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) + " internet=" + c?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) + " wifi=" + c?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) + " network=" + n)
        } catch (t: Throwable) { writeLog("NETWORK_DIAGNOSTICS_FAILED type=" + t.javaClass.name + "\n" + Log.getStackTraceString(t)) }
    }

    private fun createDiagnosticFile(): Uri? = try {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "Hyouka3D-Zombie-Diagnostics.log")
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
    } catch (t: Throwable) { Log.e(TAG, "DIAGNOSTIC_FILE_CREATE_FAILED", t); null }

    @Synchronized fun writeLog(message: String) {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val line = stamp + " | " + message + "\n"
        Log.i(TAG, message)
        try { diagnosticUri?.let { uri -> contentResolver.openOutputStream(uri, "wa")?.use { it.write(line.toByteArray(Charsets.UTF_8)); it.flush() } } }
        catch (t: Throwable) { Log.e(TAG, "DIAGNOSTIC_FILE_WRITE_FAILED", t) }
    }
}