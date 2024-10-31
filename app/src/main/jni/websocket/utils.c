#include <jni.h>
#include <android/log.h>
#define byteArrayToShort Java_com_ammar_sharing_common_utils_TypesUtils_byteArrayToShort
#define byteArrayToLong Java_com_ammar_sharing_common_utils_TypesUtils_byteArrayToLong

JNIEXPORT jint JNICALL byteArrayToShort(JNIEnv *env, jobject thiz, jbyteArray array) {
    jsize length = (*env)->GetArrayLength(env, array);
    if( length != 2 ) {
        jclass IllegalArgumentException = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        (*env)->ThrowNew(env, IllegalArgumentException, "Byte array must have the length of 2");
        return 0;
    }
    uint8_t *arrayElements = (uint8_t*) (*env)->GetByteArrayElements(env, array, NULL);
    uint16_t number = (uint16_t) arrayElements[0] << 8 | (uint16_t) arrayElements[1];
    (*env)->ReleaseByteArrayElements(env, array, (jbyte *) arrayElements, JNI_ABORT);
    return number;
}

JNIEXPORT jlong JNICALL byteArrayToLong(JNIEnv *env, jobject thiz, jbyteArray array) {
    jsize length = (*env)->GetArrayLength(env, array);
    if( length != 8 ) {
        jclass IllegalArgumentException = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        (*env)->ThrowNew(env, IllegalArgumentException, "Byte array must have the length of 8");
        return 0;
    }
    uint8_t *arrayElements = (uint8_t*) (*env)->GetByteArrayElements(env, array, NULL);
    uint64_t number = (uint64_t)arrayElements[0] << 56 | (uint64_t)arrayElements[1] << 48 | (uint64_t)arrayElements[2] << 40 | (uint64_t)arrayElements[3] << 32 | (uint64_t)arrayElements[4] << 24 | (uint64_t)arrayElements[5] << 16 | (uint64_t)arrayElements[6] << 8 | (uint64_t)arrayElements[7];
    (*env)->ReleaseByteArrayElements(env, array, (jbyte*) arrayElements, JNI_ABORT);
    return number;
}
