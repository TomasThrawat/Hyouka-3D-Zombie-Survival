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

private const val TAG = "Hyouka3D"
private const val PLAYER_MODEL = "https://cdn.3dassets.dev/assets/131/v1/model.glb"
private const val ZOMBIE_MODEL = "https://cdn.3dassets.dev/assets/29162/v1/model.glb"
private const val PROP_MODEL = "https://cdn.3dassets.dev/assets/27504/v1/model.glb"

@Composable
fun ZombieGameScreen(
    playing: Boolean,
    onStart: () -> Unit,
    onExit: () -> Unit
) {
    LaunchedEffect(playing) {
        Log.i(TAG, "SCREEN_STATE playing=$playing")
    }

    if (!playing) {
        Log.d(TAG, "MENU_RENDER")
        Box(
            Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onStart, modifier = Modifier.padding(24.dp)) {
                Text("START SURVIVAL")
            }
        }
        return
    }

    Log.i(TAG, "GAME_RENDER_BEGIN")
    Log.i(TAG, "MODEL_URL player=$PLAYER_MODEL")
    Log.i(TAG, "MODEL_URL zombie=$ZOMBIE_MODEL")
    Log.i(TAG, "MODEL_URL prop=$PROP_MODEL")

    val engine = rememberEngine()
    Log.i(TAG, "ENGINE_CREATED engine=$engine")

    val modelLoader = rememberModelLoader(engine)
    Log.i(TAG, "MODEL_LOADER_CREATED loader=$modelLoader")

    val environmentLoader = rememberEnvironmentLoader(engine)
    Log.i(TAG, "ENV_LOADER_CREATED loader=$environmentLoader")

    val environment = rememberEnvironment(environmentLoader) {
        Log.i(TAG, "ENVIRONMENT_CREATE_BEGIN")
        try {
            val env = environmentLoader.createHDREnvironment("environments/neutral/neutral_ibl.ktx")
                ?: createEnvironment(environmentLoader)
            Log.i(TAG, "ENVIRONMENT_CREATE_RESULT success=${env != null}")
            env
        } catch (t: Throwable) {
            Log.e(TAG, "ENVIRONMENT_CREATE_FAILED", t)
            try {
                createEnvironment(environmentLoader).also {
                    Log.i(TAG, "ENVIRONMENT_FALLBACK_RESULT success=${it != null}")
                }
            } catch (fallback: Throwable) {
                Log.e(TAG, "ENVIRONMENT_FALLBACK_FAILED", fallback)
                null
            }
        }
    }

    val cameraManipulator = rememberCameraManipulator()
    Log.i(TAG, "CAMERA_MANIPULATOR_CREATED")

    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 300_000f
        Log.i(TAG, "MAIN_LIGHT_CONFIGURED intensity=300000")
    }

    Box(Modifier.fillMaxSize()) {
        Log.i(TAG, "SCENEVIEW_CREATE_BEGIN")
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            environmentLoader = environmentLoader,
            environment = environment,
            cameraManipulator = cameraManipulator,
            mainLightNode = mainLightNode
        ) {
            Log.i(TAG, "SCENEVIEW_CONTENT_ENTERED")

            val playerInstance = try {
                Log.i(TAG, "PLAYER_LOAD_BEGIN")
                rememberModelInstance(
                    modelLoader = modelLoader,
                    fileLocation = PLAYER_MODEL
                )
            } catch (t: Throwable) {
                Log.e(TAG, "PLAYER_LOAD_EXCEPTION", t)
                null
            }
            Log.i(TAG, "PLAYER_LOAD_RESULT instanceNull=${playerInstance == null}")
            playerInstance?.let {
                Log.i(TAG, "PLAYER_NODE_CREATE")
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 1.8f,
                    autoAnimate = true
                )
            }

            val zombieInstance = try {
                Log.i(TAG, "ZOMBIE_LOAD_BEGIN")
                rememberModelInstance(
                    modelLoader = modelLoader,
                    fileLocation = ZOMBIE_MODEL
                )
            } catch (t: Throwable) {
                Log.e(TAG, "ZOMBIE_LOAD_EXCEPTION", t)
                null
            }
            Log.i(TAG, "ZOMBIE_LOAD_RESULT instanceNull=${zombieInstance == null}")
            zombieInstance?.let {
                Log.i(TAG, "ZOMBIE_NODE_CREATE")
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 1.8f,
                    autoAnimate = true
                )
            }

            val propInstance = try {
                Log.i(TAG, "PROP_LOAD_BEGIN")
                rememberModelInstance(
                    modelLoader = modelLoader,
                    fileLocation = PROP_MODEL
                )
            } catch (t: Throwable) {
                Log.e(TAG, "PROP_LOAD_EXCEPTION", t)
                null
            }
            Log.i(TAG, "PROP_LOAD_RESULT instanceNull=${propInstance == null}")
            propInstance?.let {
                Log.i(TAG, "PROP_NODE_CREATE")
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 1.4f
                )
            }

            Log.i(TAG, "SCENEVIEW_CONTENT_END player=${playerInstance != null} zombie=${zombieInstance != null} prop=${propInstance != null}")
        }

        Button(
            onClick = onExit,
            modifier = Modifier.align(Alignment.TopEnd).padding(18.dp)
        ) {
            Log.d(TAG, "MENU_BUTTON_RENDER")
            Text("MENU")
        }
    }
}
