package com.example.lrcjaeger;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

public class LrcJaeger extends Activity {
    private static final String TAG = "LrcJaeger";
    private static final String[] PROJECTION = new String[] {"_id", "_data", "artist", "title"};
    
    private static final int MSG_QUERY_DB = 1;
    private static final int MSG_DOWNLOAD_ALL = 2;
    private static final int MSG_UPDATE_LRC_ICON = 3;
    
    private HandlerThread mLrcHandlerThread = new HandlerThread("network");
    private LrcHandler mLrcHandler;
    private ListView mListView;
    private SongItemAdapter mAdapter;
    
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_QUERY_DB:
                // update song listview
                mAdapter.clear();
                Cursor c = null;
                try {
                    c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, null, null, "title");
                    if (c != null && c.moveToFirst()) {
                        do {
                            String path = c.getString(1);
                            String artist = c.getString(2);
                            String title = c.getString(3);
                            mAdapter.add(new SongItem(title, artist, path));
                        } while (c.moveToNext());
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
                break;
            case MSG_DOWNLOAD_ALL:
                break;
            case MSG_UPDATE_LRC_ICON:
//                for (int i = 0; i < mAdapter.getCount(); i++) {
//                    SongItem item = mAdapter.getItem(i);
//                }
                break;
            default:
                Log.w(TAG, "Unknown message");
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_lrc_jaeger);
        
//        mHandlerThread.start();
//        LrcHandler handler = new LrcHandler(mHandlerThread.getLooper());
//        
//        handler.sendEmptyMessage(1);
        
        
        mListView = (ListView) findViewById(R.id.lv_song_items);
        mAdapter = new SongItemAdapter(this, new ArrayList<SongItem>());
        mListView.setAdapter(mAdapter);
        
        // initial UI
        mUiHandler.sendEmptyMessage(MSG_QUERY_DB);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        mAdapter.clear();
        mAdapter = null;
        mUiHandler.removeCallbacksAndMessages(null);
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
            if (lrcs != null) {
                for (QueryResult item : lrcs) {
                    String result = TTDownloader.download(item);
                    Log.d(TAG, "result is " + result);
                    Log.v(TAG, "==================================================");
                }
            }
        }
    };

}
