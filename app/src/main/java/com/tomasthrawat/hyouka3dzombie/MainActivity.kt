package com.tomasthrawat.hyouka3dzombie

import android.app.Activity
import android.os.Bundle
import android.view.Window
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : Activity() {
    private var game by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.decorView.systemUiVisibility = 5894
        setContent {
            ZombieGameScreen(
                playing = game,
                onStart = { game = true },
                onExit = { game = false }
            )
        }
    }
}
