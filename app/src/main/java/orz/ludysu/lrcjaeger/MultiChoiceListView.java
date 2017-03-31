package orz.ludysu.lrcjaeger;

import android.content.Context;
import android.support.v7.view.ActionMode;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Because we target SDK 9 so need to take care of multi choices by ourselves, which is introduced
 * from SDK 11
 */
public class MultiChoiceListView extends ListView {
    private static final String TAG = "MultiChoiceListView";

    private OnItemCheckedListener mListener;
    private SparseBooleanArray mCheckedStatus = new SparseBooleanArray();

    public interface OnItemCheckedListener extends ActionMode.Callback {
        /**
         * Called when an item is checked or unchecked during selection mode.
         *
         * @param mode ActionMode object
         * @param position Adapter position of the item that was checked or unchecked
         * @param checked <code>true</code> if the item is now checked, <code>false</code>
         *                if the item is now unchecked.
         */
        public void onItemCheckedStateChanged(ActionMode mode, int position, boolean checked);

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
                toggleItemChecked(position);
            }
        });

    }

    @Override
    public void setItemChecked(int position, boolean value) {
        if (value) {
            mCheckedStatus.put(position, value);
        } else {
            mCheckedStatus.delete(position);
        }

        View view = getChildAtAbsolutePos(position);
        updateViewAtPosition(view, position, value);

        if (mListener != null) {
            mListener.onItemCheckedStateChanged(null, position, value);
            if (getCheckedItemCount() == 0) {
                mListener.onNothingChecked();
            }
        }
    }

    @Override
    public int getCheckedItemCount() {
        return mCheckedStatus.size();
    }

    @Override
    public SparseBooleanArray getCheckedItemPositions () {
        return mCheckedStatus;
    }

    @Override
    public boolean isItemChecked(int position) {
        return mCheckedStatus.get(position);
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
        mCheckedStatus.clear();
        SongItemAdapter adapter = (SongItemAdapter) getAdapter();
        adapter.clearChoices();

        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View v = getChildAtAbsolutePos(i);
            updateViewAtPosition(v, i, false);
        }
    }

    /**
     * If position > getLastVisiblePosition(), it will return null
     *
     * @param position
     * @return
     */
    private View getChildAtAbsolutePos(int position) {
        int visibleStart = getFirstVisiblePosition();
        return getChildAt(position - visibleStart);
    }

    private void updateViewAtPosition(View view, int position, boolean checked) {
        SongItemAdapter adapter = (SongItemAdapter) getAdapter();
        if (view != null) { // only update views within visible range
            adapter.setItemChecked(view, position, checked);
            super.getAdapter().getView(position, view, this);
        }
    }
}
