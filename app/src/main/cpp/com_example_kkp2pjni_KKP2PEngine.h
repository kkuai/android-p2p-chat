/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_example_kkp2pjni_KKP2PEngine */

#ifndef _Included_com_example_kkp2pjni_KKP2PEngine
#define _Included_com_example_kkp2pjni_KKP2PEngine
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_engine_init
 * Signature: (Lcom/example/kkp2pjni/KKP2PConfig;I)J
 */
JNIEXPORT jlong JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1engine_1init
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_engine_destroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1engine_1destroy
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_switch_log_level
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1switch_1log_1level
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_get_domainip
 * Signature: (J[Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1get_1domainip
  (JNIEnv *, jclass, jlong, jobjectArray);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_join_net
 * Signature: (JLjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1join_1net
  (JNIEnv *, jclass, jlong, jstring, jstring);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_join_lan
 * Signature: (JLjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1join_1lan
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_listen_fd
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1listen_1fd
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_accept
 * Signature: (JIILcom/example/kkp2pjni/KKP2PChannel;)I
 */
JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1accept
  (JNIEnv *, jclass, jlong, jint, jobject);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_connect
 * Signature: (JLcom/example/kkp2pjni/KKP2PConnectCtx;Lcom/example/kkp2pjni/KKP2PChannel;)I
 */
JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1connect
  (JNIEnv *, jclass, jlong, jobject, jobject);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_lan_search
 * Signature: (JLcom/example/kkp2pjni/KKP2PConnectCtx;Lcom/example/kkp2pjni/KKP2PChannel;)I
 */
JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1lan_1search
  (JNIEnv *, jclass, jlong, jobject, jobject);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_read
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1read
  (JNIEnv *, jclass, jint, jbyteArray , jint, jint);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_write
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1write
  (JNIEnv *, jclass, jint, jbyteArray, jint, jint);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_close_channel
 * Signature: (JJ)I
 */
JNIEXPORT void JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1close_1channel
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     com_example_kkp2pjni_KKP2PEngine
 * Method:    nv_kkp2p_close_fd
 * Signature: (I)I
 */
JNIEXPORT void JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1close_1fd
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif