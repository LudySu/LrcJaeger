package com.example.lrcjaeger;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

// TODO: 手动搜索下载，下载过滤规则，×歌词编辑，×选择服务器，OO重构，×文件夹过滤

public class LrcJaeger extends Activity {
    private static final String TAG = "LrcJaeger";
    private static final String[] PROJECTION = new String[] {"_id", "_data", "artist", "title"};
    
    private static final int MSG_QUERY_DB = 1;
    private static final int MSG_DOWNLOAD_ALL = 10;
    private static final int MSG_DOWNLOAD_ITEM = 11;
    private static final int MSG_UPDATE_LRC_ICON_ALL = 20;
    private static final int MSG_UPDATE_LRC_ICON = 21;
    
    
    private ListView mListView;
    private SongItemAdapter mAdapter;
    private MenuItem mDownAllButton;
    private ProgressBar mProgressBar;
    private DownloadTask mTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lrc_jaeger);
        
        mListView = (ListView) findViewById(R.id.lv_song_items);
        mAdapter = new SongItemAdapter(this, new ArrayList<SongItem>());
        mListView.setAdapter(mAdapter);
        
        final GestureDetector gestureDetector = new GestureDetector(this, new MyGestureDetector());
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        mListView.setOnTouchListener(gestureListener);
        
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
        
        if (mTask != null) {
            mTask.cancel(true);
        }
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
        case R.id.menu_download_rules:
            // TODO setting
            break;

        default:
            break;
        }
        return true;
    } 
    


    private class MyGestureDetector extends SimpleOnGestureListener {
        private final int REL_SWIPE_MIN_DISTANCE;
        private final int REL_SWIPE_MAX_OFF_PATH;
        private final int REL_SWIPE_THRESHOLD_VELOCITY;

        public MyGestureDetector() {
            super();
            DisplayMetrics dm = getResources().getDisplayMetrics();
            REL_SWIPE_MIN_DISTANCE = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
            REL_SWIPE_MAX_OFF_PATH = (int) (250.0f * dm.densityDpi / 160.0f + 0.5);
            REL_SWIPE_THRESHOLD_VELOCITY = (int) (200.0f * dm.densityDpi / 160.0f + 0.5);
        }

        private void onListViewItemClicked(int position) {
            Log.v(TAG, "on item click at pos " + position);
            SongItem item = mAdapter.getItem(position);
            item.updateStatus();
            if (!item.isHasLrc()) {
                Message msg = mUiHandler.obtainMessage(MSG_DOWNLOAD_ITEM, item);
                mUiHandler.sendMessage(msg);
            } else {
                // TODO search by hand
            }
        }

        private void onFling(int position) {
            Log.v(TAG, "on item fling at pos " + position);
            SongItem item = mAdapter.getItem(position);
            item.updateStatus();
            if (item.isHasLrc()) {
                // delete lrc file
                File lrc = new File(item.getLrcPath());
                boolean ret = lrc.delete();
                if (!ret) {
                    Toast.makeText(LrcJaeger.this, R.string.toast_delete_err, Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: only update single icon
                    mUiHandler.sendEmptyMessage(MSG_UPDATE_LRC_ICON_ALL);
                }
            }
        }

        // Detect a single-click and call my own handler.
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int pos = mListView.pointToPosition((int) e.getX(), (int) e.getY());
            onListViewItemClicked(pos);
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH) {
                return false;
            }
            if (Math.abs(e1.getX() - e2.getX()) > REL_SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
                int pos = mListView.pointToPosition((int) e1.getX(), (int) e1.getY());
                onFling(pos);
            }
            return false;
        }

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
                    c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, 
                            "is_music=?", new String[]{"1"}, "title_key");
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
                ArrayList<SongItem> listAll = new ArrayList<SongItem>();
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    SongItem item = mAdapter.getItem(i);
                    if (!item.isHasLrc()) {
                        listAll.add(item);
                    }
                }
                if (listAll.size() > 0) {
                    mTask = new DownloadTask();
                    mTask.execute(listAll.toArray(new SongItem[1]));
                }
                break;
            case MSG_DOWNLOAD_ITEM:
                SongItem item = (SongItem) msg.obj;
                if (item == null) {
                    Log.w(TAG, "no item found in message");
                    return;
                }
                DownloadTask task = new DownloadTask();
                task.execute(new SongItem[] {item});
                break;
            case MSG_UPDATE_LRC_ICON:
                break;
            case MSG_UPDATE_LRC_ICON_ALL:
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    SongItem it = mAdapter.getItem(i);
                    it.updateStatus();
                }
                mAdapter.notifyDataSetChanged();
                break;
            default:
                Log.w(TAG, "Unknown message");
                break;
            }
        }
    };
    
    private class DownloadTask extends AsyncTask<SongItem, Integer, Integer> {
        @Override
        protected Integer doInBackground(SongItem... list) { // on an independent thread
            if (list == null || list.length <= 0) {
                Log.w(TAG, "items null");
            }
            int count = 0;
            int downloaded = 0;
            int total = list.length;
            for (SongItem item : list) {
                if (isCancelled()) {
                    Log.i(TAG, "download task is canceled, " + (total - count) + " items left");
                    break;
                }
                count++;
                ArrayList<QueryResult> lrcs = TTDownloader.query(item.getArtist(), item.getTitle());
                if (lrcs != null && lrcs.size() > 0) {
                    boolean result = TTDownloader.download(lrcs, item.getLrcPath(),
                            TTDownloader.DOWNLLOAD_SHORTEST_NAME);
                    downloaded = result ? downloaded + 1: downloaded;   
                }
                if (total > 1) {
                    publishProgress(100 * count / total);
                }
            }
            Log.d(TAG, "downloaded " + downloaded + " of " + total + " items");
            return total;
        }
        
        @Override
        protected void onProgressUpdate(Integer...progress) {
            mProgressBar.setProgress(progress[0]);
        }


        @Override
        protected void onPostExecute(Integer total) {
            if (mDownAllButton != null && total > 1) {
                mDownAllButton.collapseActionView();
                mDownAllButton.setActionView(null);
            }
            mUiHandler.sendEmptyMessage(MSG_UPDATE_LRC_ICON_ALL);
        }
    };

}
