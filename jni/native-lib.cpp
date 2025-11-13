#include <jni.h>
#include <android/log.h>
#include <vector>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "native-lib"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetector_JNIWrapper_processFrame(
        JNIEnv* env, jobject /*thiz*/, jbyteArray nv21Data, jint width, jint height) {
    const int w = width;
    const int h = height;
    const int yuvSize = env->GetArrayLength(nv21Data);

    std::vector<unsigned char> yuv(yuvSize);
    env->GetByteArrayRegion(nv21Data, 0, yuvSize, reinterpret_cast<jbyte*>(yuv.data()));

    // NV21: Y plane (w*h) + interleaved VU (w*h/2)
    cv::Mat yuvMat(h + h / 2, w, CV_8UC1, yuv.data());

    // Convert to BGR then to Gray to follow requirement (NV21 -> RGB, then Canny)
    cv::Mat bgr;
    cv::cvtColor(yuvMat, bgr, cv::COLOR_YUV2BGR_NV21);

    cv::Mat gray;
    cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);

    cv::Mat edges;
    cv::Canny(gray, edges, 50, 150);

    // Return grayscale edges (w*h)
    jbyteArray out = env->NewByteArray(w * h);
    env->SetByteArrayRegion(out, 0, w * h, reinterpret_cast<const jbyte*>(edges.data));
    return out;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetector_JNIWrapper_processFrameRGB(
        JNIEnv* env, jobject /*thiz*/, jbyteArray nv21Data, jint width, jint height) {
    const int w = width;
    const int h = height;
    const int yuvSize = env->GetArrayLength(nv21Data);
    std::vector<unsigned char> yuv(yuvSize);
    env->GetByteArrayRegion(nv21Data, 0, yuvSize, reinterpret_cast<jbyte*>(yuv.data()));

    cv::Mat yuvMat(h + h / 2, w, CV_8UC1, yuv.data());

    cv::Mat rgba;
    cv::cvtColor(yuvMat, rgba, cv::COLOR_YUV2RGBA_NV21);

    jbyteArray out = env->NewByteArray(w * h * 4);
    env->SetByteArrayRegion(out, 0, w * h * 4, reinterpret_cast<const jbyte*>(rgba.data));
    return out;
}
