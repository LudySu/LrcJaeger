package orz.ludysu.lrcjaeger;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * 
 * @param <T>
 */
public class UiHandler<T extends Activity> extends Handler {
    WeakReference<T> mActivity = null;

    public UiHandler(T activity) {
        mActivity = new WeakReference<>(activity);
    }

    public T getActivity() {
        T a = mActivity.get();
        if (a == null) {
            Log.e("UiHandler", "Cannot handle message: activity has been destroyed");
        }
        return a;
    }
}
