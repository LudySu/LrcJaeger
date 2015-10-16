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

    // background color for current theme
    private static int BACKGROUND_COLOR = Color.argb(255, 250, 250, 250);
    private static int BACKGROUND_COLOR_CHECKED = Color.argb(255, 235, 235, 235);
    private static int HAS_LRC_COLOR = Color.argb(255, 0, 162, 232);
    private static int NO_LRC_COLOR = Color.LTGRAY;

    private LayoutInflater mInflater = null;
    private ArrayList<SongItem> mSongs;
    private OnLrcClickListener mLrcListener;
    private HashMap<Integer, Boolean> mCheckedStat = new HashMap<>();

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
     * The view must be within visible range
     *
     * @param view
     * @param position
     * @param checked
     */
    public void setItemChecked(View view, int position, boolean checked) {
        ViewHolder holder = (ViewHolder) view.getTag();
        mCheckedStat.put(position, checked);
        updateLrcIconView(holder, checked);
        //Log.v(TAG, "setItemChecked " + checked + ", at pos " + position);
    }

    /**
     * Clear all choices status, including those outside of visible range, so next time when
     * getView() is called that view can be updated.
     */
    public void clearChoices() {
        mCheckedStat.clear();
    }

    private void updateLrcIconView(ViewHolder holder, boolean checked) {
        TextView icon = holder.mLrcIcon;
        if (checked) {
            icon.setTextColor(Color.TRANSPARENT);
            icon.setBackgroundResource(R.drawable.ic_check_circle_black_36dp);
            holder.mItemView.setBackgroundColor(BACKGROUND_COLOR_CHECKED);
        } else {
            icon.setTextColor(holder.mTextColor);
            icon.setBackgroundResource(0);
            holder.mItemView.setBackgroundColor(BACKGROUND_COLOR);
        }
    }

    private class ViewHolder {
        View mItemView;
        TextView mTitle;
        TextView mArtist;
        TextView mLrcIcon;
        int mTextColor;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        // There are only a few convertViews(within visible range) and will be reused to display
        // other items
        if (convertView == null) { // init this item view
            convertView = mInflater.inflate(R.layout.song_item, parent, false);
            holder = new ViewHolder();
            holder.mItemView = convertView;
            holder.mTitle = (TextView) convertView.findViewById(R.id.tv_song_title);
            holder.mArtist = (TextView) convertView.findViewById(R.id.tv_song_artist);
            holder.mLrcIcon = (TextView) convertView.findViewById(R.id.tv_has_lrc);
            convertView.setTag(holder);
        } else { // view has been created
            holder = (ViewHolder) convertView.getTag();
        }

        // update every element in this view
        SongItem item = mSongs.get(position);
        holder.mTitle.setText(item.getTitle());
        holder.mArtist.setText(item.getArtist());

        holder.mTextColor = item.isHasLrc() ? HAS_LRC_COLOR : NO_LRC_COLOR;
        TextView lrcIcon = holder.mLrcIcon;
        lrcIcon.setTextColor(holder.mTextColor);
        Boolean b = mCheckedStat.get(position);
        boolean checked = b == null ? false : b;
        updateLrcIconView(holder, checked);

        final View view = convertView;
        holder.mLrcIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLrcListener != null) {
                    mLrcListener.OnLrcClick(position, view, parent);
                }
            }
        });

        return convertView;
    }

}
