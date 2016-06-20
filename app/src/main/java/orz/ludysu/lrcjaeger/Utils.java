package orz.ludysu.lrcjaeger;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class Utils {

    private static final String TAG = "Utils";

    public static final String PREFERENCE_NAME = "prefs";
    public static final String HIDE_FOLDER_PREF_KEY = "hide_folders";

    private static Set<Integer> sHiddenFolders = null;
    private static int sChangeCount = 0;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     *  Checks if external storage is available for read and write
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     *  Checks if external storage is available to at least read
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String readFile(String pathname) throws IOException {
        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }

    public static String getFolder(String path) {
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash > 0) {
            return path.substring(0, lastSlash);
        }
        return null;
    }

    public static String getFileNameWithoutExtension(String path) {
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash <= 0) {
            return "";
        }
        int lastDot = path.lastIndexOf(".");
        lastDot = lastDot > lastSlash ? lastDot : path.length();
        return path.substring(lastSlash + 1, lastDot);
    }

    /**
     * Load hidden folders set by user from shared prefs
     */
    public static Set<Integer> loadHiddenFoldersFromDisk(Context context) {
        if (sHiddenFolders == null) {
            String folderHash = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE).getString(HIDE_FOLDER_PREF_KEY, null);
            sHiddenFolders = new HashSet<>();
            if (folderHash != null && folderHash.length() > 0) {
                String[] tokens = folderHash.split(",");
                for (String s : tokens) {
                    sHiddenFolders.add(Integer.parseInt(s));
                }
            }
        }
        return sHiddenFolders;
    }

    private static void writeHiddenFolders(Context context, Set<Integer> set) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE).edit();
        StringBuilder sb = new StringBuilder();
        for (int v : set) {
            sb.append(v);
            sb.append(",");
        }
        if (set.size() > 0) {
            sb.deleteCharAt(sb.length() - 1); // delete last ","
        }
        editor.putString(HIDE_FOLDER_PREF_KEY, sb.toString());

        if (Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static void addHiddenFolder(Context context, int folderHash) {
        sChangeCount++;
        sHiddenFolders.add(folderHash);
        if (sChangeCount >= 5) {
            writeHiddenFolders(context, sHiddenFolders);
        }
    }

    public static void removeHiddenFolder(Context context, int folderHash) {
        sChangeCount++;
        sHiddenFolders.remove(folderHash);
        if (sChangeCount >= 5) {
            writeHiddenFolders(context, sHiddenFolders);
        }
    }

}
