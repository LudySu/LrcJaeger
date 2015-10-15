package orz.ludysu.lrcjaeger;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Handle ListView and Adapter together to provide multi choice functionality
 */
public class MultiChoiceFacade {

    private static final String TAG = "MultiChoiceFacade";

    private MultiChoiceListView mListView;
    private SongItemAdapter mAdapter;
    private AppCompatActivity mActivity;
    private ActionMode mActionMode;
    private AdapterView.OnItemClickListener mListener;
    private MultiChoiceListView.OnItemCheckedListener mActionModeListener;

    public MultiChoiceFacade(AppCompatActivity context, MultiChoiceListView listView) {
        mActivity = context;
        mListView = listView;
        mAdapter = new SongItemAdapter(context, new ArrayList<SongItem>());

        mListView.setAdapter(mAdapter);

        mListView.setOnItemCheckedListener(mActionModeCallback);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // disable item click when items are being checked
                if (mListView.getCheckedItemCount() > 0) {
                    Log.v(TAG, "in check mode, ignore click");
                    return;
                }

                if (mListener != null) {
                    mListener.onItemClick(parent, view, position, id);
                }
            }
        });

        // show popup menu
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListView.getCheckedItemCount() > 0) {
                    Log.v(TAG, "in check mode, ignore long click");
                    return true;
                }

                if (mActionMode != null) {
                    return false;
                }

                mActionMode = mActivity.startSupportActionMode(mActionModeCallback);
                mListView.setItemChecked(position, true);
                return true;
            }
        });
    }

    public void setMultiChoiceModeListener(MultiChoiceListView.OnItemCheckedListener callback) {
        if (callback == null) {
            throw new NullPointerException();
        }
        mActionModeListener = callback;
    }

    public ArrayAdapter getAdapter() {
        return mAdapter;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mListener = listener;
    }

    public SongItem getItem(int position) {
        return mAdapter.getItem(position);
    }

    public ArrayList<SongItem> getCheckedItems() {
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        int size = checked.size();
        ArrayList<SongItem> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.valueAt(i);
            if (value) {
                items.add(mAdapter.getItem(key));
            }
        }
        return items;
    }

    public int getCheckedItemCount() {
        return mListView.getCheckedItemCount();
    }

    private MultiChoiceListView.OnItemCheckedListener mActionModeCallback =
            new MultiChoiceListView.OnItemCheckedListener() {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, boolean checked) {
            //Log.v(TAG, "onItemCheckedStateChanged " + position + " - " + checked);
            if (mActionMode == null) {
                mActionMode = mActivity.startSupportActionMode(mActionModeCallback);
            }
            if (mActionModeListener != null) {
                mActionModeListener.onItemCheckedStateChanged(mActionMode, position, checked);
            }
        }

        @Override
        public void onNothingChecked() {
            Log.v(TAG, "onNothingChecked");
            if (mActionMode != null) {
                mActionMode.finish();
            }
            if (mActionModeListener != null) {
                mActionModeListener.onNothingChecked();
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (mActionModeListener != null) {
                return mActionModeListener.onCreateActionMode(mode, menu);
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (mActionModeListener != null) {
                return mActionModeListener.onPrepareActionMode(mode, menu);
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            boolean res = false;
            if (mActionModeListener != null) {
                res = mActionModeListener.onActionItemClicked(mode, item);
            }
            if (mActionMode != null) {
                mActionMode.finish();
            }
            return res;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mListView.clearChoices();
            if (mActionModeListener != null) {
                mActionModeListener.onDestroyActionMode(mode);
            }
        }
    };
}
