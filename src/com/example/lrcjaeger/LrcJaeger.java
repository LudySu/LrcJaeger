package com.example.lrcjaeger;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class LrcJaeger extends Activity {
    private static final String TAG = "LrcJaeger";
    HandlerThread mHandlerThread = new HandlerThread("network");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lrc_jaeger);
        
        mHandlerThread.start();
        LrcHandler handler = new LrcHandler(mHandlerThread.getLooper());
        
        handler.sendEmptyMessage(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_lrc_jaeger, menu);
        return true;
    }
    
    
    private class  LrcHandler extends Handler {
        public LrcHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
            ArrayList<QueryResult> lrcs = TTDownloader.query("alan", "Diamond");
            for (QueryResult item : lrcs) {
                String result = TTDownloader.download(item);
                Log.d(TAG, "result is " + result);
                Log.v(TAG, "==================================================");
            }
        }
    };

}
