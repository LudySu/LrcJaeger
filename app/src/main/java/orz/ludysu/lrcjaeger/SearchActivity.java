package orz.ludysu.lrcjaeger;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";

    public static final String INTENT_TITLE_KEY = "title";
    public static final String INTENT_ARTIST_KEY = "artist";
    
    private static final int MSG_QUERY = 1;
    private static final int MSG_DOWNLOAD = 2;
    
    private BackgroundHandler mHandler;
    private SongItem mSongItem;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitivy_search_lrc);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        
        Intent i = getIntent();
        Uri uri = i.getData();
        String title = i.getStringExtra(INTENT_TITLE_KEY);
        bar.setTitle(title.length() > 0 ? title : getString(R.string.title_search_activity));
        String artist = i.getStringExtra(INTENT_ARTIST_KEY);
        mSongItem = new SongItem(title, artist, uri.getPath());
        Log.v(TAG, "incoming intent data " + mSongItem);
        
        HandlerThread ht = new HandlerThread("single-download");
        ht.start();
        mHandler = new BackgroundHandler(ht.getLooper());
        
        final EditText titleEt = (EditText) findViewById(R.id.et_title);
        titleEt.setText(mSongItem.getTitle());
        final EditText artistEt = (EditText) findViewById(R.id.et_artist);
        artistEt.setText(mSongItem.getArtist());
        
        Button btn = (Button) findViewById(R.id.btn_search);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEt.getText().toString();
                String artist = artistEt.getText().toString();
                mSongItem.setTitle(title);
                mSongItem.setArtist(artist);
                Log.v(TAG, "search title " + title + ", artist " + artist);
                
                if (title.length() > 0) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchActivity.this,
                            android.R.layout.simple_list_item_1,
                            new String[]{getString(R.string.msg_lrc_searching)});
                    mListView.setAdapter(adapter);

                    mHandler.sendEmptyMessage(MSG_QUERY);
                }
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        
        mListView = (ListView) findViewById(R.id.lv_query_items);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mQueryResult.size() > 0) {
                    mListView.setAdapter(makeSimpleMessageAdapter(R.string.msg_lrc_downloading));

                    Message msg = mHandler.obtainMessage(MSG_DOWNLOAD, position, 0);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * Make a adapter that contains a simple message to diaplay
     * @param resId resId of the message to diaplay
     */
    private ArrayAdapter<String> makeSimpleMessageAdapter(int resId) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchActivity.this,
                android.R.layout.simple_list_item_1,
                new String[]{getString(resId)});
        return adapter;
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
                if (mQueryResult.size() > 0) {
                    ArrayAdapter<QueryResult> adapter = new ArrayAdapter<>(SearchActivity.this,
                            android.R.layout.simple_list_item_1, mQueryResult);
                    mListView.setAdapter(adapter);
                } else {
                    mListView.setAdapter(makeSimpleMessageAdapter(R.string.msg_lrc_not_found));
                }
                break;
            case MSG_DOWNLOAD:
                Toast.makeText(SearchActivity.this, R.string.toast_download_ok, Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                break;
            }
        }
    };
    
    private class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper l) {
            super(l);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_QUERY:
                    mQueryResult = TTDownloader.query(mSongItem.getArtist(), mSongItem.getTitle());
                    mUiHandler.sendEmptyMessage(MSG_QUERY);
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
