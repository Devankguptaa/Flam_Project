package com.example.edgedetector

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var program = 0
    private var aPos = 0
    private var aTex = 0
    private var uTex = 0

    private var onRequestRender: (() -> Unit)? = null

    private var textureId = 0
    private var currentFormat: Int = GLES20.GL_LUMINANCE
    private var texWidth = 0
    private var texHeight = 0

    private val needsUpload = AtomicBoolean(false)
    private var pendingBuffer: ByteBuffer? = null
    private var grayscaleBuffer: ByteBuffer? = null
    private var rgbaBuffer: ByteBuffer? = null

    private val vertexData: FloatBuffer
    private val texData: FloatBuffer

    init {
        val vertices = floatArrayOf(
            -1f, -1f,
             1f, -1f,
            -1f,  1f,
             1f,  1f
        )
        val tex = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
        )
        vertexData = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexData.put(vertices).position(0)
        texData = ByteBuffer.allocateDirect(tex.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        texData.put(tex).position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        program = ShaderUtils.buildProgram(VERT_SRC, FRAG_SRC)
        aPos = GLES20.glGetAttribLocation(program, "aPosition")
        aTex = GLES20.glGetAttribLocation(program, "aTexCoord")
        uTex = GLES20.glGetUniformLocation(program, "uTexture")

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        GLES20.glClearColor(0f, 0f, 0f, 1f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        if (needsUpload.get()) {
            synchronized(this) {
                val buf = pendingBuffer
                if (buf != null) {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
                    if (currentFormat == GLES20.GL_LUMINANCE) {
                        GLES20.glTexImage2D(
                            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                            texWidth, texHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, buf
                        )
                    } else {
                        GLES20.glTexImage2D(
                            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                            texWidth, texHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf
                        )
                    }
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
                    needsUpload.set(false)
                }
            }
        }

        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glEnableVertexAttribArray(aTex)

        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 0, vertexData)
        GLES20.glVertexAttribPointer(aTex, 2, GLES20.GL_FLOAT, false, 0, texData)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTex, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aTex)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    fun updateGrayscaleFrame(gray: ByteArray, width: Int, height: Int) {
        if (grayscaleBuffer == null || grayscaleBuffer!!.capacity() < gray.size) {
            grayscaleBuffer = ByteBuffer.allocateDirect(gray.size)
        }
        grayscaleBuffer!!.clear()
        grayscaleBuffer!!.put(gray)
        grayscaleBuffer!!.position(0)
        synchronized(this) {
            pendingBuffer = grayscaleBuffer
            texWidth = width
            texHeight = height
            currentFormat = GLES20.GL_LUMINANCE
            needsUpload.set(true)
        }
    }

    fun updateRGBAFrame(rgba: ByteArray, width: Int, height: Int) {
        if (rgbaBuffer == null || rgbaBuffer!!.capacity() < rgba.size) {
            rgbaBuffer = ByteBuffer.allocateDirect(rgba.size)
        }
        rgbaBuffer!!.clear()
        rgbaBuffer!!.put(rgba)
        rgbaBuffer!!.position(0)
        synchronized(this) {
            pendingBuffer = rgbaBuffer
            texWidth = width
            texHeight = height
            currentFormat = GLES20.GL_RGBA
            needsUpload.set(true)
        }
    }

    fun setOnRequestRender(cb: (() -> Unit)?) {
        onRequestRender = cb
    }

    fun requestRender() {
        onRequestRender?.invoke()
    }

    companion object {
        private const val VERT_SRC = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """
        private const val FRAG_SRC = """
            precision mediump float;
            uniform sampler2D uTexture;
            varying vec2 vTexCoord;
            void main() {
                vec4 c = texture2D(uTexture, vTexCoord);
                gl_FragColor = c;
            }
        """
    }
}
