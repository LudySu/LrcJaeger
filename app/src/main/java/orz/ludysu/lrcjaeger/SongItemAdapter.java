package orz.ludysu.lrcjaeger;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

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

    private class ViewHolder {
        TextView mTitle;
        TextView mArtist;
        TextView mHasLrc;
        int mPosition;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.song_item, parent, false);
            holder = new ViewHolder();
            holder.mTitle = (TextView) convertView.findViewById(R.id.tv_song_title);
            holder.mArtist = (TextView) convertView.findViewById(R.id.tv_song_artist);
            holder.mHasLrc = (TextView) convertView.findViewById(R.id.tv_has_lrc);
            holder.mPosition = position;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SongItem song = mSongs.get(position);

        holder.mTitle.setText(song.getTitle());
        holder.mArtist.setText(song.getArtist());

        int color = song.isHasLrc() ? Color.argb(255, 0, 162, 232) : Color.LTGRAY;
        TextView hasLrc = holder.mHasLrc;
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
