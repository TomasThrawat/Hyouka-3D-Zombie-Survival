package com.tomasthrawat.hyouka3dzombie

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import kotlin.math.max
import kotlin.math.min

class ZombieGameView(context: Context) : GLSurfaceView(context) {
    private val renderer = GameRenderer(context, this)
    private var joystickPointer = -1
    private var firePointer = -1
    private var baseX = 0f
    private var baseY = 0f
    private var moveX = 0f
    private var moveY = 0f

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun startNewGame() {
        renderer.startGame()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val w = width.toFloat()
        val h = height.toFloat()
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val i = e.actionIndex
                val x = e.getX(i)
                val y = e.getY(i)
                if (x < w * 0.48f && joystickPointer == -1) {
                    joystickPointer = e.getPointerId(i)
                    baseX = x
                    baseY = y
                } else if (x > w * 0.70f && firePointer == -1) {
                    firePointer = e.getPointerId(i)
                    renderer.fire()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (joystickPointer != -1) {
                    val i = e.findPointerIndex(joystickPointer)
                    if (i >= 0) {
                        val dx = e.getX(i) - baseX
                        val dy = e.getY(i) - baseY
                        val len = max(1f, kotlin.math.sqrt(dx * dx + dy * dy))
                        val radius = min(w, h) * 0.14f
                        val scale = min(1f, radius / len)
                        moveX = dx * scale / radius
                        moveY = dy * scale / radius
                        renderer.setMove(moveX, moveY)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                val id = e.getPointerId(e.actionIndex)
                if (id == joystickPointer) {
                    joystickPointer = -1
                    moveX = 0f
                    moveY = 0f
                    renderer.setMove(0f, 0f)
                }
                if (id == firePointer) firePointer = -1
            }
        }
        return true
    }
}
