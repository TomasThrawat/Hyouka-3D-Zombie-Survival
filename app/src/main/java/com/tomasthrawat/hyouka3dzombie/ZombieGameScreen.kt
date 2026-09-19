package com.tomasthrawat.hyouka3dzombie

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.createEnvironment
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberFillLightNode
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.delay

private const val PLAYER_MODEL = "https://cdn.3dassets.dev/assets/131/v1/model.glb"
private const val ZOMBIE_MODEL = "https://cdn.3dassets.dev/assets/29162/v1/model.glb"
private const val PROP_MODEL = "https://cdn.3dassets.dev/assets/27504/v1/model.glb"

@Composable
fun ZombieGameScreen(
    playing: Boolean,
    onStart: () -> Unit,
    onExit: () -> Unit,
    onLog: (String) -> Unit
) {
    LaunchedEffect(playing) { onLog("SCREEN_STATE playing=" + playing) }

    if (!playing) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Button(
                onClick = {
                    onLog("MENU_START_CLICKED uptimeMs=" + SystemClock.uptimeMillis())
                    onStart()
                },
                modifier = Modifier.padding(24.dp)
            ) { Text("START SURVIVAL") }
        }
        return
    }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val environment = rememberEnvironment(environmentLoader) {
        createEnvironment(environmentLoader)
    }
    val cameraManipulator = rememberCameraManipulator()
    val mainLightNode = rememberMainLightNode(engine) { intensity = 100_000f }
    val fillLightNode = rememberFillLightNode(engine) { intensity = 30_000f }

    val player = rememberModelInstance(modelLoader, PLAYER_MODEL)
    val zombie = rememberModelInstance(modelLoader, ZOMBIE_MODEL)
    val prop = rememberModelInstance(modelLoader, PROP_MODEL)

    var viewportWidth by remember { mutableIntStateOf(0) }
    var viewportHeight by remember { mutableIntStateOf(0) }
    var lastPollState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        onLog("GAME_RENDER_BEGIN")
        onLog("RENDER_MODE=sceneview_texture_surface")
        onLog("MODEL_URLS player=" + PLAYER_MODEL + " zombie=" + ZOMBIE_MODEL + " prop=" + PROP_MODEL)
        onLog("ENVIRONMENT_READY")
        onLog("MAIN_LIGHT_READY intensity=100000")
        onLog("FILL_LIGHT_READY intensity=30000")
        onLog("SURFACE_TYPE=TextureSurface")
        onLog("SCENEVIEW_RENDERING=true")
        onLog("SCENEVIEW_OPAQUE=true")
    }

    LaunchedEffect(Unit) {
        var tick = 0
        while (true) {
            delay(500)
            val state = "player=" + (player != null) + ",zombie=" + (zombie != null) + ",prop=" + (prop != null)
            if (state != lastPollState || tick % 4 == 0) {
                lastPollState = state
                onLog("MODEL_POLL tick=" + tick + " elapsedMs=" + ((tick + 1) * 500) + " " + state)
            }
            if (player != null && zombie != null && prop != null) {
                onLog("MODEL_POLL_COMPLETE tick=" + tick)
                break
            }
            if (tick++ >= 59) {
                onLog("MODEL_POLL_TIMEOUT elapsedMs=30000")
                break
            }
        }
    }

    LaunchedEffect(player, zombie, prop) {
        onLog("MODELS_STATE player=" + (player != null) + " zombie=" + (zombie != null) + " prop=" + (prop != null))
        if (player != null && zombie != null && prop != null) onLog("SCENE_READY_ALL_MODELS")
    }

    Box(
        Modifier.fillMaxSize().onSizeChanged {
            viewportWidth = it.width
            viewportHeight = it.height
            onLog("VIEWPORT_SIZE width=" + it.width + " height=" + it.height)
        }
    ) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            surfaceType = SurfaceType.TextureSurface,
            engine = engine,
            modelLoader = modelLoader,
            environment = environment,
            mainLightNode = mainLightNode,
            fillLightNode = fillLightNode,
            cameraManipulator = cameraManipulator,
            isOpaque = true,
            isRendering = true,
            autoCenterContent = true,
            autoFitContent = false
        ) {
            player?.let { ModelNode(modelInstance = it, scaleToUnits = 1.8f, autoAnimate = true) }
            zombie?.let { ModelNode(modelInstance = it, scaleToUnits = 1.8f, autoAnimate = true) }
            prop?.let { ModelNode(modelInstance = it, scaleToUnits = 1.4f, autoAnimate = false) }
        }

        if (player == null || zombie == null || prop == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("LOADING WORLD", color = Color.White)
            }
        }

        Button(
            onClick = { onLog("GAME_MENU_CLICKED"); onExit() },
            modifier = Modifier.align(Alignment.TopEnd).padding(18.dp)
        ) { Text("MENU") }
    }
}