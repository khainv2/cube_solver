#include <jni.h>
#include <string>
#include "algorithm/solver.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_khainv9_kubesolver_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
//    kube::init();
    return env->NewStringUTF(hello.c_str());
}