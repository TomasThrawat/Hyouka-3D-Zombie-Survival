plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

android {
    namespace = "com.tomasthrawat.hyouka3dzombie"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tomasthrawat.hyouka3dzombie"
        minSdk = 28
        targetSdk = 36
        versionCode = 9
        versionName = "9.0"
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDir(layout.buildDirectory.dir("generated/sceneAssets"))
        }
    }
}

val prepareSceneAssets by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/sceneAssets/models")
    outputs.dir(outputDir)

    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()

        val assets = mapOf(
            "player.glb" to "https://cdn.3dassets.dev/assets/131/v1/model.glb",
            "zombie.glb" to "https://cdn.3dassets.dev/assets/29162/v1/model.glb",
            "prop.glb" to "https://cdn.3dassets.dev/assets/27504/v1/model.glb"
        )

        assets.forEach { (name, url) ->
            val targetFile = File(dir, name)
            if (!targetFile.exists() || targetFile.length() < 1024L) {
                println("Downloading SceneView asset: " + name)
                URI(url).toURL().openStream().use { input ->
                    targetFile.outputStream().use { output -> input.copyTo(output) }
                }
            }
            require(targetFile.length() >= 1024L) {
                "Invalid SceneView asset " + name + ": " + targetFile.length() + " bytes"
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn(prepareSceneAssets)
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui:1.9.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.1")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("io.github.sceneview:sceneview:4.26.0")
}
