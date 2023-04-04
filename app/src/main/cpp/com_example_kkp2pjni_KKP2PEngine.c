#include "com_example_kkp2pjni_KKP2PEngine.h"
#include <android/log.h>
#include <stdlib.h>
#include <string.h>

// download url: https://kkuai.com/download/sdk/detail?platform=sdk_android
#include "kkp2p_sdk.h"
JNIEXPORT jlong JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1engine_1init
  (JNIEnv * env, jclass jniClass, jobject config, jint timeout) {
    kkp2p_engine_conf_t engineConf;
    memset(&engineConf, 0, sizeof(kkp2p_engine_conf_t));
    jclass jcls =(*env)->GetObjectClass(env, config);

    // get login domain
    jfieldID fid = (*env)->GetFieldID(env,jcls, "login_domain", "Ljava/lang/String;");
    jstring jstrDomain = (jstring)((*env)->GetObjectField(env,config, fid));
    const char* szDomain = (*env)->GetStringUTFChars(env,jstrDomain, 0);
    jsize  domainLen = (*env)->GetStringUTFLength(env,jstrDomain);
    engineConf.login_domain = (char*)calloc(1, domainLen+1);
    memcpy(engineConf.login_domain, szDomain, domainLen);

    // get login_port
    fid = (*env)->GetFieldID(env,jcls, "login_port", "I");
    jint jLoginPort = (*env)->GetIntField(env,config, fid);
    engineConf.login_port = jLoginPort;

    // lan search port
    fid = (*env)->GetFieldID(env,jcls, "lan_search_port", "I");
    jint jLanPort = (*env)->GetIntField(env,config, fid);
    engineConf.lan_search_port = jLanPort;

    // get log path
    fid = (*env)->GetFieldID(env,jcls, "log_path", "Ljava/lang/String;");
    jstring jstrPath = (jstring)((*env)->GetObjectField(env,config, fid));
    if (jstrPath) {
        const char* szPath = (*env)->GetStringUTFChars(env,jstrPath, 0);
        jsize pathLen = (*env)->GetStringUTFLength(env, jstrDomain);
        engineConf.log_path = (char *) calloc(1, pathLen + 1);
        memcpy(engineConf.log_path, szPath, pathLen);
    }

    // log size
    fid = (*env)->GetFieldID(env,jcls, "max_log_size", "I");
    jint jLogSize = (*env)->GetIntField(env,config, fid);
    engineConf.max_log_size = jLogSize;

    // call kkp2p_engine_init
    kkp2p_engine_t* p2pEngine = kkp2p_engine_init(&engineConf, timeout);
    free(engineConf.login_domain);
    (*env)->DeleteLocalRef(env,jcls);

    if (engineConf.log_path) {
        free(engineConf.log_path);
    }

    return (jlong)p2pEngine;
}

JNIEXPORT void JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1engine_1destroy
  (JNIEnv * env , jclass jniClass, jlong engine) {
    if (engine != 0) {
        kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
        kkp2p_engine_destroy(p2pEngine);
    }
}

JNIEXPORT void JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1switch_1log_1level
  (JNIEnv * env, jclass jniClass, jlong engine, jint level) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    kkp2p_switch_log_level(p2pEngine, level);
}

JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1get_1domainip
  (JNIEnv * env, jclass jniClass, jlong engine, jobjectArray jniArray) {
    // to do?
    /*
    int i, len = 0;
    int array_len = (env)->GetArrayLength(env, strs);
    for (i=0;i<array_len;i++)
    {
        jobject obj=(env)->GetObjectArrayElement(env, strs, i);
        len+=(env)->GetStringUTFLength(env, (jstring) obj);
    }
    */
    return 0;
}

JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1join_1net
  (JNIEnv * env, jclass jniClass, jlong engine, jstring peerId, jstring peerKey) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    const char* szPeerId = ((*env))->GetStringUTFChars(env,peerId, 0);
    const char* szPeerKey = ((*env))->GetStringUTFChars(env,peerKey, 0);
    return kkp2p_join_net(p2pEngine, (char*)szPeerId, (char*)szPeerKey);
}

JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1join_1lan
  (JNIEnv * env, jclass jniClas, jlong engine, jstring peerId) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    const char* szPeerId = ((*env))->GetStringUTFChars(env,peerId, 0);
    return kkp2p_join_lan(p2pEngine, (char*)szPeerId);
}

JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1listen_1fd
  (JNIEnv * env, jclass jniClass, jlong engine) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    return kkp2p_listen_fd(p2pEngine);
}

JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1accept
  (JNIEnv * env, jclass jniClass, jlong engine, jint timeout, jobject jChannel) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    kkp2p_channel_t channel;
    memset(&channel, 0 ,sizeof(kkp2p_channel_t));
    jint result =  kkp2p_accept(p2pEngine, (int)timeout, &channel);

    // set value
    jclass jcls = ((*env))->GetObjectClass(env,jChannel);
    jfieldID fid = (*env)->GetFieldID(env,jcls, "peer_id", "Ljava/lang/String;");
    jstring jstr = (jstring)((*env)->GetObjectField(env,jChannel, fid));
    const char *str = (*env)->GetStringUTFChars(env,jstr, NULL);
    (*env)->ReleaseStringUTFChars(env,jstr, str);
    jstr = (*env)->NewStringUTF(env,channel.peer_id);
    (*env)->SetObjectField(env,jChannel, fid, jstr);

    fid = (*env)->GetFieldID(env,jcls, "channel_type", "I");
    (*env)->SetIntField(env,jChannel,fid,channel.channel_type);

    fid = (*env)->GetFieldID(env,jcls, "transmit_mode", "I");
    (*env)->SetIntField(env,jChannel,fid,channel.transmit_mode);

    fid = (*env)->GetFieldID(env,jcls, "encrypt_data", "I");
    (*env)->SetIntField(env,jChannel,fid,channel.encrypt_data);

    fid = (*env)->GetFieldID(env,jcls, "channel_id", "J");
    (*env)->SetLongField(env,jChannel,fid,(long)channel.channel_id);

    fid = (*env)->GetFieldID(env,jcls, "fd", "I");
    (*env)->SetIntField(env,jChannel,fid,channel.fd);

    (*env)->DeleteLocalRef(env,jcls);

    return result;
}

JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1connect
  (JNIEnv * env, jclass jniClass, jlong engine, jobject connCtx, jobject jchannel) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    kkp2p_connect_ctx_t ctx;
    memset(&ctx, 0, sizeof(kkp2p_connect_ctx_t));
    kkp2p_channel_t channel;
    memset(&channel, 0, sizeof(kkp2p_channel_t));

    jclass jclsCtx = (*env)->GetObjectClass(env,connCtx);
    jfieldID fid = (*env)->GetFieldID(env,jclsCtx, "peer_id", "Ljava/lang/String;");
    jstring jstrPeerId = (jstring)((*env)->GetObjectField(env,connCtx, fid));
    const char* szPeerId = (*env)->GetStringUTFChars(env,jstrPeerId, 0);
    strncpy(ctx.peer_id, szPeerId, sizeof(ctx.peer_id));

    fid = (*env)->GetFieldID(env,jclsCtx, "channel_type", "I");
    ctx.channel_type =  (*env)->GetIntField(env,connCtx, fid);

    fid = (*env)->GetFieldID(env,jclsCtx, "connect_mode", "I");
    ctx.connect_mode =  (*env)->GetIntField(env,connCtx, fid);

    fid = (*env)->GetFieldID(env,jclsCtx, "encrypt_data", "I");
    ctx.encrypt_data =  (*env)->GetIntField(env,connCtx, fid);

    fid = (*env)->GetFieldID(env,jclsCtx, "time_out", "I");
    ctx.timeout =  (*env)->GetIntField(env,connCtx, fid);

    // to do ?
    ctx.func = NULL;
    ctx.func_param = NULL;

    kkp2p_switch_log_level(p2pEngine,4);
    jint result = kkp2p_connect(p2pEngine, &ctx, &channel);

    // set channel
    jclass jcls = (*env)->GetObjectClass(env,jchannel);

    fid = (*env)->GetFieldID(env,jcls, "peer_id", "Ljava/lang/String;");
    jstring jstr = (jstring)((*env)->GetObjectField(env,jchannel, fid));
    const char *str = (*env)->GetStringUTFChars(env,jstr, NULL);
    (*env)->ReleaseStringUTFChars(env,jstr, str);
    jstr = (*env)->NewStringUTF(env,ctx.peer_id);
    (*env)->SetObjectField(env,jchannel, fid, jstr);

    fid = (*env)->GetFieldID(env,jcls, "transmit_mode", "I");
    (*env)->SetIntField(env,jchannel,fid,channel.transmit_mode);

    fid = (*env)->GetFieldID(env,jcls, "encrypt_data", "I");
    (*env)->SetIntField(env,jchannel,fid,channel.encrypt_data);

    fid = (*env)->GetFieldID(env,jcls, "channel_id", "J");
    (*env)->SetLongField(env,jchannel,fid,(long)channel.channel_id);

    fid = (*env)->GetFieldID(env,jcls, "fd", "I");
    (*env)->SetIntField(env,jchannel,fid,channel.fd);

    (*env)->DeleteLocalRef(env,jcls);
    (*env)->DeleteLocalRef(env,jclsCtx);

   return result;
}

JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1lan_1search
  (JNIEnv * env, jclass jniClass, jlong engine, jobject connCtx, jobject jchannel) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    kkp2p_connect_ctx_t ctx;
    memset(&ctx, 0, sizeof(kkp2p_connect_ctx_t));
    kkp2p_channel_t channel;
    memset(&channel, 0, sizeof(kkp2p_channel_t));
    
    jclass jclsCtx = (*env)->GetObjectClass(env,connCtx);
    jfieldID fid = (*env)->GetFieldID(env,jclsCtx, "peer_id", "Ljava/lang/String;");
    jstring jstrPeerId = (jstring)((*env)->GetObjectField(env,connCtx, fid));
    const char* szPeerId = (*env)->GetStringUTFChars(env,jstrPeerId, 0);
    strncpy(ctx.peer_id, szPeerId, sizeof(ctx.peer_id));
    
    fid = (*env)->GetFieldID(env,jclsCtx, "connect_mode", "I");
    ctx.connect_mode =  (*env)->GetIntField(env,connCtx, fid);
    
    fid = (*env)->GetFieldID(env,jclsCtx, "encrypt_data", "I");
    ctx.encrypt_data =  (*env)->GetIntField(env,connCtx, fid);
    
    fid = (*env)->GetFieldID(env,jclsCtx, "time_out", "I");
    ctx.timeout =  (*env)->GetIntField(env,connCtx, fid);
    ctx.func = NULL;
    ctx.func_param = NULL;
    
    jint result = kkp2p_lan_search(p2pEngine, &ctx, &channel);
    
    // set channel
    jclass jcls = (*env)->GetObjectClass(env,jchannel);
    
    fid = (*env)->GetFieldID(env,jcls, "peer_id", "Ljava/lang/String;");
    jstring jstr = (jstring)((*env)->GetObjectField(env,jchannel, fid));
    const char *str = (*env)->GetStringUTFChars(env,jstr, NULL);
    (*env)->ReleaseStringUTFChars(env,jstr, str);
    jstr = (*env)->NewStringUTF(env,ctx.peer_id);
    (*env)->SetObjectField(env,jchannel, fid, jstr);

    fid = (*env)->GetFieldID(env,jcls, "transmit_mode", "I");
    (*env)->SetIntField(env,jchannel,fid,channel.transmit_mode);
    
    fid = (*env)->GetFieldID(env,jcls, "encrypt_data", "I");
    (*env)->SetIntField(env,jchannel,fid,channel.encrypt_data);
    
    fid = (*env)->GetFieldID(env,jcls, "channel_id", "J");
    (*env)->SetLongField(env,jchannel,fid,(long)channel.channel_id);

    fid = (*env)->GetFieldID(env,jcls, "fd", "I");
    (*env)->SetIntField(env,jchannel,fid,channel.fd);

    (*env)->DeleteLocalRef(env,jcls);
    (*env)->DeleteLocalRef(env,jclsCtx);

    return result;
}

JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1read
  (JNIEnv * env, jclass jniClass, jint fd, jbyteArray buff, jint buffLen, jint timeout) {
    char* szBuff= (char*)(*env)->GetByteArrayElements(env,buff, NULL);
    jint result = kkp2p_read(fd, szBuff, (int)buffLen, timeout);
    //__android_log_print(ANDROID_LOG_ERROR, "MYSPP2P", "jni kkp2p_1read read:%s", szBuff);
    (*env)->ReleaseByteArrayElements(env, buff, szBuff,0);
    return result;
}

JNIEXPORT jint JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1write
  (JNIEnv * env, jclass jniClass, jint fd, jbyteArray buff, jint buffLen, jint timeout) {
    char* szBuff= (char*)(*env)->GetByteArrayElements(env,buff, NULL);
    jint result =  (jint)kkp2p_write(fd, szBuff, (int)buffLen, timeout);
    (*env)->ReleaseByteArrayElements(env, buff,szBuff,0);
    return result;

}

JNIEXPORT void JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1close_1channel
  (JNIEnv * env, jclass jniClass, jlong engine, jlong channelId) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    return kkp2p_close_channel(p2pEngine, (uint32_t)channelId);
}

JNIEXPORT void JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1close_1fd
  (JNIEnv * env, jclass jniClass, jint fd) {
    return kkp2p_close_fd(fd);
}

JNIEXPORT jlong JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1start_1proxy
(JNIEnv * env, jclass jniClass, jlong engine, jstring ip, jint port, jobject connCtx) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    kkp2p_connect_ctx_t ctx;
    memset(&ctx, 0, sizeof(kkp2p_connect_ctx_t));

    jclass jclsCtx = (*env)->GetObjectClass(env,connCtx);
    jfieldID fid = (*env)->GetFieldID(env,jclsCtx, "peer_id", "Ljava/lang/String;");
    jstring jstrPeerId = (jstring)((*env)->GetObjectField(env,connCtx, fid));
    const char* szPeerId = (*env)->GetStringUTFChars(env,jstrPeerId, 0);
    strncpy(ctx.peer_id, szPeerId, sizeof(ctx.peer_id));

    fid = (*env)->GetFieldID(env,jclsCtx, "connect_mode", "I");
    ctx.connect_mode =  (*env)->GetIntField(env,connCtx, fid);

    fid = (*env)->GetFieldID(env,jclsCtx, "encrypt_data", "I");
    ctx.encrypt_data =  (*env)->GetIntField(env,connCtx, fid);

    fid = (*env)->GetFieldID(env,jclsCtx, "time_out", "I");
    ctx.timeout =  (*env)->GetIntField(env,connCtx, fid);

    fid = (*env)->GetFieldID(env,jclsCtx, "connect_desc", "I");
    ctx.connect_desc =  (*env)->GetIntField(env,connCtx, fid);

    ctx.func = NULL;
    ctx.func_param = NULL;

    const char* proxyIp = ((*env))->GetStringUTFChars(env,ip, 0);
    unsigned short proxyPort = port;
    unsigned int proxyId = 0 ;

    int result = kkp2p_start_proxy(p2pEngine, proxyIp, proxyPort, &ctx,&proxyId);
    __android_log_print(ANDROID_LOG_ERROR, "KKP2P", "kkp2p_start_proxy %s:%d %s,%d,%d,proxy id:%u", proxyIp,proxyPort,ctx.peer_id,ctx.timeout,ctx.connect_mode,proxyId);

    (*env)->DeleteLocalRef(env,jclsCtx);
    if (proxyId > 0) {
        return proxyId;
    }
    return 0;
}

JNIEXPORT void JNICALL Java_com_example_kkp2pjni_KKP2PEngine_nv_1kkp2p_1stop_1proxy
        (JNIEnv * env, jclass jniClass, jlong engine, jlong proxyId) {
    kkp2p_engine_t* p2pEngine = (kkp2p_engine_t*)(engine);
    kkp2p_stop_proxy(p2pEngine,proxyId);
}
