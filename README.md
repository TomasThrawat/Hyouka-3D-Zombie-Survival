# Hyouka: Nightfall

Native Kotlin Android 3D zombie survival game.

Version 3 replaces the cube-only OpenGL prototype with a native Filament/SceneView renderer. It is not a WebView or HTML game.

The renderer uses real GLB models from the connected free 3D asset catalogue. Touch camera input is handled directly by Compose pointer events instead of queueing one render-thread event per touch move.

No telemetry, logging, tracing, or diagnostic files are written by the app.
