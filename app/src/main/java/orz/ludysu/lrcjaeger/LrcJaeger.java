package orz.ludysu.lrcjaeger;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// Main activity
public class LrcJaeger extends AppCompatActivity {

    private static final String TAG = "LrcJaeger";
    private static final String[] PROJECTION = new String[] {"_id", "_data", "artist", "title"};
    
    private static final int MSG_QUERY_DB = 1;
    private static final int MSG_DOWNLOAD_ALL = 10;
    private static final int MSG_DOWNLOAD_ITEMS = 11;
    private static final int MSG_UPDATE_LRC_ICON_ALL = 20;
    private static final int MSG_UPDATE_LRC_ICON = 21;

    private ArrayAdapter mAdapter;
    private MenuItem mDownAllButton;
    private ProgressBar mProgressBar;
    private BulkDownloadTask mTask;
    private MyHandler mUiHandler;
    private Set<String> mAllFolders = new HashSet<>();
    private MultiChoiceFacade mMultiChoiceFacade;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO savedInstanceState
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lrc_jaeger);

        mUiHandler = new MyHandler(this);
        MultiChoiceListView lv = (MultiChoiceListView) findViewById(R.id.lv_song_items);
        mMultiChoiceFacade = new MultiChoiceFacade(this, lv);

        mMultiChoiceFacade.setOnItemClickListener(mOnItemClickListener);
        mMultiChoiceFacade.setMultiChoiceModeListener(mActionModeCallback);
        mAdapter = mMultiChoiceFacade.getAdapter();
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SongItem item = mMultiChoiceFacade.getItem(position);
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
    };

    private MultiChoiceListView.OnItemCheckedListener mActionModeCallback =
            new MultiChoiceListView.OnItemCheckedListener() {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, boolean checked) {
            int count = mMultiChoiceFacade.getCheckedItemCount();
            String str = String.format(getString(R.string.title_items_checked), count);
            mode.setTitle(str);
        }

        @Override
        public void onNothingChecked() {
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.activity_lrc_jaeger_contextual, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ArrayList<SongItem> items = mMultiChoiceFacade.getCheckedItems();
            switch (item.getItemId()) {
                case R.id.action_delete_all_lrc:
                    for (SongItem i : items) {
                        File f = new File(i.getLrcPath());
                        if (f.exists()) {
                            boolean res = f.delete();
                            Log.v(TAG, "deleting " + i.getTitle() + ", OK " + res);
                        }
                    }

                    // update lrc icons which indicate whether the song has a lrc
                    mUiHandler.sendEmptyMessage(MSG_UPDATE_LRC_ICON_ALL);
                    return true;

                case R.id.action_downall_context:
                    // update lrc icons which indicate whether the song has a lrc
                    mUiHandler.sendEmptyMessage(MSG_UPDATE_LRC_ICON_ALL);
                    return true;

                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    };

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();

        // initialize song list
        mUiHandler.sendEmptyMessage(MSG_QUERY_DB);

        // update lrc icons which indicate whether the song has a lrc
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
                ArrayList<String> list = new ArrayList<>();
                list.addAll(mAllFolders);
                i.putStringArrayListExtra(HideFoldersActivity.INTENT_DATA_KEY, list);
                startActivity(i);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    
    private static class MyHandler extends UiHandler<LrcJaeger> {

        public MyHandler(LrcJaeger activity) {
            super(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LrcJaeger activity = super.getActivity();
            if (activity == null) {
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
                                PROJECTION, "is_music=?", new String[]{"1"}, "title COLLATE LOCALIZED ASC");
                        if (c != null && c.moveToFirst()) {
                            do {
                                String path = c.getString(1);
                                String artist = c.getString(2);
                                String title = c.getString(3);
                                String folder = Utils.getFolder(path);
                                activity.mAllFolders.add(folder);
                                if (!hiddenSet.contains(folder.hashCode())) {
                                    // TODO change to facade
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
                        SongItem item = (SongItem) activity.mAdapter.getItem(i);
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
                                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
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
                        SongItem it = (SongItem) activity.mAdapter.getItem(i);
                        it.updateStatus();
                    }
                    activity.mAdapter.notifyDataSetChanged();
                    break;

                default:
                    throw new IllegalArgumentException("Unknown message code " + msg.what);
            }
        }
    };

}
