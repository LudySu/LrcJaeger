package com.example.lrcjaeger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * Utils for quering and getting lyrics from qianqianjingting server.
 */
public class TTDownloader {
    private static final String TAG = "LrcJaeger/TTDownloader";
    
    private static final String SERVER_URL = "http://ttlrccnc.qianqian.com/dll/lyricsvr.dll?";
    private static final String SERVER_URL_CT =  "http://ttlrccct2.qianqian.com/dll/lyricsvr.dll?";

    private static final char[] DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
        'B', 'C', 'D', 'E', 'F' };
    
    /** 
     * Convert character string to hex string.
     * @param str string to convert
     */
    private static String toHexString(String str) {
        String newStr = filterString(str);
        Log.v(TAG, "toHexString from " + newStr);
        byte[] strBytes = null;
        try {
            strBytes = newStr.toLowerCase().getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (strBytes == null) {
            throw new IllegalArgumentException("cannot get hex string from " + newStr);
        }
        
        StringBuilder builder = new StringBuilder();  
        char[] tmp = new char[2];
        for (byte byteValue : strBytes) {
            tmp[0] = DIGIT[(byteValue >>> 4) & 0X0F];
            tmp[1] = DIGIT[byteValue & 0X0F];
            builder.append(tmp);
        }
        
        return builder.toString();
    }
    
    private static String filterString(String str) {
        // 转小写
        str = str.toLowerCase();
        // 去括号，大中小还有全角的小括号
        str = str.replaceAll("[\\[\\]{}\\(\\)（）]+", "");
        // 去除半角特殊符号，空格，逗号，etc
        str = str.replaceAll("[\\s\\/:\\@\\`\\~\\-,\\.]+", "");
        // 去除全角特殊符号
        str = str.replaceAll("[\u2014\u2018\u201c\u2026\u3001\u3002\u300a\u300b\u300e\u300f\u3010\u3011" + 
                    "\u30fb\uff01\uff08\uff09\uff0c\uff1a\uff1b\uff1f\uff5e\uffe5]+", "");
        // TODO　简繁转换
        return str;
    }
    

    /**
     * Parameters must be encoded in utf8.
     * @param artist can be null
     * @param title
     * @return url for quering
     */
    private static String buildQueryUrl(String artist, String title) {
        String titleHex = toHexString(title);
        String aritistHex = toHexString(artist);
        String url = SERVER_URL + "sh?Artist=" + aritistHex + "&Title=" + titleHex;
        Log.v(TAG, "query url is " + url);
        return url;
    }
    
    /**
     * @return url for downloading
     */
    private static String buildDownloadUrl(int lrcId, int code) {
        return SERVER_URL + "dl?Id=" + lrcId + "&Code=" + code;
    }
    
    public static String download(QueryResult item) {
        try {
            int code = computeCode(item.mId, item.mArtist, item.mTitle);
            String url = buildDownloadUrl(item.mId, code);

            String result = getHttpResponse(url);
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static boolean download(QueryResult item, String lrcPath) {
        try {
            int code = computeCode(item.mId, item.mArtist, item.mTitle);
            String url = buildDownloadUrl(item.mId, code);
            String result = getHttpResponse(url);
            
            FileWriter fstream = new FileWriter(lrcPath, false);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(result);
            out.close();
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    /**
     * Query for lyrics, parameters must be encoded in utf8.
     * @param artist can be null
     * @param title
     * @return
     */
    public static ArrayList<QueryResult> query(String artist, String title) {
        ArrayList<QueryResult> result = new ArrayList<QueryResult>();
        String xml = getHttpResponse(buildQueryUrl(artist, title));
        if (xml == null) {
            Log.e(TAG, "Error: cannot get xml response from server");
            return null;
        }
        Log.v(TAG, "xml is " + xml);

        Document doc = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            doc = builder.parse(is);
            NodeList nl = doc.getElementsByTagName(QueryResult.ITEM_LRC);
    
            for (int i = 0; i < nl.getLength(); i++) {
                Element elem = (Element) nl.item(i);
                String id = elem.getAttribute(QueryResult.ATTRIBUTE_ID);
                String art = elem.getAttribute(QueryResult.ATTRIBUTE_ARTIST);
                String tit = elem.getAttribute(QueryResult.ATTRIBUTE_TITLE);
                result.add(new QueryResult(Integer.parseInt(id), art, tit));
                Log.v(TAG, "======== id = " + id + ", art = " + art + ", title = " + tit);
            }
        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
        }
        return result;
    }
    
    private static String getHttpResponse(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httppost = new HttpGet(url);
        
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
                total.append(line + "\n");
            }
            return total.toString();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Compute code for a given lrcId. Must call {@link #query(String, String)} 
     * first, which returns a XML file containing all parameters.
     * @param lrcId attribute in the XML file, unique id
     * @param artist attribute in the XML file, not the artist used in querying
     * @param title attribute in the XML file, not the title used in querying
     * @return
     */
    private static int computeCode(int lrcId, String artist, String title)
            throws UnsupportedEncodingException {
        byte[] bytes = (artist + title).getBytes("UTF-8");
        int[] song = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            song[i] = bytes[i] & 0xff;
        }
        int intVal1 = 0, intVal2 = 0, intVal3 = 0;
        intVal1 = (lrcId & 0xFF00) >> 8;
        if ((lrcId & 0xFF0000) == 0) {
            intVal3 = 0xFF & ~intVal1;
        } else {
            intVal3 = 0xFF & ((lrcId & 0x00FF0000) >> 16);
        }
        intVal3 = intVal3 | ((0xFF & lrcId) << 8);
        intVal3 = intVal3 << 8;
        intVal3 = intVal3 | (0xFF & intVal1);
        intVal3 = intVal3 << 8;
        if ((lrcId & 0xFF000000) == 0) {
            intVal3 = intVal3 | (0xFF & (~lrcId));
        } else {
            intVal3 = intVal3 | (0xFF & (lrcId >> 24));
        }
        int uBound = bytes.length - 1;
        while (uBound >= 0) {
            int c = song[uBound];
            if (c >= 0x80)
                c = c - 0x100;
            intVal1 = c + intVal2;
            intVal2 = intVal2 << (uBound % 2 + 4);
            intVal2 = intVal1 + intVal2;
            uBound -= 1;
        }
        uBound = 0;
        intVal1 = 0;
        while (uBound <= bytes.length - 1) {
            int c = song[uBound];
            if (c >= 128)
                c = c - 256;
            int intVal4 = c + intVal1;
            intVal1 = intVal1 << (uBound % 2 + 3);
            intVal1 = intVal1 + intVal4;
            uBound += 1;
        }
        int intVal5 = intVal2 ^ intVal3;
        intVal5 = intVal5 + (intVal1 | lrcId);
        intVal5 = intVal5 * (intVal1 | intVal3);
        intVal5 = intVal5 * (intVal2 ^ lrcId);
        return intVal5;
    }

}
