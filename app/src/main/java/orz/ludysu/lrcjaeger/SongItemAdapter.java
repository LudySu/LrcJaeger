package orz.ludysu.lrcjaeger;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SongItemAdapter extends ArrayAdapter<SongItem> {
    private static final String TAG = "SongItemAdapter";

    private LayoutInflater mInflater = null;
    private ArrayList<SongItem> mSongs;
    private OnLrcClickListener mLrcListener;
    private HashMap<Integer, View> mConvertViewMap = new HashMap<>();

    public SongItemAdapter(Context context, ArrayList<SongItem> songs) {
        super(context, 0, songs);
        mInflater = LayoutInflater.from(context);
        mSongs = songs;
    }

    public interface OnLrcClickListener {
        public void OnLrcClick(int position, View convertView, ViewGroup parent);
    }

    public void setLrcClickListener(OnLrcClickListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        mLrcListener = l;
    }

    /**
     * Update the item view that has been checked, i.e. display an indicator
     *
     * @param position
     * @param checked
     */
    public void setItemChecked(int position, boolean checked) {
        ViewHolder holder = (ViewHolder) mConvertViewMap.get(position).getTag();
        holder.mIsChecked = checked;
        updateLrcIconView(holder.mLrcIcon, holder);
        //Log.v(TAG, "setItemChecked " + checked + ", at pos " + position);
    }

    private void updateLrcIconView(TextView icon, ViewHolder holder) {
        if (holder.mIsChecked) {
            icon.setTextColor(Color.TRANSPARENT);
            icon.setBackgroundResource(R.drawable.ic_check_circle_black_36dp);
        } else {
            icon.setTextColor(holder.mTextColor);
            icon.setBackgroundResource(0);
        }
    }

    private class ViewHolder {
        TextView mTitle;
        TextView mArtist;
        TextView mLrcIcon;
        int mTextColor;
        boolean mIsChecked;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) { // init this item view
            convertView = mInflater.inflate(R.layout.song_item, parent, false);
            holder = new ViewHolder();
            holder.mTitle = (TextView) convertView.findViewById(R.id.tv_song_title);
            holder.mArtist = (TextView) convertView.findViewById(R.id.tv_song_artist);
            holder.mLrcIcon = (TextView) convertView.findViewById(R.id.tv_has_lrc);
            holder.mIsChecked = false;
            convertView.setTag(holder);
            mConvertViewMap.put(position, convertView);

            final View view = convertView;
            holder.mLrcIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLrcListener != null) {
                        mLrcListener.OnLrcClick(position, view, parent);
                    }
                }
            });
        } else { // view has been created
            holder = (ViewHolder) convertView.getTag();
        }

        // update every element in this view
        SongItem item = mSongs.get(position);
        holder.mTitle.setText(item.getTitle());
        holder.mArtist.setText(item.getArtist());

        holder.mTextColor = item.isHasLrc() ? Color.argb(255, 0, 162, 232) : Color.LTGRAY;
        TextView lrcIcon = holder.mLrcIcon;
        lrcIcon.setTextColor(holder.mTextColor);
        updateLrcIconView(lrcIcon, holder);

        return convertView;
    }

}
