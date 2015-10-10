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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SongItemAdapter extends ArrayAdapter<SongItem> {

    private LayoutInflater mInflater = null;
    private ArrayList<SongItem> mSongs;
    private OnLrcClickListener mLrcListener;
    private HashMap<Integer, View> mConvertViewMap = new HashMap<>();
    
    public interface OnLrcClickListener {
        public void OnLrcClick(int position, View convertView, ViewGroup parent);
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
        boolean mChecked;
    }

    /**
     * Change the icon of this item to indicate its checked status
     *
     * @param convertView
     * @param checked
     */
    public void setItemChecked(View convertView, boolean checked) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.mChecked = checked;
    }

    /**
     * Change the icon of all items to unchecked state
     */
    public void clearChoice() {
        Set<Map.Entry<Integer, View>> set = mConvertViewMap.entrySet();
        for (Map.Entry<Integer, View> entry : set) {
            ViewHolder holder = (ViewHolder) entry.getValue().getTag();
            boolean wasChecked = holder.mChecked;
            holder.mChecked = false;
            if (wasChecked) {
                int color = mSongs.get(entry.getKey()).isHasLrc() ? Color.argb(255, 0, 162, 232)
                        : Color.LTGRAY;
                holder.mHasLrc.setTextColor(color);
            }
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.song_item, parent, false);
            holder = new ViewHolder();
            holder.mTitle = (TextView) convertView.findViewById(R.id.tv_song_title);
            holder.mArtist = (TextView) convertView.findViewById(R.id.tv_song_artist);
            holder.mHasLrc = (TextView) convertView.findViewById(R.id.tv_has_lrc);
            holder.mPosition = position;
            holder.mChecked = false;
            convertView.setTag(holder);
            mConvertViewMap.put(position, convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SongItem song = mSongs.get(position);

        holder.mTitle.setText(song.getTitle());
        holder.mArtist.setText(song.getArtist());

        int color = song.isHasLrc() ? Color.argb(255, 0, 162, 232) : Color.LTGRAY;
        color = holder.mChecked ? Color.RED : color;
        TextView hasLrc = holder.mHasLrc;
        hasLrc.setTextColor(color);
        
        hasLrc.setClickable(true);
        final View view = convertView;
        hasLrc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLrcListener.OnLrcClick(position, view, parent);
            }
        });
        
        return convertView;
    }

    @Override
    public void clear() {
        super.clear();
        mConvertViewMap.clear();
    }

}
