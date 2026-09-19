package com.tomasthrawat.hyouka3dzombie

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.sceneview.SceneView
import io.github.sceneview.createEnvironment
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
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
    LaunchedEffect(playing) { onLog("SCREEN_STATE playing=$playing") }

    if (!playing) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Button(onClick = onStart, modifier = Modifier.padding(24.dp)) { Text("START SURVIVAL") }
        }
        return
    }

    onLog("GAME_RENDER_BEGIN")
    onLog("MODEL_URLS player=$PLAYER_MODEL zombie=$ZOMBIE_MODEL prop=$PROP_MODEL")

    val engine = rememberEngine()
    onLog("ENGINE_CREATED")
    val modelLoader = rememberModelLoader(engine)
    onLog("MODEL_LOADER_CREATED")
    val environmentLoader = rememberEnvironmentLoader(engine)
    onLog("ENV_LOADER_CREATED")

    val environment = rememberEnvironment(environmentLoader) {
        onLog("ENVIRONMENT_CREATE_BEGIN")
        try {
            val env = try {
                environmentLoader.createHDREnvironment("environments/neutral/neutral_ibl.ktx")
            } catch (t: Throwable) {
                onLog("HDR_ENV_EXCEPTION ${Log.getStackTraceString(t)}")
                null
            } ?: createEnvironment(environmentLoader)
            onLog("ENVIRONMENT_CREATE_RESULT success=${env != null}")
            env
        } catch (t: Throwable) {
            onLog("ENVIRONMENT_CREATE_FAILED ${Log.getStackTraceString(t)}")
            null
        }
    }

    val cameraManipulator = rememberCameraManipulator()
    onLog("CAMERA_MANIPULATOR_CREATED")
    val mainLightNode = rememberMainLightNode(engine) { intensity = 300_000f }
    onLog("MAIN_LIGHT_CREATED")

    Box(Modifier.fillMaxSize()) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            environmentLoader = environmentLoader,
            environment = environment,
            cameraManipulator = cameraManipulator,
            mainLightNode = mainLightNode
        ) {
            onLog("SCENEVIEW_CONTENT_ENTERED")

            fun loadModel(name: String, url: String, scale: Float, animate: Boolean): ModelNode? {
                onLog("${name}_LOAD_BEGIN")
                return try {
                    rememberModelInstance(modelLoader = modelLoader, fileLocation = url)?.also {
                        onLog("${name}_LOAD_RESULT success=true")
                    }?.let { ModelNode(modelInstance = it, scaleToUnits = scale, autoAnimate = animate) }
                        ?: run { onLog("${name}_LOAD_RESULT success=false"); null }
                } catch (t: Throwable) {
                    onLog("${name}_LOAD_EXCEPTION ${Log.getStackTraceString(t)}")
                    null
                }
            }

            loadModel("PLAYER", PLAYER_MODEL, 1.8f, true)
            loadModel("ZOMBIE", ZOMBIE_MODEL, 1.8f, true)
            loadModel("PROP", PROP_MODEL, 1.4f, false)
            onLog("SCENEVIEW_CONTENT_END")
        }

        Button(onClick = onExit, modifier = Modifier.align(Alignment.TopEnd).padding(18.dp)) { Text("MENU") }
    }
}
