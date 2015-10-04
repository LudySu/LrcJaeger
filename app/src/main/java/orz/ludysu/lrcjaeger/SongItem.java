package orz.ludysu.lrcjaeger;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class SongItem implements Parcelable {
    private String mTitle;
    private String mArtist;
    private String mPath;
    private String mLrcPath;
    private boolean mHasLrc;

    /**
     * @return title of the song, or file name if there is no title
     */
    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    /**
     * @return path of the song file itself
     */
    public String getPath() {
        return mPath;
    }

    /**
     * @return path to lyric file associates with the song
     */
    public String getLrcPath() {
        return mLrcPath;
    }

    public boolean isHasLrc() {
        return mHasLrc;
    }

    public SongItem(String title, String artist, String path) {
        mTitle = title.length() > 0 ? title : Utils.getFileNameWithoutExtension(path);
        mArtist = artist;
        mPath = path;
        
        mLrcPath = getLrcPath(path);
        mHasLrc = mLrcPath == null ? false : (new File(mLrcPath)).exists();
    }

    private SongItem(Parcel in) {
        this(in.readString(), in.readString(), in.readString());
    }
    
    public boolean updateStatus() {
        boolean hasLrc = (new File(mLrcPath)).exists() ? true : false;
        boolean changed = hasLrc == mHasLrc;
        mHasLrc = hasLrc;
        return changed;
    }
    
    public void setArtist(String artist) {
        mArtist = artist;
    }
    
    public void setTitle(String title) {
        mTitle = title;
    }

    private static String getLrcPath(String file) {
        int index = file.lastIndexOf(".");
        if (index != -1 && file.length() > index + 1) {
            return file.substring(0, index) + ".lrc";
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "[title = " + mTitle + ", artist = " + mArtist + ", path = " + mPath + "]";
    }

    public static final Parcelable.Creator<SongItem> CREATOR
            = new Parcelable.Creator<SongItem>() {
        public SongItem createFromParcel(Parcel in) {
            return new SongItem(in);
        }

        public SongItem[] newArray(int size) {
            return new SongItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mArtist);
        dest.writeString(mPath);
    }
}
