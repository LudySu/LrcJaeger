package orz.ludysu.lrcjaeger;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;


public class Utils {

    private static final String TAG = "Utils";

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
    public static Set<Integer> getHiddenFoldersFromPreference(Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences(HideFoldersActivity.PREFERENCE_NAME,
                Activity.MODE_PRIVATE);
        String folderHash = settings.getString(HideFoldersActivity.HIDE_FOLDER_PREF_KEY, null);
        Set<Integer> set = new HashSet<>();
        if (folderHash != null && folderHash.length() > 0) {
            String[] tokens = folderHash.split(",");
            for (String s : tokens) {
                set.add(Integer.parseInt(s));
            }
        }
        return set;
    }
}
