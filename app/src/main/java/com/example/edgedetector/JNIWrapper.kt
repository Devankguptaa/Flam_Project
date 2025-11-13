package com.example.edgedetector

object JNIWrapper {
    init {
        System.loadLibrary("native-lib")
    }

    external fun processFrame(nv21: ByteArray, width: Int, height: Int): ByteArray
    external fun processFrameRGB(nv21: ByteArray, width: Int, height: Int): ByteArray
}
