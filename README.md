# Edge Detector (Android + OpenCV + OpenGL ES) with Web Viewer

## Overview
This project implements a real-time edge-detection Android app that:
- Captures camera frames via Camera1 + SurfaceTexture
- Sends NV21 frames to C++ through JNI/NDK
- Uses OpenCV (C++) to convert NV21 → RGB and apply Canny edge detection
- Returns a processed grayscale byte array for edge output
- Renders frames using OpenGL ES 2.0 with a single texture
- Maintains ~15 FPS on modest devices (targeting 640x480 preview)
- Toggles between Raw (RGBA) and Edge (grayscale) display

A separate TypeScript web viewer displays a static processed frame (Base64 PNG) and overlays FPS and resolution.

## Architecture
- Android (Kotlin):
  - CameraRenderer opens Camera1, configures NV21, and uses setPreviewCallbackWithBuffer.
  - Each received NV21 frame is processed in native code via JNI.
  - GLRenderer uploads either LUMINANCE (grayscale) or RGBA textures and draws a full-screen quad.
  - GLTexture hosts the GL context (OpenGL ES 2.0).
- Native (C++/OpenCV):
  - processFrame: NV21 → BGR → Gray → Canny → grayscale bytes (w*h) back to Java.
  - processFrameRGB: NV21 → RGBA → bytes (w*h*4) back to Java for raw color.
- Shaders (GLSL): Simple pass-through vertex + sampler-based fragment.
- Web (TypeScript): Loads a Base64 PNG, shows it in an <img>, and overlays FPS + resolution text via DOM.

## Frame Flow (Android)
1. Camera1 delivers NV21 (w*h*3/2) buffers via setPreviewCallbackWithBuffer.
2. Kotlin calls into JNI with `processFrame(nv21, w, h)` for edges or `processFrameRGB(nv21, w, h)` for raw.
3. C++/OpenCV converts and optionally runs Canny, returning a byte array.
4. Kotlin uploads bytes to a GL texture:
   - edges → GL_LUMINANCE texture (w x h)
   - raw → GL_RGBA texture (w x h)
5. GL draws a full-screen quad using the texture.

## JNI
Required function (implemented):
- `jbyteArray processFrame(JNIEnv*, jobject, jbyteArray, jint, jint)`

Additionally provided for raw color:
- `jbyteArray processFrameRGB(JNIEnv*, jobject, jbyteArray, jint, jint)`

## Build Instructions (Android)
Prereqs: Android Studio, Android SDK, NDK, and OpenCV Android SDK.

1. OpenCV SDK
   - Download the OpenCV Android SDK from https://opencv.org/releases/
   - Note the path to `OpenCV-android-sdk/sdk/native/jni`.

2. Configure CMake
   - In Android Studio, set CMake arguments so that `OpenCV_DIR` points to the above path.
   - Example: `-DOpenCV_DIR="/absolute/path/to/OpenCV-android-sdk/sdk/native/jni"`

3. Open Project
   - Open the `project/` directory in Android Studio as a project.
   - Let Gradle sync. Ensure NDK and CMake components are installed.

4. Build/Run
   - Connect an Android device.
   - Run the app module. On first launch, grant Camera permission.

Notes:
- The module-level `app/CMakeLists.txt` delegates to `jni/CMakeLists.txt`.
- If you prefer, you can copy/symlink OpenCV headers/libs under `jni/include/opencv` and adjust CMake, but the recommended approach is `OpenCV_DIR`.

## Web Viewer
Prereqs: Node.js (for `npx`).

Build:
- From `project/web`, run: `npx tsc -p .`
- This produces `dist/index.js`.

Run:
- Open `project/web/index.html` in a browser (or serve locally).
- The viewer loads `sample-frame-base64.txt`, shows the image, and overlays FPS + resolution.

## Screenshots
- Android Edge View: [placeholder]
- Android Raw View: [placeholder]
- Web Viewer: [placeholder]

## Notes
- Performance target is ~15 FPS at 640x480. Increase preview size with caution.
- Fragment shader accepts both LUMINANCE and RGBA textures without branching.
- Orientation/rotation can vary by device; for simplicity, frames are rendered as-is.
