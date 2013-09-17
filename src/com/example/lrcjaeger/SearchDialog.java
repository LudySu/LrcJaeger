package com.example.lrcjaeger;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SearchDialog extends Activity {
    private static final String TAG = "LrcJaeger/Search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitivy_search_dialog);
        
        final EditText titleEt = (EditText) findViewById(R.id.et_title);
        final EditText artistEt = (EditText) findViewById(R.id.et_artist);
        
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
                    // TODO search and result list
                }
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
