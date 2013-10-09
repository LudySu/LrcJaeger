package orz.ludysu.lrcjaeger;

import java.util.ArrayList;

import orz.ludysu.lrcjaeger.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SongItemAdapter extends ArrayAdapter<SongItem> {
    private LayoutInflater mInflater = null;
    private ArrayList<SongItem> mSongs;
    private OnLrcClickListener mLrcListener;
    
    public interface OnLrcClickListener {
        public void OnLrcClick(int position);
    }

    public SongItemAdapter(Context context, ArrayList<SongItem> songs) {
        super(context, 0, songs);
        mInflater = LayoutInflater.from(context);
        mSongs = songs;
    }
    
    public void setLrcClickListener(OnLrcClickListener l) {
        mLrcListener = l;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.song_item, parent, false);
        }
        
        SongItem song = mSongs.get(position);
        
        TextView title = (TextView) convertView.findViewById(R.id.tv_song_title);
        title.setText(song.getTitle());
        
        TextView artist = (TextView) convertView.findViewById(R.id.tv_song_artist);
        artist.setText(song.getArtist());

        int color = song.isHasLrc() ? Color.argb(255, 0, 162, 232) : Color.LTGRAY;
        TextView hasLrc = (TextView) convertView.findViewById(R.id.tv_has_lrc);
        hasLrc.setTextColor(color);
        
        hasLrc.setClickable(true);
        hasLrc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLrcListener.OnLrcClick(position);
            }
        });
        
        return convertView;
    }

}
