#include <jni.h>
#include <qrcode.h>
#include <android/log.h>
#include <dirent.h>
#include <stdlib.h>
#include <string.h>

#define encodeTextToQR Java_com_ammar_filescenter_common_Utils_encodeTextToQR
#define findImagesRecursively Java_com_ammar_filescenter_common_Utils_findImagesRecursively

#define numelemnts(arr) sizeof(arr)/sizeof(arr[0])

void findImagesRecursively_REAL(JNIEnv *env, const char* path, jobject filesArrayList, jclass FileClass, int depth);
char *join_path(const char *path1, const char *path2);
bool isFileExtinArray(const char *filename, const char **exts, int exts_len);

jmethodID FileConstructor;
jmethodID ArrayListAdd;
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1; // error
    }

    jclass FileClass = (*env)->FindClass(env, "java/io/File");
    jclass ArrayList = (*env)->FindClass(env, "java/util/ArrayList");
    FileConstructor = (*env)->GetMethodID(env, FileClass, "<init>", "(Ljava/lang/String;)V");
    ArrayListAdd = (*env)->GetMethodID(env, ArrayList, "add", "(Ljava/lang/Object;)Z");

    return JNI_VERSION_1_6;
}


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


JNIEXPORT void JNICALL findImagesRecursively(JNIEnv *env, jobject thiz, jstring root, jobject filesArrayList) {
    const char *path = (*env)->GetStringUTFChars(env, root, JNI_FALSE);
    jclass FileClass = (*env)->FindClass(env, "java/io/File");
    findImagesRecursively_REAL(env, path, filesArrayList, FileClass, 0);
}


int filter( const struct dirent *e ) {
    return strcmp(e->d_name, ".") && strcmp(e->d_name, "..");
}

void findImagesRecursively_REAL(JNIEnv *env, const char* path, jobject filesArrayList, jclass FileClass, int depth) {
    const char *image_exts[] = {
        "png",
        "jpg",
        "jpeg",
        "webp",
        "gif",
        "bmp",
        "apng",
        "avif",
        "tiff",
        "ico",
        "cfg"
    };

    struct dirent **files_list;
    int size = scandir(path, &files_list, filter, alphasort);
    if( size < 0 ) return;
    
    for( int i = 0 ; i < size ; i++ ) {
        struct dirent *file = files_list[i];
        __android_log_print(ANDROID_LOG_DEBUG, "DIRLOG", "DIR: %s\n", file->d_name);
        switch (file->d_type) {
            case DT_DIR: {
                char *full_dir_path = join_path(path, file->d_name);
                if( depth < 3 ) {
                    findImagesRecursively_REAL(env, full_dir_path, filesArrayList, FileClass, depth+1);
                }
                free(full_dir_path); // join_path uses malloc so we have to free
            }
                break;
            case DT_REG:
                if( isFileExtinArray(file->d_name, image_exts, numelemnts(image_exts)) ) {
                    char *full_img_path = join_path(path, file->d_name);
                    
                    jobject fileObject = (*env)->NewObject(env, FileClass, FileConstructor, (*env)->NewStringUTF(env, full_img_path));
                    (*env)->CallBooleanMethod(env, filesArrayList, ArrayListAdd, fileObject);

                    free(full_img_path); // join_path uses malloc so we have to free
                }
                break;
            default:
                break;
        }
        
        free(file);
    }
    free(files_list);
}

// free string after use
char *join_path(const char *path1, const char *path2) {
    int path1_len = strlen(path1);
    int path2_len = strnlen(path2, 256);
    char *full_dir_path = malloc(sizeof(char) * (path1_len + path2_len + 2));
    memcpy(full_dir_path, path1, path1_len + 1);

    if( full_dir_path[path1_len-1] != '/' ) {
        strcat(full_dir_path, "/");
        full_dir_path = realloc(full_dir_path, sizeof(char) * (path1_len + path2_len + 1));
    }

    strcat(full_dir_path, path2);
    return full_dir_path;
}

bool isFileExtinArray(const char *filename, const char **exts, int exts_len) {
    int dotIndex = -1;
    const char *s;
    for (s = filename; *s; ++s){
        if( *s == '.' ) {
            dotIndex = s - filename + 1;
        }
    }
    int size = (s - filename);

    if( dotIndex == size ||
        dotIndex == -1  ) return false; // safety first

    const char *ext = filename + dotIndex;

    for(int i = 0; i < exts_len ; i++) {
        if( !strcmp(ext, exts[i]) ) return true;
    }
    return false;
}
