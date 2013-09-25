package com.example.lrcjaeger;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

public class BulkDownloadTask extends AsyncTask<SongItem, Integer, Integer> {
    private static final String TAG = "LrcJaeger/Download";
    
    private EventListener mListener;
    
    public interface EventListener {
        public void onFinish(int downloaded);
        public void onProgressUpdate(int progress);
    }
    
    public BulkDownloadTask(EventListener l) {
        mListener = l;
    }
    
    @Override
    protected Integer doInBackground(SongItem... list) { // on an independent thread
        if (list == null || list.length <= 0) {
            Log.w(TAG, "items null");
        }
        int total = list.length;
        int count = 0;
        int downloaded = 0;
        for (SongItem item : list) {
            if (isCancelled()) {
                Log.i(TAG, "download task is canceled, " + (total - count) + " items left");
                break;
            }
            count++;
            ArrayList<QueryResult> lrcs = TTDownloader.query(item.getArtist(), item.getTitle());
            if (lrcs != null && lrcs.size() > 0) {
                boolean result = TTDownloader.download(lrcs, item.getLrcPath(),
                        TTDownloader.DOWNLLOAD_SHORTEST_NAME);
                downloaded = result ? downloaded + 1: downloaded;   
            }
            publishProgress(100 * count / total);
        }
        Log.d(TAG, "downloaded " + downloaded + " of " + total + " items");
        return downloaded;
    }
    
    @Override
    protected void onProgressUpdate(Integer...progress) {
        if (mListener != null) {
            mListener.onProgressUpdate(progress[0]);
        }
    }


    @Override
    protected void onPostExecute(Integer downloaded) {
        if (mListener != null) {
            mListener.onFinish(downloaded);
        }
    }
};
