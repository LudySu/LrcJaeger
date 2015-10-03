package orz.ludysu.lrcjaeger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayLrcActivity extends AppCompatActivity {

    public static final String TAG = "DisplayLrcActivity";
    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String INTENT_CONTENT_KEY = "lrc_content";
    public static final String INTENT_TITLE_KEY = "lrc_title";
    public static final String INTENT_PATH_KEY = "lrc_path";

    private String mLrcContent;
    private String mTextContent;
    private boolean mIsInTextOnlyMode = false;

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
        mLrcContent = i.getStringExtra(INTENT_CONTENT_KEY);
        tv.setText(mLrcContent);
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
                if (!mIsInTextOnlyMode) {
                    if (mTextContent == null) {
                        StringBuilder sb = new StringBuilder();
                        try {
                            BufferedReader reader = new BufferedReader(new StringReader(mLrcContent));

                            // match lines like: [00:06.78]lyric content
                            Pattern p = Pattern.compile("^\\[\\d{2}:\\d{2}\\.\\d{2}\\](.*)");

                            String line;
                            while ((line = reader.readLine()) != null) {
                                Matcher m = p.matcher(line);
                                boolean matches = m.find();
                                Log.v(TAG, "match = " + matches + ", line = " + line);

                                if (matches) {
                                    sb.append(m.group(1));
                                    sb.append(LINE_SEPARATOR);
                                }
                            }
                        } catch (IOException ex) {
                            // ignore
                            ex.printStackTrace();
                        }
                        mTextContent = sb.toString();
                    }

                    TextView tv = (TextView) findViewById(R.id.tv_lrc_content);
                    tv.setText(mTextContent);

                    item.setTitle(R.string.action_whole_lrc);
                    mIsInTextOnlyMode = true;
                } else {
                    TextView tv = (TextView) findViewById(R.id.tv_lrc_content);
                    tv.setText(mLrcContent);

                    item.setTitle(R.string.action_text_only);
                    mIsInTextOnlyMode = false;
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}
