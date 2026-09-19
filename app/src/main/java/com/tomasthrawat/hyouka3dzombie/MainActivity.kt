package com.tomasthrawat.hyouka3dzombie

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var game: ZombieGameView
    private lateinit var menu: LinearLayout
    private lateinit var title: TextView
    private lateinit var start: Button
    private lateinit var status: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.decorView.systemUiVisibility = 5894
        root = FrameLayout(this)
        game = ZombieGameView(this)
        root.addView(game, FrameLayout.LayoutParams(-1, -1))
        buildMenu()
        setContentView(root)
    }

    private fun buildMenu() {
        menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(40, 20, 40, 20)
            setBackgroundColor(Color.rgb(5, 7, 10))
        }
        title = TextView(this).apply {
            text = "HYOUKA\nNIGHTFALL"
            textSize = 38f
            gravity = android.view.Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        status = TextView(this).apply {
            text = "3D ZOMBIE SURVIVAL"
            textSize = 15f
            gravity = android.view.Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 18, 0, 28)
        }
        start = Button(this).apply {
            text = "START SURVIVAL"
            textSize = 18f
            setOnClickListener { startGame() }
        }
        menu.addView(title, LinearLayout.LayoutParams(-1, -2))
        menu.addView(status, LinearLayout.LayoutParams(-1, -2))
        menu.addView(start, LinearLayout.LayoutParams(-2, -2))
        root.addView(menu, FrameLayout.LayoutParams(-1, -1))
    }

    private fun startGame() {
        menu.visibility = View.GONE
        game.startNewGame()
    }

    fun showMenu(result: String) {
        runOnUiThread {
            status.text = result
            start.text = "PLAY AGAIN"
            menu.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        game.onPause()
    }

    override fun onResume() {
        super.onResume()
        game.onResume()
    }
}
