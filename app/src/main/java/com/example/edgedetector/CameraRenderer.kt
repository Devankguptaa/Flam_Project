package com.example.edgedetector

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

class CameraRenderer(
    private val context: Context,
    private val glRenderer: GLRenderer,
    private val listener: FrameListener
) {
    enum class Mode { RAW, EDGE }

    interface FrameListener {
        fun onFpsAndResolution(fps: Double, width: Int, height: Int, mode: Mode)
    }

    private var camera: Camera? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var previewSize: Camera.Size? = null
    private var mode: Mode = Mode.EDGE

    private val handlerThread = HandlerThread("CameraThread")
    private lateinit var handler: Handler

    private val processing = AtomicBoolean(false)
    private var lastTime = System.nanoTime()
    private var frameCount = 0

    fun setMode(newMode: Mode) {
        mode = newMode
    }

    fun start() {
        if (!handlerThread.isAlive) {
            handlerThread.start()
        }
        handler = Handler(handlerThread.looper)
        handler.post { openCamera() }
    }

    fun stop() {
        handler.post { closeCamera() }
        handlerThread.quitSafely()
    }

    private fun openCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
            val cam = camera ?: return

            // Choose a preview size around 640x480 for performance
            val params = cam.parameters
            var chosen: Camera.Size? = null
            var bestScore = Int.MAX_VALUE
            for (s in params.supportedPreviewSizes) {
                val score = Math.abs(s.width * s.height - 640 * 480)
                if (score < bestScore) {
                    bestScore = score
                    chosen = s
                }
            }
            previewSize = chosen ?: params.previewSize
            params.setPreviewSize(previewSize!!.width, previewSize!!.height)
            params.previewFormat = android.graphics.ImageFormat.NV21
            cam.parameters = params

            // Need a SurfaceTexture for the camera preview pipe even if we don't display it
            surfaceTexture = SurfaceTexture(0)
            cam.setPreviewTexture(surfaceTexture)

            // Setup callback buffers
            val w = previewSize!!.width
            val h = previewSize!!.height
            val bufferSize = w * h * 3 / 2
            repeat(3) { cam.addCallbackBuffer(ByteArray(bufferSize)) }

            cam.setPreviewCallbackWithBuffer { data, c ->
                if (data == null || c == null) return@setPreviewCallbackWithBuffer
                onFrame(data, w, h)
                c.addCallbackBuffer(data)
            }

            cam.startPreview()
        } catch (t: Throwable) {
            Log.e("CameraRenderer", "Failed to open camera", t)
        }
    }

    private fun closeCamera() {
        try {
            camera?.setPreviewCallbackWithBuffer(null)
            camera?.stopPreview()
            camera?.release()
        } catch (_: Throwable) {}
        camera = null
        surfaceTexture?.release()
        surfaceTexture = null
    }

    private fun onFrame(nv21: ByteArray, width: Int, height: Int) {
        if (processing.getAndSet(true)) return
        try {
            val start = System.nanoTime()
            if (mode == Mode.EDGE) {
                val gray = JNIWrapper.processFrame(nv21, width, height)
                glRenderer.updateGrayscaleFrame(gray, width, height)
            } else {
                val rgba = JNIWrapper.processFrameRGB(nv21, width, height)
                glRenderer.updateRGBAFrame(rgba, width, height)
            }
            glRenderer.requestRender()

            frameCount++
            val now = System.nanoTime()
            val elapsed = now - lastTime
            if (elapsed >= 1_000_000_000L) {
                val fps = frameCount * 1e9 / elapsed
                lastTime = now
                frameCount = 0
                listener.onFpsAndResolution(fps, width, height, mode)
            }
        } catch (t: Throwable) {
            Log.e("CameraRenderer", "Frame processing error", t)
        } finally {
            processing.set(false)
        }
    }
}
