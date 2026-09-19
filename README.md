# Hyouka: Nightfall

Native Kotlin Android 3D zombie survival game.

The project uses Jetpack Compose for the menu/HUD layer and SceneView/Filament for native 3D rendering. It is not a WebView or HTML game.

The current prototype loads real GLB models from the connected free 3D asset catalogue. Remote assets require network access, so the manifest grants INTERNET permission.

No telemetry, logging, tracing, or diagnostic files are written by the app.
