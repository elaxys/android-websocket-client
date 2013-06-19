package com.elaxys.android.wsclient_test;

import java.util.Arrays;
import java.util.Random;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.elaxys.android.wsclient_test.R;
import com.elaxys.android.websocket.Client;
import com.elaxys.android.websocket.Client.Error;
import com.elaxys.android.websocket.Client.Stats;


public class MainActivity extends ListActivity implements
    OnSharedPreferenceChangeListener, Client.Listener {

    static final String LOGTAG = "WSAPP";
    static final String[] MENU = new String[] {
        "Start",
        "Stop",
        "Ping Test",
        "String Test",
        "Binary Test",
        "String Frag Test",
        "Binary Frag Test",
        "Clear Stats",
        "Clear TxQueue",
        "Preferences",
    };
    static final int M_START            = 0;
    static final int M_STOP             = 1;
    static final int M_PING_TEST        = 2;
    static final int M_STRING_TEST      = 3;
    static final int M_BIN_TEST         = 4;
    static final int M_STRING_FRAG_TEST = 5;
    static final int M_BINARY_FRAG_TEST = 6;
    static final int M_CLEAR_STATS      = 7;
    static final int M_CLEAR_TXQUEUE    = 8;
    static final int M_PREFS            = 9;
    static final String CHARS = 
            "ABCDEFGHIJKLMNOPQRXTUVWXZabcdefghijklmnopqrstuvwxyz" +
            "ÁÉÍÓÚáéíóúÄËÏÖÚÃẼĨÕŨãẽĩõũÀÈÌÒÙàèìòù" + 
            "\u03C0";
    static final int T_NONE             = 0;
    static final int T_PING             = 1;
    static final int T_STRING           = 2;
    static final int T_BINARY           = 3;
    static final int T_STRING_FRAG      = 4;
    static final int T_BINARY_FRAG      = 5;
    
    private TextView        mStatusText;
    private TextView        mStatusDetail;
    private ArrayAdapter<String> mAdapter;
    private boolean         mConfigChanged;
    private Client          mSocket;
    private int             mTest;
    private int             mTestCount;
    private int             mTestPayloadSize;
    private int             mTestCounter;
    private byte[]          mBytesSent;
    private String          mStringSent;
    private Random mRandom  = new Random();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        setContentView(R.layout.activity_main);
        mStatusText   = (TextView)findViewById(R.id.main_status_text);
        mStatusDetail = (TextView)findViewById(R.id.main_status_detail);
        mStatusText.setText("STOPPED");
    
        // Creates ArrayAdapter with the menu options
        mAdapter = new ArrayAdapter<String>(this, R.layout.menu_item, MENU);
        setListAdapter(mAdapter);
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);     
        mSocket = null;
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.stop();
        }
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent;
        switch ((int)id)  {
        case M_START:
            mTest = T_NONE;
            if (mSocket == null || mConfigChanged) {
                mSocket = createClient();
                if (mSocket == null) {
                    break;
                }
            }
            else
            if (mSocket.getStatus() != Client.ST_STOPPED) {
                break;
            }
            mSocket.start();
            break;
        case M_STOP:
            if (mSocket == null) {
                break;
            }
            mSocket.stop();
            break;
        case M_PING_TEST:
            if (mSocket == null || mTest != T_NONE) {
                break;
            }
            testPing();
            break;
        case M_STRING_TEST:
            if (mSocket == null || mTest != T_NONE) {
                break;
            }
            testString();
            break;
        case M_BIN_TEST:
            if (mSocket == null || mTest != T_NONE) {
                break;
            }
            testBinary();
            break;
        case M_STRING_FRAG_TEST:
            if (mSocket == null || mTest != T_NONE) {
                break;
            }
            testStringFrag();
            break;
        case M_BINARY_FRAG_TEST:
            if (mSocket == null || mTest != T_NONE) {
                break;
            }
            testBinaryFrag();
            break;
        case M_CLEAR_STATS:
            if (mSocket == null) {
                break;
            }
            mSocket.clearStats();
            updateStatus();
            break;
        case M_CLEAR_TXQUEUE:
            if (mSocket == null) {
                break;
            }
            mSocket.clearTx();
            updateStatus();
            break;
        case M_PREFS:
            intent = new Intent(this, PrefsActivity.class);
            startActivity(intent);
            break;
        }
    }

    
    private Client createClient() {
        mConfigChanged = false;
        mTestCount = PrefsActivity.getTestCount(this);
        mTestPayloadSize = PrefsActivity.getTestPayloadSize(this);
        Client.Config config = new Client.Config();
        config.mURI           = PrefsActivity.getServerURI(this);
        config.mQueueSize     = 10;
        config.mConnTimeout   = PrefsActivity.getConnTimeout(this);
        config.mRetryInterval = PrefsActivity.getRetryInterval(this);
        config.mMaxRxSize     = PrefsActivity.getMaxRxSize(this);
        config.mRespondPing   = PrefsActivity.getRespondPing(this);
        config.mServerCert    = PrefsActivity.getServerCert(this);
        config.mLogTag        = "WSCLIENT";
        try {
            return new Client(config, this);
        } catch (Error e) {
            mStatusDetail.setText(e.getMessage());
            return null;
        }
    }
    
    
    private void testPing() {
        mTest = T_PING;
        mTestCounter = 0;
        nextPing();
    }

    
    private void nextPing() {
        if (mTestCounter == mTestCount) {
            mTest = T_NONE;
            return;
        }
        mBytesSent = genBytes(125);
        try {
            mSocket.ping(mBytesSent);
        } catch (Error e) {
            e.printStackTrace();
        }
        mTestCounter++;
    }


    private void testString() {
        mTest = T_STRING;
        mTestCounter = 0;
        nextString();
    }
    

    private void nextString() {
        if (mTestCounter == mTestCount) {
            mTest = T_NONE;
            return;
        }
        mStringSent = genString(mTestPayloadSize);
        try {
            mSocket.send(mStringSent);
        } catch (Error e) {
            mStatusDetail.setText(e.toString());
            mSocket.stop();
        }
        mTestCounter++;
    }
    
    
    private void testBinary() {
        mTest = T_BINARY;
        mTestCounter = 0;
        nextBinary();
    }

    
    private void nextBinary() {
        if (mTestCounter == mTestCount) {
            mTest = T_NONE;
            return;
        }
        mBytesSent = genBytes(mTestPayloadSize);
        try {
            mSocket.send(mBytesSent);
        } catch (Error e) {
            mStatusDetail.setText(e.toString());
            mSocket.stop();
        }
        mTestCounter++;
    }

    
    private void testStringFrag() {
        mTest = T_STRING_FRAG;
        mTestCounter = 0;
        nextStringFrag();
    }
    
    private void nextStringFrag() {
        if (mTestCounter == mTestCount) {
            mTest = T_NONE;
            return;
        }
        try {
            mStringSent = genString(mTestPayloadSize);
            int nfrags  = 5;
            int fragsize = mStringSent.length() / nfrags;
            int start;
            int end;
            for (int i = 0; i < nfrags; i++) {
                start = fragsize * i; end = start + fragsize;
                if (i == 0) {
                    mSocket.sendFirst(mStringSent.substring(start, end));
                    continue;
                }
                if (i < nfrags - 1) {
                    mSocket.sendNext(mStringSent.substring(start, end));
                    continue;
                }
                mSocket.sendLast(mStringSent.substring(start));
            }
        } catch (Error e) {
            mStatusDetail.setText(e.toString());
            mSocket.stop();
        }
        mTestCounter++;
    }
    
    private void testBinaryFrag() {
        mTest = T_BINARY_FRAG;
        mTestCounter = 0;
        nextBinaryFrag();
    }
    
    private void nextBinaryFrag() {
        if (mTestCounter == mTestCount) {
            mTest = T_NONE;
            return;
        }
        try {
            mBytesSent = genBytes(mTestPayloadSize);
            int nfrags  = 5;
            int fragsize = mBytesSent.length / nfrags;
            int start;
            int end;
            for (int i = 0; i < nfrags; i++) {
                start = fragsize * i; end = start + fragsize;
                if (i == 0) {
                    mSocket.sendFirst(CopyOfRange(mBytesSent, start, end));
                    continue;
                }
                if (i < nfrags - 1) {
                    mSocket.sendNext(CopyOfRange(mBytesSent, start, end));
                    continue;
                }
                mSocket.sendLast(CopyOfRange(mBytesSent, start, mBytesSent.length));
            }
        } catch (Error e) {
            mStatusDetail.setText(e.toString());
            mSocket.stop();
        }
        mTestCounter++;
    }

    
    private byte[] genBytes(int max) {
        byte[] data;
        int size;
        
        size = mRandom.nextInt(max);
        data = new byte[size];
        mRandom.nextBytes(data);
        return data;
    }

    
    private String genString(int max) {
        int size;
        StringBuilder sb;
        
        size = mRandom.nextInt(max);
        sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(CHARS.charAt(mRandom.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
    
    private static byte[] CopyOfRange(byte[] from, int start, int end) {
        int length = end - start;
        byte[] result = new byte[length];
        System.arraycopy(from, start, result, 0, length);
        return result;
    }
    
    
    private void updateStatus() {
        String text;
        Stats stats;
    
        stats = new Stats();
        mSocket.getStats(stats);
        text = String.format("TX: %d (%d / %d / %d)\nRX: %d (%d / %d)",
                stats.mTxFrames,
                stats.mTxBytes,
                stats.mTxData,
                stats.mInQueue,
                stats.mRxFrames,
                stats.mRxBytes,
                stats.mRxData
        );
        mStatusDetail.setText(text);
        
    }

    
    @Override
    public void onClientStart() {
        mStatusText.setText("STARTED");
    }
    
    
    @Override
    public void onClientConnect() {
        mStatusText.setText("CONNECTING...");
    }

    
    @Override
    public void onClientConnected() {
        mStatusText.setText("CONNECTED");
        updateStatus();
    }


    @Override
    public void onClientError(int code, String msg) {
        mStatusText.setText(String.format("ERROR (%d)", code));
        mStatusDetail.setText(msg);
    }

    
    @Override
    public void onClientRecv(int type, String data) {
        updateStatus();
        if (mTest == T_STRING) {
            if (type == Client.F_TEXT) {
                if (!data.equals(mStringSent)) {
                    mStatusDetail.setText("Received Packet Different");
                    mSocket.stop();
                }
                nextString();
                return;
            }
            return;
        }
        if (mTest == T_STRING_FRAG) {
            if (type == Client.F_TEXT) {
                if (!data.equals(mStringSent)) {
                    mStatusDetail.setText("Received Packet Different");
                    mSocket.stop();
                }
                nextStringFrag();
                return;
            }
            return;
        }
    }

    
    @Override
    public void onClientRecv(int type, byte[] data) {
        updateStatus();
        if (mTest == T_PING) {
            if (type == Client.F_PONG) {
                nextPing();
                return;
            }
            mStatusDetail.setText("Unexpected Response");
            mSocket.stop();
            return;
        }
        if (mTest == T_BINARY) {
            if (type == Client.F_BINARY) {
                if (!Arrays.equals(data, mBytesSent)) {
                    mStatusDetail.setText("Received Packet Different");
                    mSocket.stop();
                }
                nextBinary();
                return;
            }
            mStatusDetail.setText("Unexpected Response");
            mSocket.stop();
            return;
        }
        if (mTest == T_BINARY_FRAG) {
            if (type == Client.F_BINARY) {
                if (!Arrays.equals(data, mBytesSent)) {
                    mStatusDetail.setText("Received Packet Different");
                    mSocket.stop();
                }
                nextBinaryFrag();
                return;
            }
            mStatusDetail.setText("Unexpected Response");
            mSocket.stop();
            return;
        }
    }
    
    @Override
    public void onClientSent(int fid) {
        updateStatus();
    }
    
    
    @Override
    public void onClientStop() {
        mStatusText.setText("STOPPED");
        mSocket.clearTx();
    }
    
    private void logDebug(String format, Object... args) {
        Log.println(Log.DEBUG, LOGTAG, String.format(format, args));
    }

    public void logError(String format, Object... args) {
        Log.println(Log.ERROR, LOGTAG, String.format(format, args));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mConfigChanged = true;
    }
}
