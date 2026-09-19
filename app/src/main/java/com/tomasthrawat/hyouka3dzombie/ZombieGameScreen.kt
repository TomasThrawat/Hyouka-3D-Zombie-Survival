package com.tomasthrawat.hyouka3dzombie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
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
    val cameraManipulator = rememberCameraManipulator()

    val player = rememberModelInstance(modelLoader, PLAYER_MODEL)
    val zombie = rememberModelInstance(modelLoader, ZOMBIE_MODEL)
    val prop = rememberModelInstance(modelLoader, PROP_MODEL)

    var yaw by remember { mutableFloatStateOf(0f) }

    Box(Modifier.fillMaxSize()) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraManipulator = cameraManipulator,
            autoCenterContent = true
        ) {
            player?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 1.8f,
                    autoAnimate = true
                )
            }
            zombie?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 1.8f,
                    autoAnimate = true
                )
            }
            prop?.let {
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
