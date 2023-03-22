package com.example.kkp2pjni;

public class KKP2PChannel {
    public String peer_id;
    public int channel_type;
    public int  transmit_mode;
    public int encrypt_data;
    public long channel_id;
    int fd;
    KKP2PChannel() {
        peer_id = "";
        channel_type = 0;
        transmit_mode = 0 ;
        encrypt_data = 0 ;
        channel_id = 0 ;
        fd = 0;
    }
}
