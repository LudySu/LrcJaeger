package com.example.lrcjaeger;

public class SongItem {
    private String mTitle;
    private String mArtist;
    private boolean mHasLrc;
    
    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public boolean isHasLrc() {
        return mHasLrc;
    }
    
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public void setHasLrc(boolean mHasLrc) {
        this.mHasLrc = mHasLrc;
    }

    public SongItem(String title, String artist, boolean hasLrc) {
        mTitle = title;
        mArtist = artist;
        mHasLrc = hasLrc;
    }

}
