package orz.ludysu.lrcjaeger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import orz.ludysu.lrcjaeger.SongItemAdapter.OnLrcClickListener;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

// Main activity
public class LrcJaeger extends AppCompatActivity {
    private static final String TAG = "LrcJaeger";
    private static final String[] PROJECTION = new String[] {"_id", "_data", "artist", "title"};
    
    private static final int MSG_QUERY_DB = 1;
    private static final int MSG_DOWNLOAD_ALL = 10;
    private static final int MSG_DOWNLOAD_ITEM = 11;
    private static final int MSG_UPDATE_LRC_ICON_ALL = 20;
    private static final int MSG_UPDATE_LRC_ICON = 21;
    private static final int MSG_REMOVE_ITEM_FROM_LIST = 31;

    private ListView mListView;
    private SongItemAdapter mAdapter;
    private MenuItem mDownAllButton;
    private ProgressBar mProgressBar;
    private BulkDownloadTask mTask;
    private UiHandler mUiHandler;
    private Set<String> mAllFolders = new HashSet<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lrc_jaeger);

        mUiHandler = new UiHandler(this);
        mListView = (ListView) findViewById(R.id.lv_song_items);
        mAdapter = new SongItemAdapter(this, new ArrayList<SongItem>());
        mAdapter.setLrcClickListener(new OnLrcClickListener() {
            @Override
            public void OnLrcClick(int position) {
                // FIXME
            }
        });

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SongItem item = mAdapter.getItem(position);
                if (item.isHasLrc()) { // display lyric content
                    Intent i = new Intent();
                    i.setClass(LrcJaeger.this, DisplayLrcActivity.class);
                    i.putExtra(Constants.INTENT_KEY_OBJECT, item);
                    startActivity(i);
                } else { // start a search activity
                    Intent i = new Intent();
                    i.setClass(LrcJaeger.this, SearchActivity.class);
                    i.putExtra(Constants.INTENT_KEY_OBJECT, item);
                    startActivity(i);
                }
            }
        });

        // show popup menu
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
            }
        });
    }
    
    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();

        // initialize song list
        mUiHandler.sendEmptyMessage(MSG_QUERY_DB);

        // update lrc icons
        mUiHandler.sendEmptyMessage(MSG_UPDATE_LRC_ICON_ALL);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        
        if (mTask != null) {
            mTask.cancel(true);
        }
        mAdapter.clear();
        mAdapter = null;
        mUiHandler.removeCallbacksAndMessages(null);
        mUiHandler = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_lrc_jaeger, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_downall:
                if (!Utils.isNetworkAvailable(this)) {
                    Toast.makeText(this, R.string.toast_no_network_connection, Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "no network connection");
                    break;
                }
                mDownAllButton = item;
                mProgressBar = (ProgressBar)getLayoutInflater().inflate(R.layout.progressbar, null);
                mUiHandler.sendEmptyMessage(MSG_DOWNLOAD_ALL);
                break;

            case R.id.action_hide:
                Intent i = new Intent();
                i.setClass(this, HideFoldersActivity.class);
                ArrayList<String> list = new ArrayList();
                list.addAll(mAllFolders);
                i.putStringArrayListExtra(HideFoldersActivity.INTENT_DATA_KEY, list);
                startActivity(i);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    
    private static class UiHandler extends Handler {
        WeakReference<LrcJaeger> mActivity = null;

        public UiHandler(LrcJaeger activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LrcJaeger activity = mActivity.get();
            if (activity == null) {
                Log.e(TAG, "Cannot handle message: activity has been destroyed");
                return;
            }

            switch (msg.what) {
                case MSG_QUERY_DB:
                    // folders in this set should be hidden to user
                    Set<Integer> hiddenSet = Utils.getHiddenFoldersFromPreference(activity);

                    // update song listview
                    activity.mAdapter.clear();
                    Cursor c = null;
                    try {
                        c = activity.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                PROJECTION, "is_music=?", new String[]{"1"}, "title_key");
                        if (c != null && c.moveToFirst()) {
                            do {
                                String path = c.getString(1);
                                String artist = c.getString(2);
                                String title = c.getString(3);
                                String folder = Utils.getFolder(path);
                                activity.mAllFolders.add(folder);
                                if (!hiddenSet.contains(folder.hashCode())) {
                                    activity.mAdapter.add(new SongItem(title, artist, path));
                                }
                            } while (c.moveToNext());
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                    break;

                case MSG_DOWNLOAD_ALL:
                    final ArrayList<SongItem> listAll = new ArrayList<>();
                    for (int i = 0; i < activity.mAdapter.getCount(); i++) {
                        SongItem item = activity.mAdapter.getItem(i);
                        if (!item.isHasLrc()) {
                            listAll.add(item);
                        }
                    }
                    if (listAll.size() > 0) {
                        MenuItemCompat.setActionView(activity.mDownAllButton, activity.mProgressBar);
                        MenuItemCompat.expandActionView(activity.mDownAllButton);

                        activity.mTask = new BulkDownloadTask(new BulkDownloadTask.EventListener() {
                            @Override
                            public void onFinish(int downloaded) {
                                if (activity.mDownAllButton != null) {
                                    MenuItemCompat.collapseActionView(activity.mDownAllButton);
                                    MenuItemCompat.setActionView(activity.mDownAllButton, null);
                                }
                                sendEmptyMessage(MSG_UPDATE_LRC_ICON_ALL);

                                String text = String.format(activity.getString(R.string.toast_lrc_downloaded),
                                        downloaded, listAll.size());
                                Toast.makeText(activity, text, Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onProgressUpdate(int progress) {
                                activity.mProgressBar.setProgress(progress);
                            }
                        });
                        activity.mTask.execute(listAll.toArray(new SongItem[1]));
                    }
                    break;

                case MSG_UPDATE_LRC_ICON:
                    break;

                case MSG_UPDATE_LRC_ICON_ALL:
                    for (int i = 0; i < activity.mAdapter.getCount(); i++) {
                        SongItem it = activity.mAdapter.getItem(i);
                        it.updateStatus();
                    }
                    activity.mAdapter.notifyDataSetChanged();
                    break;

                case MSG_REMOVE_ITEM_FROM_LIST:
                    int pos = msg.arg1;
                    activity.mAdapter.remove(activity.mAdapter.getItem(pos));
                    break;

                default:
                    throw new IllegalArgumentException("Unknown message code " + msg.what);
            }
        }
    };

}
