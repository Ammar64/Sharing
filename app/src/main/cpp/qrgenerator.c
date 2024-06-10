#include <jni.h>
#include <qrcode.h>

#define encodeTextToQR Java_com_ammar_filescenter_utils_Utils_encodeTextToQR

JNIEXPORT jbyteArray JNICALL encodeTextToQR(JNIEnv *env, jobject thiz, jstring jtext) {
    const char *text = (*env)->GetStringUTFChars(env, jtext, 0);
    // generate QR Code
    QRCode qrcode;
    uint8_t qrcodeData[qrcode_getBufferSize(2)];
    qrcode_initText(&qrcode, qrcodeData, 2, 0, text);

    jbyteArray qrCodeArray = (*env)->NewByteArray(env, qrcode.size * qrcode.size);
    jbyte *qrCodeBytes = (*env)->GetByteArrayElements(env, qrCodeArray, JNI_FALSE);

    for (int y = 0; y < qrcode.size; y++) {
        for (int x = 0; x < qrcode.size; x++) {
            qrCodeBytes[qrcode.size * x + y] = qrcode_getModule(&qrcode, x, y) ? (jbyte) 1
                                                                               : (jbyte) 0;
        }
    }

    (*env)->ReleaseByteArrayElements(env, qrCodeArray, qrCodeBytes, 0);
    return qrCodeArray;
}
