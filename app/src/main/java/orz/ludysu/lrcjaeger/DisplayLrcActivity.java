package orz.ludysu.lrcjaeger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayLrcActivity extends AppCompatActivity {

    private static final String TAG = "DisplayLrcActivity";


    private String mLrcContent;
    private String mTextContent;
    private boolean mIsInTextOnlyMode = false;
    private SongItem mSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_lrc);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(false);

        Intent i = getIntent();
        mSong = i.getParcelableExtra(Constants.INTENT_KEY_OBJECT);
        bar.setTitle(mSong.getTitle());

        TextView path = (TextView) findViewById(R.id.tv_lrc_path);
        path.setText(getString(R.string.title_lrc_path) + mSong.getLrcPath());

        TextView tv = (TextView) findViewById(R.id.tv_lrc_content);
        mLrcContent = i.getStringExtra(Constants.INTENT_KEY_CONTENT);
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
                if (!mIsInTextOnlyMode) { // toggle to text only mode
                    if (mTextContent == null) {
                        // remove time tag in .lrc file and only extract text
                        StringBuilder sb = new StringBuilder();
                        try {
                            BufferedReader reader = new BufferedReader(new StringReader(mLrcContent));

                            // match lines in .lrc format like: [00:06.78]lyric content
                            Pattern p = Pattern.compile("^[\\[\\d{2}:\\d{2}\\.\\d{2}\\]]+(.*)");

                            String line;
                            while ((line = reader.readLine()) != null) {
                                Matcher m = p.matcher(line);
                                boolean matches = m.find();
                                if (matches) {
                                    sb.append(m.group(1));
                                    sb.append(Constants.LINE_SEPARATOR);
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
                } else { // toggle to whole lrc content mode including time tag
                    TextView tv = (TextView) findViewById(R.id.tv_lrc_content);
                    tv.setText(mLrcContent);

                    item.setTitle(R.string.action_text_only);
                    mIsInTextOnlyMode = false;
                }
                break;

            case R.id.action_delete_lrc:
                String lrcPath = mSong.getLrcPath();
                if (lrcPath.length() > 0) {
                    boolean ok = (new File(lrcPath)).delete();
                    if (!ok) {
                        Toast.makeText(this, getString(R.string.toast_delete_err), Toast.LENGTH_SHORT);
                    }
                    finish();
                }
                break;

            case R.id.action_search_lrc:
                break;

            case R.id.action_edit_lrc:
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}
