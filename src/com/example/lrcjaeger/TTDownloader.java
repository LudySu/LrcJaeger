package com.example.lrcjaeger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Utils for quering and getting lyrics from qianqianjingting server.
 */
public class TTDownloader {
    private static final String SERVER_URL = "http://ttlrcct.qianqian.com/dll/lyricsvr.dll?";
    private static final String TAG = "LrcJaeger/TTDownloader";
    
    
    /** 
     * Convert character string to hex string.
     * @param str string to convert
     * */
    private static String toHexString(String str) {
        if (str == null) {
            return null;
        }

        byte[] tmp = str.getBytes();
        StringBuilder sb = new StringBuilder();
        for (byte ch : tmp) {
            sb.append(Integer.toHexString(ch));
        }
        return sb.toString();
    }
    
    /**
     * Parameters must be encoded in utf8.
     * @param artist can be null
     * @param title
     * @return url for quering
     */
    private static String buildQueryUrl(String artist, String title) {
        return SERVER_URL + "sh?Artist=" + artist + "&Title=" + title;
    }
    
    /**
     * Parameters must be encoded in utf8.
     * @return url for downloading
     */
    private static String buildDownloadUrl(int lrcId, int code ) {
        return SERVER_URL + "dl?Id=" + lrcId + "&Code=" + code;
    }
    
    /**
     * Query for lyrics, parameters must be encoded in utf8.
     * @param artist can be null
     * @param title
     * @return
     */
    public static String queryLyrics(String artist, String title) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httppost = new HttpGet("http://ttlrccnc.qianqian.com/dll/lyricsvr.dll?sh?Artist=954EE353D5889999&Title=670072006F00770073006C006F0077006C007900");

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            HttpEntity ht = response.getEntity();

                BufferedHttpEntity buf = new BufferedHttpEntity(ht);

                InputStream is = buf.getContent();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                return total.toString();

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    /**
     * Compute code for a given lrcId. Must call {@link #queryLyrics(String, String)} 
     * first, which returns a XML file containing all parameters.
     * @param artist attribute in the XML file, not the artist used in querying
     * @param title attribute in the XML file, not the title used in querying
     * @param lrcId attribute in the XML file, unique id
     * @return
     */
    public static String computeCode(String artist, String title, int lrcId) {
        String qqHexStr = toHexString(artist + title);
        int length = qqHexStr.length() / 2;
        int[] song = new int[length];
        for (int i = 0; i < length; i++) {
            song[i] = Integer.parseInt(qqHexStr.substring(i * 2, i * 2 + 2), 16);
        }
        
        int t1 = 0, t2 = 0, t3 = 0;
        t1 = (lrcId & 0x0000FF00) >> 8;
        if ((lrcId & 0x00FF0000) == 0) {
            t3 = 0x000000FF & ~t1;
        } else {
            t3 = 0x000000FF & ((lrcId & 0x00FF0000) >> 16);
        }

        t3 = t3 | ((0x000000FF & lrcId) << 8);
        t3 = t3 << 8;
        t3 = t3 | (0x000000FF & t1);
        t3 = t3 << 8;
        if ((lrcId & 0xFF000000) == 0) {
            t3 = t3 | (0x000000FF & (~lrcId));
        } else {
            t3 = t3 | (0x000000FF & (lrcId >> 24));
        }

        int j = length - 1;
        while (j >= 0) {
            int c = song[j];
            if (c >= 0x80) c = c - 0x100;

            t1 = (int)((c + t2) & 0x00000000FFFFFFFF);
            t2 = (int)((t2 << (j % 2 + 4)) & 0x00000000FFFFFFFF);
            t2 = (int)((t1 + t2) & 0x00000000FFFFFFFF);
            j -= 1;
        }
        j = 0;
        t1 = 0;
        while (j <= length - 1) {
            int c = song[j];
            if (c > 128) c = c - 256;
            int t4 = (int)((c + t1) & 0x00000000FFFFFFFF);
            t1 = (int)((t1 << (j % 2 + 3)) & 0x00000000FFFFFFFF);
            t1 = (int)((t1 + t4) & 0x00000000FFFFFFFF);
            j += 1;
        }

        int t5 = (int)Conv(t2 ^ t3);
        t5 = (int)Conv(t5 + (t1 | lrcId));
        t5 = (int)Conv(t5 * (t1 | t3));
        t5 = (int)Conv(t5 * (t2 ^ lrcId));

        long t6 = (long)t5;
        if (t6 > 2147483648L)
            t5 = (int)(t6 - 4294967296L);
        return String.valueOf(t5);
    }
    
    private static long Conv(int i) {
        long r = i % 4294967296L;
        if (i >= 0 && r > 2147483648L)
            r = r - 4294967296L;

        if (i < 0 && r < 2147483648L)
            r = r + 4294967296L;
        return r;
    }
}
