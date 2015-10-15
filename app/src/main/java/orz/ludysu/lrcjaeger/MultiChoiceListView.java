package orz.ludysu.lrcjaeger;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.HashMap;

/**
 * Because we target SDK 7 so need to take care of multi choice by ourselves, which is introduced
 * from SDK 11
 */
public class MultiChoiceListView extends ListView {
    private static final String TAG = "MultiChoiceListView";

    public OnItemCheckedListener mListener;
    private int mCheckedCount = 0;
    private HashMap<Integer, Boolean> mCheckedStatus = new HashMap<>();

    public interface OnItemCheckedListener {
        /**
         * Called when an item is checked or unchecked during selection mode.
         *
         * @param view the view of the checked item
         * @param position Adapter position of the item that was checked or unchecked
         * @param checked <code>true</code> if the item is now checked, <code>false</code>
         *                if the item is now unchecked.
         */
        public void onItemCheckedStateChanged(View view, int position, boolean checked);

        public void onNothingChecked();
    }

    public MultiChoiceListView(Context context) {
        super(context);
    }

    public MultiChoiceListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiChoiceListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if (!(adapter instanceof SongItemAdapter)) {
            throw new IllegalArgumentException();
        }

        SongItemAdapter s = (SongItemAdapter) adapter;
        s.setLrcClickListener(new SongItemAdapter.OnLrcClickListener() {
            @Override
            public void OnLrcClick(int position, View convertView, ViewGroup parent) {
                Log.v(TAG, "lrc click " + position);
                toggleItemChecked(position);
            }
        });

    }

    @Override
    public void setItemChecked (int position, boolean value) {
        mCheckedStatus.put(position, value);
        if (value) {
            mCheckedCount++;
        } else {
            mCheckedCount--;
        }

        Log.v("ListView", "checked items " + super.getCheckedItemPositions());
        View view = getChildAtAbsolutePos(position);
        updateViewAtPosition(view, position, value);

        if (mListener != null) {
            mListener.onItemCheckedStateChanged(view, position, value);
            if (mCheckedCount == 0) {
                mListener.onNothingChecked();
            }
        }
    }

    @Override
    public int getCheckedItemCount() {
        return mCheckedCount;
    }

    @Override
    public boolean isItemChecked(int position) {
        Boolean b = mCheckedStatus.get(position);
        return b == null ? false : b;
    }

    public void toggleItemChecked(int position) {
        setItemChecked(position, !isItemChecked(position));
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        mListener = listener;
    }

    @Override
    public void clearChoices() {
        mCheckedCount = 0;
        mCheckedStatus.clear();
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View v = getChildAtAbsolutePos(i);
            updateViewAtPosition(v, i, false);
        }
    }

    private View getChildAtAbsolutePos(int position) {
        int visiblePosition = super.getFirstVisiblePosition();
        return getChildAt(position - visiblePosition);
    }

    private void updateViewAtPosition(View view, int position, boolean checked) {
        SongItemAdapter adapter = (SongItemAdapter) getAdapter();
        adapter.setItemChecked(position, checked);
        super.getAdapter().getView(position, view, this);
    }
}
