package com.tomasthrawat.hyouka3dzombie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun ZombieGameScreen(
    playing: Boolean,
    onStart: () -> Unit,
    onExit: () -> Unit
) {
    if (!playing) {
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

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val environment = rememberEnvironment(environmentLoader) {
        environmentLoader.createHDREnvironment("environments/neutral/neutral_ibl.ktx")
            ?: createEnvironment(environmentLoader)
    }
    val cameraManipulator = rememberCameraManipulator()
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 300_000f
    }

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
            rememberModelInstance(
                modelLoader = modelLoader,
                fileLocation = PLAYER_MODEL
            )?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 1.8f,
                    autoAnimate = true
                )
            }

            rememberModelInstance(
                modelLoader = modelLoader,
                fileLocation = ZOMBIE_MODEL
            )?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 1.8f,
                    autoAnimate = true
                )
            }

            rememberModelInstance(
                modelLoader = modelLoader,
                fileLocation = PROP_MODEL
            )?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 1.4f
                )
            }
        }

        Button(
            onClick = onExit,
            modifier = Modifier.align(Alignment.TopEnd).padding(18.dp)
        ) {
            Text("MENU")
        }
    }
}
