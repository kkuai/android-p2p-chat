package com.example.kkp2pjni;

import java.nio.ByteBuffer;

public class KKP2PEngine {
    static {
        System.loadLibrary("kkp2pjni");
    }

    /*
    long engine;

    KKP2PEngine(){
        engine = 0;
    }

    protected void finalize() {
        if (engine > 0) {
            nv_kkp2p_engine_destroy(engine);
        }
    }

    public long kkp2p_engine_init(KKP2PConfig config, int timeOut) {
        long tmpEngine = nv_kkp2p_engine_init(config, timeOut);
        if (tmpEngine > 0) {
            engine = tmpEngine;
        }
        return tmpEngine;
    }
    public void kkp2p_engine_destroy() {
        if (engine > 0) {
            nv_kkp2p_engine_destroy(engine);
            engine = 0;
        }
    }

    public void kkp2p_switch_log_level(int level) {
        nv_kkp2p_switch_log_level(engine, level);
    }

    public int kkp2p_get_domainip(String[] arr) {
        return nv_kkp2p_get_domainip(engine, arr);
    }

    public int kkp2p_join_net(String peerId, String secret) {
        return nv_kkp2p_join_net(engine, peerId, secret);
    }

    public int kkp2p_join_lan(String peerId) {
        return nv_kkp2p_join_lan(engine, peerId);
    }

    public int kkp2p_listen_fd() {
        return nv_kkp2p_listen_fd(engine);
    }

    public int kkp2p_accept(int listenFd, int timeout, KKP2PChannel channel) {
        return nv_kkp2p_accept(engine, listenFd, timeout, channel);
    }

    public int kkp2p_connect(KKP2PConnectCtx ctx, KKP2PChannel channel) {
        return nv_kkp2p_connect(engine, ctx, channel);
    }

    public int kkp2p_lan_search(KKP2PConnectCtx ctx, KKP2PChannel channel) {
        return nv_kkp2p_lan_search(engine, ctx, channel);
    }

    public int kkp2p_read(int fd, byte[] buff, int len, int timeout) {
        return nv_kkp2p_read(fd, buff, len, timeout);
    }

    public int kkp2p_write(int fd, byte[] buff,int len, int timeout) {
        return nv_kkp2p_write(fd, buff, len, timeout);
    }

    public void kkp2p_close_channel(long channel_id) {
        nv_kkp2p_close_channel(engine, channel_id);
    }

    public int kkp2p_close_fd(int fd) {
        return nv_kkp2p_close_fd(fd);
    }
    */

    public static native long nv_kkp2p_engine_init(KKP2PConfig config, int timeOut);
    public static native void nv_kkp2p_engine_destroy(long obj);
    public static native void nv_kkp2p_switch_log_level(long obj, int level);
    public static native int nv_kkp2p_get_domainip(long obj, String[] arr);
    public static native int nv_kkp2p_join_net(long obj, String peerId, String secret);
    public static native int nv_kkp2p_join_lan(long obj, String peerId);
    public static native int nv_kkp2p_listen_fd(long obj);
    public static native int nv_kkp2p_accept(long engine, int timeout, KKP2PChannel channel);
    public static native int nv_kkp2p_connect(long engine, KKP2PConnectCtx ctx, KKP2PChannel channel);
    public static native int nv_kkp2p_lan_search(long engine, KKP2PConnectCtx ctx, KKP2PChannel channel);
    public static native int nv_kkp2p_read(int fd, byte[] buff, int len, int timeout);
    public static native int nv_kkp2p_write(int fd, byte[] buff, int len, int timeout);
    public static native void nv_kkp2p_close_channel(long engine, long channel_id);
    public static native void nv_kkp2p_close_fd(int fd);
}
