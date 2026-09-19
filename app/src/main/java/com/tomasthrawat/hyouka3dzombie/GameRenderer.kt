package com.tomasthrawat.hyouka3dzombie

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class GameRenderer(private val context: Context, private val view: GLSurfaceView) : GLSurfaceView.Renderer {
    private data class Zombie(var x: Float, var z: Float, var hp: Int, var speed: Float, var phase: Float)
    private data class Bullet(var x: Float, var z: Float, var vx: Float, var vz: Float, var life: Float)

    private lateinit var program: Shader
    private lateinit var mesh: CubeMesh
    private val zombies = ArrayList<Zombie>()
    private val bullets = ArrayList<Bullet>()
    private var playing = false
    private var won = false
    private var health = 100f
    private var kills = 0
    private var supplies = 0
    private var wave = 1
    private var spawnTimer = 0f
    private var shotCooldown = 0f
    private var last = 0L
    private var moveX = 0f
    private var moveY = 0f
    private var px = 0f
    private var pz = 0f
    private var angle = 0f

    private val projection = FloatArray(16)
    private val viewM = FloatArray(16)
    private val model = FloatArray(16)
    private val mvp = FloatArray(16)

    override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, cfg: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(0.025f, 0.03f, 0.04f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        program = Shader()
        mesh = CubeMesh()
        last = System.nanoTime()
    }

    override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
        Matrix.perspectiveM(projection, 0, 62f, w.toFloat() / max(1, h), 0.1f, 120f)
    }

    override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) {
        val now = System.nanoTime()
        val dt = minOf(0.05f, (now - last) / 1_000_000_000f)
        last = now
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (!playing) {
            drawWorld(0f)
            return
        }
        update(dt)
        drawWorld(dt)
    }

    fun startGame() {
        view.queueEvent {
            playing = true
            won = false
            health = 100f
            kills = 0
            supplies = 0
            wave = 1
            spawnTimer = 0f
            shotCooldown = 0f
            px = 0f
            pz = 0f
            angle = 0f
            zombies.clear()
            bullets.clear()
            repeat(3) { spawnZombie() }
        }
    }

    fun setMove(x: Float, y: Float) {
        view.queueEvent { moveX = x; moveY = y }
    }

    fun fire() {
        view.queueEvent {
            if (!playing || shotCooldown > 0f) return@queueEvent
            val dx = sin(angle)
            val dz = -cos(angle)
            bullets.add(Bullet(px + dx * 0.8f, pz + dz * 0.8f, dx * 22f, dz * 22f, 1.2f))
            shotCooldown = 0.22f
        }
    }

    private fun update(dt: Float) {
        shotCooldown = max(0f, shotCooldown - dt)
        val speed = 5.0f
        px += moveX * speed * dt
        pz += -moveY * speed * dt
        px = px.coerceIn(-18f, 18f)
        pz = pz.coerceIn(-18f, 18f)
        if (moveX * moveX + moveY * moveY > 0.08f) angle = kotlin.math.atan2(moveX.toDouble(), (-moveY).toDouble()).toFloat()

        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            repeat(minOf(2, 1 + wave / 3)) { spawnZombie() }
            spawnTimer = max(0.8f, 3.2f - wave * 0.12f)
        }

        val zi = zombies.iterator()
        while (zi.hasNext()) {
            val z = zi.next()
            val dx = px - z.x
            val dz = pz - z.z
            val d = sqrt(dx * dx + dz * dz)
            if (d > 0.9f) {
                z.x += dx / d * z.speed * dt
                z.z += dz / d * z.speed * dt
            } else {
                health -= 18f * dt
            }
        }

        val bi = bullets.iterator()
        while (bi.hasNext()) {
            val b = bi.next()
            b.x += b.vx * dt
            b.z += b.vz * dt
            b.life -= dt
            var hit = false
            val zi2 = zombies.iterator()
            while (zi2.hasNext()) {
                val z = zi2.next()
                val dx = z.x - b.x
                val dz = z.z - b.z
                if (dx * dx + dz * dz < 0.7f) {
                    z.hp--
                    hit = true
                    if (z.hp <= 0) {
                        zi2.remove()
                        kills++
                        if (kills % 5 == 0) wave++
                    }
                    break
                }
            }
            if (hit || b.life <= 0f || abs(b.x) > 25f || abs(b.z) > 25f) bi.remove()
        }

        if (supplies < 3) {
            val target = when (supplies) {
                0 -> floatArrayOf(-11f, -9f)
                1 -> floatArrayOf(10f, -8f)
                else -> floatArrayOf(8f, 11f)
            }
            val dx = px - target[0]
            val dz = pz - target[1]
            if (dx * dx + dz * dz < 2.0f) supplies++
        }

        if (health <= 0f) {
            playing = false
            (context as? MainActivity)?.showMenu("GAME OVER  •  KILLS $kills")
        } else if (supplies >= 3 && px * px + pz * pz < 5f) {
            playing = false
            won = true
            (context as? MainActivity)?.showMenu("SAFEHOUSE REACHED  •  KILLS $kills")
        }
    }

    private fun spawnZombie() {
        val edge = Random.nextInt(4)
        val s = Random.nextFloat() * 12f + 10f
        val (x, z) = when (edge) {
            0 -> Pair(-s, Random.nextFloat() * 24f - 12f)
            1 -> Pair(s, Random.nextFloat() * 24f - 12f)
            2 -> Pair(Random.nextFloat() * 24f - 12f, -s)
            else -> Pair(Random.nextFloat() * 24f - 12f, s)
        }
        zombies.add(Zombie(x, z, 2 + wave / 4, 1.0f + wave * 0.035f, Random.nextFloat() * 6f))
    }

    private fun drawWorld(dt: Float) {
        val camX = px - sin(angle) * 8f
        val camY = 6.2f
        val camZ = pz + cos(angle) * 8f
        Matrix.setLookAtM(viewM, 0, camX, camY, camZ, px, 0.8f, pz, 0f, 1f, 0f)

        drawCube(0f, -0.65f, 0f, 42f, 0.5f, 42f, 0.08f, 0.11f, 0.13f)
        for (x in -18..18 step 6) for (z in -18..18 step 6) {
            drawCube(x.toFloat(), -0.36f, z.toFloat(), 0.08f, 0.08f, 0.08f, 0.13f, 0.16f, 0.18f)
        }

        // Safehouse
        drawCube(0f, 0.4f, 0f, 3.5f, 0.8f, 3.5f, 0.12f, 0.36f, 0.2f)

        val targets = arrayOf(floatArrayOf(-11f, -9f), floatArrayOf(10f, -8f), floatArrayOf(8f, 11f))
        for (i in 0 until 3) if (i >= supplies) {
            val t = targets[i]
            drawCube(t[0], 0.25f, t[1], 0.9f, 0.5f, 0.9f, 0.8f, 0.62f, 0.12f)
        }

        for (z in zombies) {
            drawCube(z.x, 0.7f, z.z, 0.55f, 1.15f, 0.4f, 0.35f, 0.65f, 0.35f)
            drawCube(z.x, 1.75f, z.z, 0.48f, 0.48f, 0.42f, 0.55f, 0.72f, 0.4f)
        }

        for (b in bullets) drawCube(b.x, 0.9f, b.z, 0.12f, 0.12f, 0.12f, 1f, 0.75f, 0.15f)

        drawCube(px, 0.85f, pz, 0.42f, 0.9f, 0.35f, 0.18f, 0.45f, 0.85f)
        drawCube(px, 1.75f, pz, 0.35f, 0.35f, 0.35f, 0.82f, 0.72f, 0.58f)
    }

    private fun drawCube(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float, r: Float, g: Float, b: Float) {
        Matrix.setIdentityM(model, 0)
        Matrix.translateM(model, 0, x, y, z)
        Matrix.scaleM(model, 0, sx, sy, sz)
        Matrix.multiplyMM(mvp, 0, viewM, 0, model, 0)
        Matrix.multiplyMM(mvp, 0, projection, 0, mvp, 0)
        program.use(mvp, r, g, b)
        mesh.draw(program)
    }

    private fun abs(v: Float) = kotlin.math.abs(v)
}

private class Shader {
    private val program: Int
    private val pos: Int
    private val color: Int
    private val mvp: Int

    init {
        val vs = """
            attribute vec3 aPosition;
            uniform mat4 uMvp;
            void main(){ gl_Position=uMvp*vec4(aPosition,1.0); }
        """.trimIndent()
        val fs = """
            precision mediump float;
            uniform vec3 uColor;
            void main(){ gl_FragColor=vec4(uColor,1.0); }
        """.trimIndent()
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, compile(GLES20.GL_VERTEX_SHADER, vs))
        GLES20.glAttachShader(program, compile(GLES20.GL_FRAGMENT_SHADER, fs))
        GLES20.glLinkProgram(program)
        pos = GLES20.glGetAttribLocation(program, "aPosition")
        color = GLES20.glGetUniformLocation(program, "uColor")
        mvp = GLES20.glGetUniformLocation(program, "uMvp")
    }

    fun use(matrix: FloatArray, r: Float, g: Float, b: Float) {
        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(mvp, 1, false, matrix, 0)
        GLES20.glUniform3f(color, r, g, b)
    }

    fun position() = pos

    private fun compile(type: Int, src: String): Int {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, src)
        GLES20.glCompileShader(s)
        return s
    }
}

private class CubeMesh {
    private val vertices: FloatBuffer
    private val indices: ShortBuffer
    private val data = floatArrayOf(
        -1f,-1f,-1f, 1f,-1f,-1f, 1f,1f,-1f, -1f,1f,-1f,
        -1f,-1f,1f, 1f,-1f,1f, 1f,1f,1f, -1f,1f,1f
    )
    private val idx = shortArrayOf(
        0,1,2, 0,2,3, 4,6,5, 4,7,6, 0,4,5, 0,5,1,
        3,2,6, 3,6,7, 1,5,6, 1,6,2, 0,3,7, 0,7,4
    )

    init {
        vertices = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertices.put(data).position(0)
        indices = ByteBuffer.allocateDirect(idx.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
        indices.put(idx).position(0)
    }

    fun draw(shader: Shader) {
        vertices.position(0)
        GLES20.glEnableVertexAttribArray(shader.position())
        GLES20.glVertexAttribPointer(shader.position(), 3, GLES20.GL_FLOAT, false, 12, vertices)
        indices.position(0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, idx.size, GLES20.GL_UNSIGNED_SHORT, indices)
        GLES20.glDisableVertexAttribArray(shader.position())
    }
}
