package com.tomasthrawat.hyouka3dzombie

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberNodes

private const val PLAYER_MODEL = "https://cdn.3dassets.dev/assets/131/v1/model.glb"
private const val ZOMBIE_MODEL = "https://cdn.3dassets.dev/assets/29162/v1/model.glb"
private const val PROP_MODEL = "https://cdn.3dassets.dev/assets/27504/v1/model.glb"

@Composable
fun ZombieGameScreen(playing: Boolean, onStart: () -> Unit, onExit: () -> Unit) {
    if (!playing) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Button(onClick = onStart, modifier = Modifier.padding(24.dp)) {
                Text("START SURVIVAL")
            }
        }
        return
    }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = io.github.sceneview.math.Position(z = 8.0f, y = 4.5f)
        lookAt(io.github.sceneview.math.Position(y = 1.0f))
    }

    val player = rememberModelInstance(modelLoader, PLAYER_MODEL)
    val zombie = rememberModelInstance(modelLoader, ZOMBIE_MODEL)
    val prop = rememberModelInstance(modelLoader, PROP_MODEL)

    val nodes = rememberNodes {
        add(ModelNode(player, scaleToUnits = 1.8f).apply {
            position = io.github.sceneview.math.Position(0f, 0f, 0f)
        })
        add(ModelNode(zombie, scaleToUnits = 1.8f).apply {
            position = io.github.sceneview.math.Position(3f, 0f, -4f)
        })
        add(ModelNode(prop, scaleToUnits = 1.4f).apply {
            position = io.github.sceneview.math.Position(-3f, 0f, -3f)
        })
    }

    var yaw by remember { mutableFloatStateOf(0f) }
    Box(
        Modifier.fillMaxSize().pointerInput(Unit) {
            detectDragGestures { _, dragAmount ->
                yaw += dragAmount.x * 0.18f
                cameraNode.rotation = io.github.sceneview.math.Rotation(x = -12f, y = yaw)
            }
        }
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            childNodes = nodes
        )
        Button(
            onClick = onExit,
            modifier = Modifier.align(Alignment.TopEnd).padding(18.dp)
        ) { Text("MENU") }
    }
}
