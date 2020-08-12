/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class me_roan_util_FileSelector */

#ifndef _Included_me_roan_util_FileSelector
#define _Included_me_roan_util_FileSelector
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     me_roan_util_FileSelector
 * Method:    showNativeFileOpen
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileOpen
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     me_roan_util_FileSelector
 * Method:    showNativeFolderOpen
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFolderOpen
  (JNIEnv *, jclass);

/*
 * Class:     me_roan_util_FileSelector
 * Method:    showNativeFileSave
 * Signature: (ILjava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileSave
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     me_roan_util_FileSelector
 * Method:    registerNativeFileExtension
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_me_roan_util_FileSelector_registerNativeFileExtension
  (JNIEnv *, jclass, jstring, jstring, jstring);

#ifdef __cplusplus
}
#endif
#endif
