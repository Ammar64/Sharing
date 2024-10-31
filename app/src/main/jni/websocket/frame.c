#include <jni.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>

#define constructWebSocketFrame Java_com_ammar_sharing_network_websocket_WebSocketImpl_constructWebSocketFrame
JNIEXPORT jbyteArray JNICALL constructWebSocketFrame(JNIEnv *env, jobject thiz, jbyteArray payload, jbyte opCode) {
    if( opCode & 0xf0 ) {
        jclass IllegalArgumentException = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        (*env)->ThrowNew(env, IllegalArgumentException, "Invalid opcode");
        return NULL;
    }
    
    jint length = 0;
    if(payload == NULL) {
        length = 0;
    } else {
        length = (*env)->GetArrayLength(env, payload);
    }

    uint8_t frameByte0 = 0x80 | opCode;
    uint8_t frameByte1;
    memset(&frameByte1, 0, 1);

    int payloadOffset;
    if( length <= 125 ) {
        payloadOffset = 2;
        frameByte1 = length;
    } else if (length <= UINT16_MAX) {
        payloadOffset = 4;
        frameByte1 = 126;
    } else if(length <= UINT64_MAX) {
        payloadOffset = 10;
        frameByte1 = 127;
    } else {
        jclass RuntimeException = (*env)->FindClass(env, "java/lang/RuntimeException");
        (*env)->ThrowNew(env, RuntimeException, "Invalid payload length");
        return NULL;
    }

    jbyteArray wsFrameRawJava = (*env)->NewByteArray(env, length + payloadOffset);
    jbyte *wsFrameRaw = (*env)->GetByteArrayElements(env, wsFrameRawJava, NULL);
    memcpy(&wsFrameRaw[0], &frameByte0, 1);
    memcpy(&wsFrameRaw[1], &frameByte1, 1);
    if( payloadOffset == 4 ) {
        uint16_t lengthNet = htons((uint16_t)length);
        memcpy(&wsFrameRaw[2], &lengthNet, 2);
    } else if( payloadOffset == 10 ) {
        uint64_t lengthNet = htobe64((uint64_t) length);
        memcpy(&wsFrameRaw[2], &lengthNet, 8);
    }
    if( payload != NULL && length != 0 ) {
        jbyte *payloadRawData = (*env)->GetPrimitiveArrayCritical(env, payload, NULL);
        memcpy(&wsFrameRaw[payloadOffset], payloadRawData, (size_t) length);
        (*env)->ReleasePrimitiveArrayCritical(env, payload, payloadRawData, JNI_ABORT);
    }
    (*env)->ReleaseByteArrayElements(env, wsFrameRawJava, wsFrameRaw, 0);
    return wsFrameRawJava;
}
