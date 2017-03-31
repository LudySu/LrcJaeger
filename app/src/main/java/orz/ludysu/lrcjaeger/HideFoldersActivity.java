package orz.ludysu.lrcjaeger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class HideFoldersActivity extends AppCompatActivity {
    private static final String TAG = "HideFoldersActivity";

    public static final String INTENT_DATA_KEY = "folders";

    private Set<Integer> mHiddenFolders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_folders);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        Intent i = getIntent();
        ArrayList<String> folders = i.getStringArrayListExtra(INTENT_DATA_KEY);
        final ArrayAdapter adapter = new FolderAdapter(this, folders);

        // load hidden folders set by user from shared prefs
        mHiddenFolders = Utils.loadHiddenFoldersFromDisk(this);

        ListView lv = (ListView) findViewById(R.id.lv_hide_folders);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView tv = (CheckedTextView) view;
                tv.toggle();
                int color = tv.isChecked() ? Color.BLACK : Color.LTGRAY;
                tv.setTextColor(color);

                int hashCode = adapter.getItem(position).hashCode();
                if (tv.isChecked()) {
                    mHiddenFolders.remove(hashCode);
                    Utils.removeHiddenFolder(getBaseContext(), hashCode);
                } else {
                    mHiddenFolders.add(hashCode);
                    Utils.addHiddenFolder(getBaseContext(), hashCode);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        Utils.writeHiddenFolders(this);
        super.onDestroy();
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
            if (mHiddenFolders.contains(folder.hashCode())) {
                // this folder was set to be hidden
                tv.setChecked(false);
                tv.setTextColor(Color.LTGRAY);
            } else {
                // set default status
                tv.setChecked(true);
                tv.setTextColor(Color.BLACK);
            }

            return convertView;
        }

    }

}
