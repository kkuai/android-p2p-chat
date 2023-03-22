package com.example.kkp2pjni;

public class KKP2PConnectCtx {
    public String peer_id;
    public int channel_type;
    public int  connect_mode;
    public int encrypt_data;
    public int time_out;
    public int connect_desc;
    KKP2PConnectCb func;
    Object func_param;
    KKP2PConnectCtx() {
        peer_id = "";
        channel_type = 0;
        connect_mode = 0;
        encrypt_data = 0;
        time_out = 0;
        func = null;
        func_param = null;
        connect_desc = 0 ;
    }
}
