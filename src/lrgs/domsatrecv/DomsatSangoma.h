/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class lrgs_domsatrecv_DomsatSangoma */

#ifndef _Included_lrgs_domsatrecv_DomsatSangoma
#define _Included_lrgs_domsatrecv_DomsatSangoma
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     lrgs_domsatrecv_DomsatSangoma
 * Method:    setInterfaceName
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_lrgs_domsatrecv_DomsatSangoma_setInterfaceName
  (JNIEnv *, jclass, jstring);

/*
 * Class:     lrgs_domsatrecv_DomsatSangoma
 * Method:    initSangoma
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_lrgs_domsatrecv_DomsatSangoma_initSangoma
  (JNIEnv *, jclass);

/*
 * Class:     lrgs_domsatrecv_DomsatSangoma
 * Method:    readPacket
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_lrgs_domsatrecv_DomsatSangoma_readPacket
  (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     lrgs_domsatrecv_DomsatSangoma
 * Method:    closeSangoma
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_lrgs_domsatrecv_DomsatSangoma_closeSangoma
  (JNIEnv *, jclass);

/*
 * Class:     lrgs_domsatrecv_DomsatSangoma
 * Method:    enable
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_lrgs_domsatrecv_DomsatSangoma_enable
  (JNIEnv *, jclass);

/*
 * Class:     lrgs_domsatrecv_DomsatSangoma
 * Method:    disable
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_lrgs_domsatrecv_DomsatSangoma_disable
  (JNIEnv *, jclass);

/*
 * Class:     lrgs_domsatrecv_DomsatSangoma
 * Method:    getErrorMsg
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_lrgs_domsatrecv_DomsatSangoma_getErrorMsg
  (JNIEnv *, jclass, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
