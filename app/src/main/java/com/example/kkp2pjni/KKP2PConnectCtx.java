package com.example.kkp2pjni;

public class KKP2PConnectCtx {
    public String peer_id;
    public int  connect_mode;
    public int encrypt_data;
    public int time_out;
    KKP2PConnectCb func;
    Object func_param;
    KKP2PConnectCtx() {
        peer_id = "";
        connect_mode = 0;
        encrypt_data = 0;
        time_out = 0;
        func = null;
        func_param = null;
    }
}
