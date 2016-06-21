package orz.ludysu.lrcjaeger;

import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    
    private static final int MSG_QUERY = 1;
    private static final int MSG_DOWNLOAD = 2;
    
    private BackgroundHandler mHandler;
    private SongItem mSongItem;
    private ListView mListView;
    private MyHandler mUiHandler;
    private List<QueryResult> mQueryResult = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitivy_search_lrc);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        
        Intent i = getIntent();
        mSongItem = i.getParcelableExtra(Constants.INTENT_KEY_OBJECT);
        Log.v(TAG, "incoming intent data " + mSongItem);

        bar.setTitle(mSongItem.getTitle());
        
        HandlerThread ht = new HandlerThread("single-download");
        ht.start();
        mHandler = new BackgroundHandler(ht.getLooper());
        mUiHandler = new MyHandler(this);
        
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
     * Make a adapter that contains a simple text message to diaplay
     * @param resId resId of the message to display in ListView
     */
    private ArrayAdapter<String> makeSimpleMessageAdapter(int resId) {
        return new ArrayAdapter<>(SearchActivity.this,
                android.R.layout.simple_list_item_1,
                new String[]{getString(resId)});
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    

    private static class MyHandler extends UiHandler<SearchActivity> {

        public MyHandler(SearchActivity activity) {
            super(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final SearchActivity activity = super.getActivity();
            if (activity == null) {
                return;
            }

            switch (msg.what) {
                case MSG_QUERY:
                    if (activity.mQueryResult.size() > 0) {
                        ArrayAdapter<QueryResult> adapter = new ArrayAdapter<>(activity,
                                android.R.layout.simple_list_item_1, activity.mQueryResult);
                        activity.mListView.setAdapter(adapter);
                    } else {
                        activity.mListView.setAdapter(activity.makeSimpleMessageAdapter(R.string.msg_lrc_not_found));
                    }
                    break;
                case MSG_DOWNLOAD:
                    Toast.makeText(activity, R.string.toast_download_ok, Toast.LENGTH_SHORT).show();
                    activity.finish();
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
