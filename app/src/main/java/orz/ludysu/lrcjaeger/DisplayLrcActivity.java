package orz.ludysu.lrcjaeger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class DisplayLrcActivity extends AppCompatActivity {

    public static final String INTENT_CONTENT_KEY = "lrc_content";
    public static final String INTENT_TITLE_KEY = "lrc_title";
    public static final String INTENT_PATH_KEY = "lrc_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_lrc);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        bar.setTitle(i.getStringExtra(INTENT_TITLE_KEY));

        TextView path = (TextView) findViewById(R.id.tv_lrc_path);
        path.setText(getString(R.string.title_lrc_path) + i.getStringExtra(INTENT_PATH_KEY));

        TextView tv = (TextView) findViewById(R.id.tv_lrc_content);
        tv.setText(i.getStringExtra(INTENT_CONTENT_KEY));
        tv.setMovementMethod(new ScrollingMovementMethod());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_display_lrc, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_text_only:
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}
