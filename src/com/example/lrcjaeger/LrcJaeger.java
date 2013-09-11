package com.example.lrcjaeger;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

// TODO: »»ui£¬ÊÖ¶¯ËÑË÷ÏÂÔØ£¬ÏÂÔØ¹ýÂË¹æÔò£¬¸è´ÊÉ¾³ý£¬¸è´Ê±à¼­

public class LrcJaeger extends Activity {
    private static final String TAG = "LrcJaeger";
    private static final String[] PROJECTION = new String[] {"_id", "_data", "artist", "title"};
    
    private static final int MSG_QUERY_DB = 1;
    private static final int MSG_DOWNLOAD_ALL = 2;
    private static final int MSG_UPDATE_LRC_ICON = 3;
    
    private ListView mListView;
    private SongItemAdapter mAdapter;
    private MenuItem mDownAllButton;
    private ProgressBar mProgressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lrc_jaeger);
        
        mListView = (ListView) findViewById(R.id.lv_song_items);
        mAdapter = new SongItemAdapter(this, new ArrayList<SongItem>());
        mListView.setAdapter(mAdapter);
        
        // initial song list
        mUiHandler.sendEmptyMessage(MSG_QUERY_DB);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // update song list
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_lrc_jaeger, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_downall:
            if (!Utils.isNetworkAvailable(this)) {
                Toast.makeText(this, R.string.toast_no_network_connection, Toast.LENGTH_SHORT).show();
                Log.w(TAG, "no network connection");
                break;
            }
            mDownAllButton = item;
            mProgressBar = (ProgressBar)getLayoutInflater().inflate(R.layout.progressbar, null);
            mDownAllButton.setActionView(mProgressBar);
            mDownAllButton.expandActionView();
            mUiHandler.sendEmptyMessage(MSG_DOWNLOAD_ALL);
            break;

        default:
            break;
        }
        return true;
    } 
    
    
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_QUERY_DB:
                // update song listview
                mAdapter.clear();
                Cursor c = null;
                try {
                    c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, null, null, "title_key");
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
                ArrayList<SongItem> list = new ArrayList<SongItem>();
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    SongItem item = mAdapter.getItem(i);
                    if (!item.isHasLrc()) {
                        list.add(item);
                    }
                }
                if (list.size() > 0) {
                    new DownloadTask().execute(list.toArray(new SongItem[1]));
                }
                break;
            case MSG_UPDATE_LRC_ICON:
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    SongItem item = mAdapter.getItem(i);
                    item.updateStatus();
                }
                mAdapter.notifyDataSetChanged();
                break;
            default:
                Log.w(TAG, "Unknown message");
                break;
            }
        }
    };
    
    private class DownloadTask extends AsyncTask<SongItem, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(SongItem... list) { // on an independent thread
            if (list == null || list.length <= 0) {
                Log.w(TAG, "items null");
            }
            int count = 0;
            int downloaded = 0;
            int total = list.length;
            for (SongItem item : list) {
                count++;
                ArrayList<QueryResult> lrcs = TTDownloader.query(item.getArtist(), item.getTitle());
                if (lrcs != null && lrcs.size() > 0) {
                    boolean result = TTDownloader.download(lrcs, item.getLrcPath(),
                            TTDownloader.DOWNLLOAD_SHORTEST_NAME);
                    downloaded = result ? downloaded + 1: downloaded;   
                }
                publishProgress(100 * count / total);
            }
            Log.d(TAG, "downloaded " + downloaded + " of " + total + " items");
            return true;
        }
        
        @Override
        protected void onProgressUpdate(Integer...progress) {
            mProgressBar.setProgress(progress[0]);
        }


        @Override
        protected void onPostExecute(Boolean result) {
            if (mDownAllButton != null) {
                mDownAllButton.collapseActionView();
                mDownAllButton.setActionView(null);
            }
            mUiHandler.sendEmptyMessage(MSG_UPDATE_LRC_ICON);
        }
    };

}
