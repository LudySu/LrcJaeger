package com.example.lrcjaeger;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class LrcJaeger extends Activity {
    private static final String TAG = "LrcJaeger";
    HandlerThread mHandlerThread = new HandlerThread("network");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lrc_jaeger);
        
        mHandlerThread.start();
        LrcHandler handler = new LrcHandler(mHandlerThread.getLooper());
        
        handler.sendEmptyMessage(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_lrc_jaeger, menu);
        return true;
    }
    
    
    private class  LrcHandler extends Handler {
        public LrcHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
            String result = TTDownloader.queryLyrics(null, null);
            Log.d(TAG, "result is " + result);
        }
    };
    
    private void test() {
//        System.out.println("Code = 108159856, Id = 172943, Artist = alan, Title = diamond");
//
//        try {
//            URL url = new URL("http://ttlrcct.qianqian.com/dll/lyricsvr.dll?sh?Artist" +
//                    "=790075006900&Title=61006700610069006E00&Flags=0");
//            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//            String inputLine;
//            while ((inputLine = in.readLine()) != null)
//                System.out.println(inputLine);
//            in.close();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Code = " + LrcUtils.computeCode("alan", "Diamond", 172943));
    }

}
