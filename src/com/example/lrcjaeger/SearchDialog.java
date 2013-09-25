package com.example.lrcjaeger;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SearchDialog extends Activity {
    private static final String TAG = "LrcJaeger/Search";
    
    private static final int MSG_QUERY = 1;
    private static final int MSG_DOWNLOAD = 2;
    
    private MyHandler mHandler;
    private SongItem mSongItem;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitivy_search_dialog);
        
        Intent i = getIntent();
        Uri uri = i.getData();
        String title = i.getStringExtra("title");
        String artist = i.getStringExtra("artist");
        mSongItem = new SongItem(title, artist, uri.getPath());
        Log.v(TAG, "incoming intent data " + mSongItem);
        
        HandlerThread ht = new HandlerThread("single-download");
        ht.start();
        mHandler = new MyHandler(ht.getLooper());
        
        final EditText titleEt = (EditText) findViewById(R.id.et_title);
        titleEt.setText(mSongItem.getTitle());
        final EditText artistEt = (EditText) findViewById(R.id.et_artist);
        artistEt.setText(mSongItem.getArtist());
        
        Button cancel = (Button) findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        
        Button ok = (Button) findViewById(R.id.btn_ok);
        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String title = titleEt.getText().toString();
                String artist = artistEt.getText().toString();
                Log.v(TAG, "search title " + title + ", artist " + artist);
                
                if (!title.isEmpty()) {
                    mHandler.sendEmptyMessage(MSG_QUERY);
                }
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
                inputManager.hideSoftInputFromWindow(SearchDialog.this.getCurrentFocus().getWindowToken(),      
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        
        mListView = (ListView) findViewById(R.id.lv_query_items);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message msg = mHandler.obtainMessage(MSG_DOWNLOAD, position, 0);
                mHandler.sendMessage(msg);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    private ArrayList<QueryResult> mQueryResult;
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_QUERY:
                ArrayAdapter<QueryResult> adapter = new ArrayAdapter<QueryResult>(SearchDialog.this, 
                        android.R.layout.simple_list_item_1, mQueryResult);
                mListView.setAdapter(adapter);
                break;
            case MSG_DOWNLOAD:
                Toast.makeText(SearchDialog.this, "Donwload OK", Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                break;
            }
        }
    };
    
    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_QUERY:
                mQueryResult = TTDownloader.query(mSongItem.getArtist(), mSongItem.getTitle());
                if (mQueryResult != null && mQueryResult.size() > 0) {
                    mUiHandler.sendEmptyMessage(MSG_QUERY);
                }
                break;
            case MSG_DOWNLOAD:
                int position = msg.arg1;
                QueryResult item = mQueryResult.get(position);
                if (item == null) {
                    Log.w(TAG, "no item found in message");
                    return;
                }
                boolean result = TTDownloader.download(item, mSongItem.getLrcPath());
                mUiHandler.sendEmptyMessage(MSG_DOWNLOAD);
                break;
            default:
                break;
            }
        }
    };
}
