package orz.ludysu.lrcjaeger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HideFoldersActivity extends AppCompatActivity {
    private static final String TAG = "HideFoldersActivity";

    private ArrayAdapter mAdapter = null;
    private ListView mListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_folders);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(R.string.action_hide_folder);

        Intent i = getIntent();
        ArrayList<String> folders = i.getStringArrayListExtra("folders");
        mAdapter = new FolderAdapter(this, folders);

        mListView = (ListView) findViewById(R.id.lv_hide_folders);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView tv = (CheckedTextView) view;
                tv.toggle();
                int color = tv.isChecked() ? Color.BLACK : Color.LTGRAY;
                tv.setTextColor(color);
            }
        });

    }

    private class FolderAdapter extends ArrayAdapter<String> {

        private LayoutInflater mInflater = null;

        public FolderAdapter(Context context, ArrayList<String> folders) {
            super(context, 0, folders);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.folder_item, parent, false);
            }

            String folder = getItem(position);

            CheckedTextView tv = (CheckedTextView) convertView.findViewById(R.id.cb_folder);
            tv.setText(folder);

            return convertView;
        }

    }

}
