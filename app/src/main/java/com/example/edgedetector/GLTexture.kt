package com.example.edgedetector

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class GLTexture @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    init {
        setEGLContextClientVersion(2)
    }

    fun setRenderer(renderer: GLRenderer) {
        super.setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        renderer.setOnRequestRender { this.requestRender() }
    }
}
