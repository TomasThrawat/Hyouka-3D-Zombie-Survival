package com.tomasthrawat.hyouka3dzombie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.sceneview.SceneView
import io.github.sceneview.createEnvironment
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

private const val PLAYER_MODEL = "https://cdn.3dassets.dev/assets/131/v1/model.glb"
private const val ZOMBIE_MODEL = "https://cdn.3dassets.dev/assets/29162/v1/model.glb"
private const val PROP_MODEL = "https://cdn.3dassets.dev/assets/27504/v1/model.glb"

@Composable
fun ZombieGameScreen(playing: Boolean, onStart: () -> Unit, onExit: () -> Unit, onLog: (String) -> Unit) {
    LaunchedEffect(playing) { onLog("SCREEN_STATE playing=" + playing) }

    if (!playing) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Button(onClick = { onLog("MENU_START_CLICKED"); onStart() }, modifier = Modifier.padding(24.dp)) {
                Text("START SURVIVAL")
            }
        }
        return
    }

    LaunchedEffect(Unit) {
        onLog("GAME_RENDER_BEGIN")
        onLog("MODEL_URLS player=" + PLAYER_MODEL + " zombie=" + ZOMBIE_MODEL + " prop=" + PROP_MODEL)
    }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val environment = rememberEnvironment(environmentLoader) { createEnvironment(environmentLoader) }
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 3.5f, z = 8f)
        lookAt(Position(x = 0f, y = 1f, z = 0f))
    }
    val mainLightNode = rememberMainLightNode(engine) { intensity = 300_000f }

    LaunchedEffect(Unit) { onLog("ENGINE_AND_SCENE_READY camera=0,3.5,8 light=300000") }

    val player = rememberModelInstance(modelLoader = modelLoader, fileLocation = PLAYER_MODEL)
    val zombie = rememberModelInstance(modelLoader = modelLoader, fileLocation = ZOMBIE_MODEL)
    val prop = rememberModelInstance(modelLoader = modelLoader, fileLocation = PROP_MODEL)

    LaunchedEffect(player, zombie, prop) {
        onLog("MODELS_STATE player=" + (player != null) + " zombie=" + (zombie != null) + " prop=" + (prop != null))
    }

    var sceneReady by remember { mutableStateOf(false) }
    LaunchedEffect(player, zombie, prop) {
        if (player != null && zombie != null && prop != null) {
            sceneReady = true
            onLog("SCENE_READY_ALL_MODELS")
        }
    }

    Box(Modifier.fillMaxSize()) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            environmentLoader = environmentLoader,
            environment = environment,
            cameraNode = cameraNode,
            cameraManipulator = null,
            mainLightNode = mainLightNode,
            autoCenterContent = false,
            autoFitContent = false
        ) {
            player?.let { ModelNode(modelInstance = it, scaleToUnits = 1.8f, position = Position(x = 0f, y = 0f, z = 0f), autoAnimate = true) }
            zombie?.let { ModelNode(modelInstance = it, scaleToUnits = 1.8f, position = Position(x = 3f, y = 0f, z = -4f), autoAnimate = true) }
            prop?.let { ModelNode(modelInstance = it, scaleToUnits = 1.4f, position = Position(x = -3f, y = 0f, z = -3f), autoAnimate = false) }
        }

        if (!sceneReady) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("LOADING WORLD", color = Color.White)
            }
        }

        Button(onClick = { onLog("GAME_MENU_CLICKED"); onExit() }, modifier = Modifier.align(Alignment.TopEnd).padding(18.dp)) {
            Text("MENU")
        }
    }
}