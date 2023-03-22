package com.example.kkp2pjni;
import com.google.gson.Gson;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kkp2pjni.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'kkp2pjni' library on application startup.
    static {
        System.loadLibrary("kkp2pjni");
    }

    private ActivityMainBinding binding;

    private KKP2PEngine p2pEngine;
    private long p2pHandle;
    KKP2PConnectCtx connect_ctx;
    KKP2PChannel channel;

    private String src_peer_id;
    private String src_peer_key;
    private EditText login_domain;
    private EditText login_port;
    private EditText lan_port;
    private Spinner connect_mode;
    private Spinner account_info;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        intent = null;

        // assign widget
        login_domain =  (EditText)findViewById(R.id.editDomain);
        login_port = (EditText)findViewById(R.id.editPort);
        lan_port =  (EditText)findViewById(R.id.editLanPort);

        // set select connect_mode
        connect_mode = (Spinner) findViewById(R.id.spinnerMode);
        String strConnMode="auto|0,p2p|1,relay|2,lanSearch";
        String[] arrConnMode = strConnMode.split(",");

        ArrayAdapter<String> adapterMode;
        adapterMode = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, arrConnMode);
        adapterMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        connect_mode.setAdapter(adapterMode);
        connect_mode.setSelection(1);
        connect_mode.setVisibility(View.VISIBLE);

        // set select account_info
        account_info = (Spinner) findViewById(R.id.spinnerAccount);
        String strAccount="kkuai-ipc-00001|WtXmjG,kkuai-ipc-00002|OBq26M";
        String[] arrAccount = strAccount.split(",");

        ArrayAdapter<String> adapterAccount;
        adapterAccount = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, arrAccount);
        adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        account_info.setAdapter(adapterAccount);
        account_info.setVisibility(View.VISIBLE);
        p2pEngine = new KKP2PEngine();
        p2pHandle = 0 ;
        Button bt = findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                KKP2PConfig config = new KKP2PConfig();
                config.login_domain = login_domain.getText().toString();
                config.login_port =  Integer.parseInt(login_port.getText().toString());
                config.lan_search_port = Integer.parseInt(lan_port.getText().toString());
                config.log_path = null;
                config.max_log_size = 1024*1024;
                if (p2pHandle != 0) {
                    p2pEngine.nv_kkp2p_engine_destroy(p2pHandle);
                }
                p2pHandle = p2pEngine.nv_kkp2p_engine_init(config, 5000);
                if (p2pHandle == 0) {
                    Toast toast = Toast.makeText(MainActivity.this, "p2p engine init error",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    return;
                }

                // get connect param ctx
                Boolean lanSearch = new Boolean("false");
                connect_ctx = new KKP2PConnectCtx();
                connect_ctx.connect_mode = 0;

                // get connect mode
                String strConnect = connect_mode.getSelectedItem().toString();
                String[] strConnArray = strConnect.split("\\|");
                if (strConnArray.length == 2) {
                    connect_ctx.connect_mode = Integer.parseInt(strConnArray[1]);
                } else if (strConnArray[0].equals("lanSearch")) {
                    connect_ctx.connect_mode = 1;
                    lanSearch = true;
                }

                // crate a like tcp channel
                connect_ctx.channel_type = 0;

                // get src peer id and dest peerId
                String strAccount = account_info.getSelectedItem().toString();
                String[] strAccountArray = strAccount.split("\\|");
                src_peer_id =  strAccountArray[0];
                src_peer_key = strAccountArray[1];
                if (src_peer_id.equals("kkuai-ipc-00001")) {
                    connect_ctx.peer_id = "kkuai-ipc-00002";
                } else {
                    connect_ctx.peer_id = "kkuai-ipc-00001";
                }
                connect_ctx.encrypt_data = 0;
                connect_ctx.time_out = 5000;
                connect_ctx.func = null;
                connect_ctx.func_param = null;

                // join net
                int result = p2pEngine.nv_kkp2p_join_net(p2pHandle, src_peer_id, src_peer_key);
                if (result < 0) {
                    Toast.makeText(MainActivity.this, "kkp2p join net error",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                result =  p2pEngine.nv_kkp2p_join_lan(p2pHandle, src_peer_id);
                if (result < 0) {
                    Toast.makeText(MainActivity.this, "kkp2p join lan error",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // start the chat activity
                if (intent == null) {
                    intent = new Intent(MainActivity.this, ChatActivity.class);
                }

                // put data to chat activity
                Gson gs = new Gson();
                String targetCtx = gs.toJson(connect_ctx);
                intent.putExtra("extraConnectCtx",  targetCtx);
                intent.putExtra("extraP2PHandle", p2pHandle);
                intent.putExtra("lanSearch", lanSearch);

                startActivity(intent);
            }
        });
    }
}