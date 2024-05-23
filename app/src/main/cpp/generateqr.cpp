#include <jni.h>
#include <qrcodegen.hpp>

#define encodeTextToQR Java_com_ammar_filescenter_activities_SharingActivity_encodeTextToQR
using namespace qrcodegen;

extern "C" JNIEXPORT jbyteArray JNICALL encodeTextToQR(JNIEnv *env, jobject thiz, jstring text) {
    QrCode qrcode = QrCode::encodeText(env->GetStringUTFChars(text, JNI_FALSE), QrCode::Ecc::LOW);

    
    int qrCodeSize = qrcode.getSize();
    jbyteArray qrCodeArray = env->NewByteArray( qrCodeSize * qrCodeSize );
    jbyte *qrCodeBytes = env->GetByteArrayElements(qrCodeArray, JNI_FALSE);

    for( int y = 0 ; y < qrCodeSize ; y++ ) {
        for( int x = 0 ; x < qrCodeSize ; x++ ) {
            qrCodeBytes[qrCodeSize * x + y] = qrcode.getModule(x, y) ?(jbyte)1 : (jbyte)0; 
        }
    }

    env->ReleaseByteArrayElements(qrCodeArray, qrCodeBytes, 0);
    return qrCodeArray;
}
