package com.example.edgedetector

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), CameraRenderer.FrameListener {
    private lateinit var glView: GLTexture
    private lateinit var renderer: GLRenderer
    private lateinit var toggle: ToggleButton
    private lateinit var overlay: TextView
    private lateinit var cameraRenderer: CameraRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glView = findViewById(R.id.gl_view)
        overlay = findViewById(R.id.overlay_text)
        toggle = findViewById(R.id.toggle_mode)

        renderer = GLRenderer(this)
        glView.setRenderer(renderer)
        glView.renderMode = GLTexture.RENDERMODE_WHEN_DIRTY

        cameraRenderer = CameraRenderer(this, renderer, this)

        toggle.setOnCheckedChangeListener { _, isChecked ->
            cameraRenderer.setMode(if (isChecked) CameraRenderer.Mode.EDGE else CameraRenderer.Mode.RAW)
        }

        ensurePermissions()
    }

    private fun ensurePermissions() {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.CAMERA)
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 100)
        }
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraRenderer.start()
        }
    }

    override fun onPause() {
        cameraRenderer.stop()
        glView.onPause()
        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraRenderer.start()
        }
    }

    override fun onFpsAndResolution(fps: Double, width: Int, height: Int, mode: CameraRenderer.Mode) {
        runOnUiThread {
            overlay.visibility = View.VISIBLE
            val modeText = if (mode == CameraRenderer.Mode.EDGE) "Edge" else "Raw"
            overlay.text = String.format("%s | %dx%d | %.1f FPS", modeText, width, height, fps)
        }
    }
}
