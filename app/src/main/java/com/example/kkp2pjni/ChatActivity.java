package com.example.kkp2pjni;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class ChatActivity<Unit> extends AppCompatActivity {
    private KKP2PEngine p2pEngine;

    // clientChannel can read and wirte data
    private KKP2PChannel clientChannel;

    // acceptChannel can read and write data too
    private ArrayList<KKP2PChannel> acceptChannelArray;
    private long p2pHandle;
    private int listenFd;
    private Button picButton;
    private Button sendButton;
    private TextView msgText;
    private RecyclerView recyclerView;
    private ArrayList<Msg> msgList;
    private MsgAdapter msgAdapter;
    private ProgressBar progressBar;

    private KKP2PConnectCtx connect_ctx;
    private boolean lanSearch;
    private Thread accept_thread;
    private boolean accept_exit;
    private Thread recv_thread;
    private SocketHandler mHandler;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // init view
        msgText = (EditText)findViewById(R.id.input_text);
        sendButton = (Button)findViewById(R.id.send);
        picButton = (Button)findViewById(R.id.pic);

        // init recycler
        recyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        msgList = new ArrayList<Msg>();
        msgAdapter = new MsgAdapter(msgList);
        recyclerView.setAdapter(msgAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        // init progressBar
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        mHandler = new SocketHandler();

        // init p2p engine
        p2pEngine = new KKP2PEngine();
        connect_ctx = new KKP2PConnectCtx();
        clientChannel = new KKP2PChannel();
        acceptChannelArray = new ArrayList<KKP2PChannel>();

        Intent intent = getIntent();
        String strCtx = intent.getStringExtra("extraConnectCtx");
        Gson gs = new Gson();
        connect_ctx = gs.fromJson(strCtx, KKP2PConnectCtx.class);
        p2pHandle = intent.getLongExtra("extraP2PHandle",0);
        listenFd = p2pEngine.nv_kkp2p_listen_fd(p2pHandle);
        lanSearch = intent.getBooleanExtra("lanSearch",false);
        Log.d("KKP2P","lanSearch:" + lanSearch);

        // set friend account
        Button firendBT = (Button) findViewById(R.id.friend);
        firendBT.setText(connect_ctx.peer_id);

        // set connect listen
        Button btConnect = findViewById(R.id.connect);
        btConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //actively establish new connections
                int result = -1;
                long startT = System.currentTimeMillis();
                if (lanSearch) {
                    result = p2pEngine.nv_kkp2p_lan_search(p2pHandle, connect_ctx, clientChannel);
                } else {
                    result = p2pEngine.nv_kkp2p_connect(p2pHandle, connect_ctx, clientChannel);
                }
                long endT = System.currentTimeMillis();
                long cost = endT-startT;

                if (result < 0) {
                    Toast toast = Toast.makeText(ChatActivity.this, "kkp2p connect error(delay:"+cost+"ms)",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    return;
                } else {
                    Toast toast = Toast.makeText(ChatActivity.this, "kkp2p connect success(delay:"+cost+"ms)",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                Button connetBT = (Button) findViewById(R.id.connect);
                if (clientChannel.transmit_mode == 1) {
                    if (clientChannel.is_ipv6_p2p == 1) {
                        connetBT.setText("p2p(ipv6)");
                    } else {
                        connetBT.setText("p2p(ipv4)");
                    }
                } else {
                    connetBT.setText("relay");
                }
                connetBT.setEnabled(false);
                // begin recv data from channel
                StartRecvThread();
            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            public void onClick(View v) {
                new AsyncTask<String, Integer, String>() {
                    protected String doInBackground(String... params) {
                        int result = 0;
                        if (getAcceptChannel() != null && getAcceptChannel().fd > 0) {
                            result = sendMsg(getAcceptChannel());
                            if (result <0) {
                                KKP2PEngine.nv_kkp2p_close_fd(getAcceptChannel().fd);
                                KKP2PEngine.nv_kkp2p_close_channel(p2pHandle, getAcceptChannel().channel_id);
                                getAcceptChannel().fd = -1;
                                getAcceptChannel().channel_id = 0;
                            }
                        } else if (clientChannel.fd > 0) {
                            result = sendMsg(clientChannel);
                            if (result < 0 ) {
                                KKP2PEngine.nv_kkp2p_close_fd(clientChannel.fd);
                                KKP2PEngine.nv_kkp2p_close_channel(p2pHandle,clientChannel.channel_id);
                                clientChannel.fd = -1;
                                clientChannel.channel_id = 0;
                            }
                        } else {
                            return null;
                        }

                        if (result < 0 ) {
                            Message msg = mHandler.obtainMessage();
                            String strDesc = "kkp2p_write error, close fd and channel";
                            msg.what = 4;
                            msg.obj = strDesc;
                            msg.sendToTarget();
                        }
                        return null;
                    }
                }.execute();
            }
        });

        // on pic button
        int REQUEST_CODE_CONTACT = 101;
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        for (String str : permissions) {
            if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
            }
        }
        ActivityResultLauncher activityResultLauncher = registerForActivityResult(
                new ResultContract(),
                result -> {
                    Toast toast = Toast.makeText(ChatActivity.this, result,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    String imagePath = result;
                    new AsyncTask<String, Integer, String>() {
                        protected String doInBackground(String... params) {
                            sendMediaFile(params[0]);
                            return null;
                        }
                    }.execute(imagePath);
                });

        picButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*;image/*");
                    activityResultLauncher.launch(intent);
                }
            });

        // begin accept new channel
        // passively wait for new connections to come in
        accept_exit = false;
        StartAcceptThread();
    }

    private KKP2PChannel getAcceptChannel() {
        int count = acceptChannelArray.size();
        if (count > 0) {
            return acceptChannelArray.get(count - 1);
        }
        return null;
    }

    protected int sendSocketMsg(KKP2PChannel channel, byte[] byteArray, int expectLen) {
        int snd = 0 ;
        int sended = 0;

        snd = p2pEngine.nv_kkp2p_write(channel.fd, byteArray, expectLen,5000);
        if (snd < 0 ) {
            return -1;
        }
        sended += snd;
        while ( sended < expectLen) {
            byte[] tmpArray = new byte[expectLen - sended];
            System.arraycopy(byteArray,sended,tmpArray,0,expectLen - sended);
            snd = p2pEngine.nv_kkp2p_write(channel.fd, tmpArray, tmpArray.length,5000);
            if (snd < 0 ) {
                return -1;
            }
            sended += snd;
        }
        return sended;
    }

    protected int recvSocketMsg(KKP2PChannel channel,byte[] byteArray, int expectLen) {
        int recv = 0 ;
        int recved = 0;

        recv = p2pEngine.nv_kkp2p_read(channel.fd, byteArray, expectLen,5000);
        if (recv < 0 ) {
            Log.e("KKP2P","nv_kkp2p_read error,error code:" + recv);
            return -1;
        }
        recved += recv;
        while (recved < expectLen) {
            byte[] tmpArray = new byte[expectLen - recved];
            recv = p2pEngine.nv_kkp2p_read(channel.fd, tmpArray, tmpArray.length,5000);
            if (recv < 0 ) {
                Log.e("KKP2P","nv_kkp2p_read error,error code:" + recv);
                return -1;
            }
            System.arraycopy(tmpArray,0,byteArray,recved,recv);
            recved += recv;
        }
        return recved;
    }

    protected int sendMsg(KKP2PChannel channel) {
        int result = 0;
        try {
            String text = msgText.getText().toString().trim();
            // the protocol format is TLV:tag,lenth,value
            // send tag is 1
            byte[] tagArray = ByteConvert.uintToBytes(1);
            result = sendSocketMsg(channel,tagArray,tagArray.length);
            if (result < 0) {
                return result;
            }
            // send length
            byte[] byteArray = text.getBytes();
            byte[] lengthArray = ByteConvert.uintToBytes(byteArray.length);
            result = sendSocketMsg(channel,lengthArray,lengthArray.length);
            if (result < 0) {
                return result;
            }

            // send value
            result = sendSocketMsg(channel,byteArray,byteArray.length);

            if (result < 0) {
                return result;
            }

            Message message = new Message();
            message.what = Msg.TYPE_SENT;
            message.obj = text;
            mHandler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected int sendMediaFile(String imagePath) {
        int result = 0;
        if (getAcceptChannel() != null && getAcceptChannel().fd > 0) {
            result = sendFile(getAcceptChannel(),imagePath);
            if (result < 0) {
                KKP2PEngine.nv_kkp2p_close_fd(getAcceptChannel().fd);
                KKP2PEngine.nv_kkp2p_close_channel(p2pHandle, getAcceptChannel().channel_id);
                getAcceptChannel().fd = -1;
                getAcceptChannel().channel_id = 0;
            }
        } else if (clientChannel.fd > 0) {
            result = sendFile(clientChannel,imagePath);
            if (result < 0 ) {
                KKP2PEngine.nv_kkp2p_close_fd(clientChannel.fd);
                KKP2PEngine.nv_kkp2p_close_channel(p2pHandle,clientChannel.channel_id);
                clientChannel.fd = -1;
                clientChannel.channel_id = 0;
            }
        }

        if (result < 0 ) {
            Message msg = mHandler.obtainMessage();
            String strDesc = "kkp2p_write error, close fd and channel";
            msg.what = 4;
            msg.obj = strDesc;
            msg.sendToTarget();
        }
        return result;
    }

    protected int sendFile(KKP2PChannel channel,String imagePath) {
        Log.d("KKP2P","begin send file:" + imagePath);
        int result = 0;
        try {
            File file = new File(imagePath);
            long fileLen = file.length();

            // the protocol format is TLV:tag,lenth,value
            // send file tag is 2
            byte[] tagArray = ByteConvert.uintToBytes(2);
            result = sendSocketMsg(channel,tagArray,tagArray.length);
            if (result < 0) {
                return result;
            }
            // send length
            byte[] lengthArray = ByteConvert.uintToBytes(fileLen);
            result = sendSocketMsg(channel,lengthArray,lengthArray.length);
            if (result < 0) {
                return result;
            }

            long startMs = System.currentTimeMillis();
            byte[] buffer = new byte[1024];
            int len = 0;
            FileInputStream inStream = new FileInputStream(file);
            int totalSended = 0 ;
            while( (len = inStream.read(buffer))!= -1) {
                result = sendSocketMsg(channel,buffer,len);
                if (result < 0) {
                    return result;
                }
                totalSended += len;

                Message message = Message.obtain();
                message.obj = (int)((long)totalSended * 100 /fileLen);
                message.what = 5;
                mHandler.sendMessage(message);
            }

            // send success
            long endMs = System.currentTimeMillis();
            Message message = Message.obtain();

            String strDesc = "send file len:"+ fileLen
                    + ",speed:" + fileLen*1000/(endMs-startMs) + " Byte/s";

            message.obj = strDesc;
            message.what = 6;
            mHandler.sendMessage(message);
            inStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void LoopReadChannel(KKP2PChannel channel) {
        // loop recv msg
        int recvLen = 0;
        while (true) {
            recvLen = recvMsgAndFile(channel);
            if (recvLen < 0) {
                String strDesc = "kkp2p_read error, close fd and channel,fd:"
                        + channel.fd + ",result:" + recvLen + ",channel hash code:"
                        + channel.hashCode();
                Log.e("KKP2P","LoopReadChannel:" + strDesc);

                // socket error
                p2pEngine.nv_kkp2p_close_channel(p2pHandle,channel.channel_id);
                p2pEngine.nv_kkp2p_close_fd(channel.fd);
                channel.fd = -1;
                channel.channel_id = 0;
                Message msg = mHandler.obtainMessage();
                msg.what = 4;
                msg.obj = strDesc;
                msg.sendToTarget();
                return;
            }
        }
    }

    protected int recvMsgAndFile(KKP2PChannel channel) {
        // recv tag
        byte[] tagArray = new byte[4];
        int result = recvSocketMsg(channel,tagArray,tagArray.length);
        if (result < 0 ) {
            return result;
        }
        long  tag = ByteConvert.bytesToUint(tagArray);

        // recv length
        byte []lenArray = new byte[4];
        result = recvSocketMsg(channel,lenArray,lenArray.length);
        if (result < 0 ) {
            return result;
        }
        long  length = ByteConvert.bytesToUint(lenArray);
        if (tag == 1) {
            // it's a text message
            byte[] textArray = new byte[(int)length];
            result = recvSocketMsg(channel,textArray,textArray.length);
            if (result < 0 ) {
                return result;
            }
            Message msg = mHandler.obtainMessage();
            String text = new String(textArray,0,result);
            msg.what = Msg.TYPE_RECEIVED;
            msg.obj = text;
            msg.sendToTarget();
        } else if (tag == 2) {
            // recv file,discard it
            Log.d("KKP2P","receive file len:"+length);
            long startMs = System.currentTimeMillis();
            int  totalReceived = 0;
            byte[] byteArray = new byte[1024];
            while(totalReceived < length) {
                int expectLen= Math.min(1024, (int)length-totalReceived);
                result = recvSocketMsg(channel,byteArray, expectLen);
                if (result < 0 ) {
                    return result;
                }
                totalReceived += result;
            }
            long endMs = System.currentTimeMillis();
            Message msg = mHandler.obtainMessage();
            String text = "recv file,len:" + totalReceived + ",speed:" +
                    (totalReceived/(endMs-startMs))*1000 + " Byte/s,discard it";
            msg.what = Msg.TYPE_RECEIVED;
            msg.obj = text;
            msg.sendToTarget();
        }
        return (int)length;
    }

    private void StartRecvThread() {
        // create accept thread to accept connect
        recv_thread = new Thread(new Runnable() {
            public void run() {
                LoopReadChannel(clientChannel);
            }
        });
        recv_thread.start();
    }

    private void StartAcceptThread() {
        // create accept thread to accept connect
        accept_thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    int result = 0;
                    KKP2PChannel acceptChannel = new KKP2PChannel();
                    while (result == 0) {
                        //5000 ms
                        result = p2pEngine.nv_kkp2p_accept(p2pHandle,1000, acceptChannel);
                        if (result <0 || accept_exit) {
                            // error
                            Log.d("KKP2P","accept thread exit");
                            return ;
                        }
                    }

                    String strLog = "nv_kkp2p_accept a new channel success,"
                            + "fd:" + acceptChannel.fd + ",transmit_mode:"
                            + acceptChannel.transmit_mode + "," + ",p2pHandle"
                            + p2pHandle;

                    Log.d("KKP2P", strLog);
                    String strDesc = new String("");
                    if (acceptChannel.transmit_mode == 1) {
                        if (acceptChannel.is_ipv6_p2p ==1 ) {
                            strDesc = "p2p(ipv6 accepted)";
                        } else {
                            strDesc = "p2p(ipv4 accepted)";
                        }
                    } else if (acceptChannel.transmit_mode == 2) {
                        strDesc = "relay(accepted)";
                    }
                    Message msg = mHandler.obtainMessage();
                    msg.what = 3;
                    msg.obj = strDesc;
                    msg.sendToTarget();
                    Thread recvThread = new Thread(new Runnable() {
                        public void run() {
                            // second, loop read from this channel
                            LoopReadChannel(acceptChannel);
                        }
                    });
                    recvThread.start();
                    // add to list
                    acceptChannelArray.add(acceptChannel);
                }
            }
        });
        accept_thread.start();
    }

    class SocketHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LinearLayoutManager manager;
            switch (msg.what) {
                case 0:
                    // recv msg
                    try {
                        String text = new String((String)msg.obj);
                        Msg chatMsg = new Msg(text, 0);
                        msgList.add(chatMsg);
                        Log.d("KKP2P","recv msg:"+text +",p2pHandle:"+ p2pHandle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    msgAdapter.notifyItemInserted(msgList.size()-1);
                    recyclerView.scrollToPosition(msgAdapter.getItemCount()-1);
                    break;
                case 1:
                    // send msg
                    try {
                        String text = new String((String)msg.obj);
                        Msg chatMsg = new Msg(text, 1);
                        msgList.add(chatMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    msgAdapter.notifyItemInserted(msgList.size()-1);
                    recyclerView.scrollToPosition(msgAdapter.getItemCount()-1);
                    msgText.setText("");
                    break;
                case 3:
                    // update button
                    try {
                        String text = new String((String)msg.obj);
                        String strLog = "accept channel type desc:" + text;
                        Button connetBT = (Button) findViewById(R.id.connect);
                        connetBT.setText(text);
                        connetBT.setEnabled(false);
                        String strToast = "accepted a new connection:" + text;
                        Toast toast = Toast.makeText(ChatActivity.this,
                                strToast, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    // notify close channel
                    try {
                        String text = new String((String)msg.obj);
                        Toast toast = Toast.makeText(ChatActivity.this,
                                text, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();

                        Button connetBT = (Button) findViewById(R.id.connect);
                        connetBT.setText("disconnect");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 5:
                    // update progress
                    try {
                        int percent = (int)msg.obj;
                        if (percent >0 && percent < 100) {
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setProgress(percent);
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 6:
                    // update progress
                    try {
                        String text = new String((String)msg.obj);
                        Msg chatMsg = new Msg(text, 1);
                        msgList.add(chatMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    msgAdapter.notifyItemInserted(msgList.size()-1);
                    recyclerView.scrollToPosition(msgAdapter.getItemCount()-1);
                    break;
                default:
                    break;
            }
        }
    }

    public class ResultContract extends ActivityResultContract<Intent, String> {
        Context myContext;

        public Intent createIntent(Context context, Intent input) {
            myContext = context;
            return new Intent(Intent.ACTION_GET_CONTENT).setType("video/*;image/*");
        }

        public String parseResult(int resultCode, Intent data) {
            Uri uri = data.getData();
            return PickUtils.getPath(myContext, uri);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accept_exit = true;
        Log.d("KKP2P","activity onDestroy");
    }

    public static String bytes2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String tmp;
        sb.append("[");
        int i = 0;
        for (byte b : bytes) {
            tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            sb.append(tmp).append(" ");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append("]");
        return sb.toString();
    }
}