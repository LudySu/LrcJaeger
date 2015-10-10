package orz.ludysu.lrcjaeger;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class MultiChoiceListView extends ListView {

    public OnItemCheckedListener mListener;

    private int mCheckedCount = 0;

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
        super.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    public MultiChoiceListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    public MultiChoiceListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    public void setItemChecked (int position, boolean value) {
        super.setItemChecked(position, value);
        if (value) {
            mCheckedCount++;
        } else {
            mCheckedCount--;
        }

        Log.v("ListView", "checked items " + super.getCheckedItemPositions());
        View view = getChildAtAbsolutePos(position);
        updateViewAtPosition(view, position);

        if (mListener != null) {
            mListener.onItemCheckedStateChanged(view, position, value);
            if (mCheckedCount == 0) {
                mListener.onNothingChecked();
            }
        }
    }

    private View getChildAtAbsolutePos(int position) {
        int visiblePosition = super.getFirstVisiblePosition();
        return getChildAt(position - visiblePosition);
    }

    public int getCheckedCount() {
        return mCheckedCount;
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

    public OnItemCheckedListener getOnItemCheckedListener() {
        return mListener;
    }

    @Override
    public void clearChoices() {
        super.clearChoices();
        mCheckedCount = 0;
    }

    private void updateViewAtPosition(View view, int position) {
        super.getAdapter().getView(position, view, this);
    }
}
