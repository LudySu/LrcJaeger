package orz.ludysu.lrcjaeger;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
    private ActionMode.Callback mActionModeListener;

    public MultiChoiceFacade(AppCompatActivity context, MultiChoiceListView listView) {
        mActivity = context;
        mListView = listView;
        mAdapter = new SongItemAdapter(context, new ArrayList<SongItem>());

        mListView.setAdapter(mAdapter);

        mListView.setOnItemCheckedListener(new MultiChoiceListView.OnItemCheckedListener() {
            @Override
            public void onItemCheckedStateChanged(View view, int position, boolean checked) {
                Log.v(TAG, "onItemCheckedStateChanged " + position + " - " + checked);
                if (mActionMode == null) {
                    mActionMode = mActivity.startSupportActionMode(mActionModeCallback);
                }
            }

            @Override
            public void onNothingChecked() {
                Log.v(TAG, "onNothingChecked");
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
        });

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

    public void setMultiChoiceModeListener(ActionMode.Callback callback) {
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

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.activity_lrc_jaeger_contextual, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete_all_lrc:
                    return true;

                case R.id.action_downall_context:
                    return true;

                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mListView.clearChoices();
        }
    };
}
