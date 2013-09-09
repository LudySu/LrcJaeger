package com.example.lrcjaeger;



import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SongItemAdapter extends ArrayAdapter<SongItem> {
    private LayoutInflater mInflater = null;
    ArrayList<SongItem> mSongs;

    public SongItemAdapter(Context context, ArrayList<SongItem> songs) {
        super(context, 0, songs);
        mInflater = LayoutInflater.from(context);
        mSongs = songs;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.song_item, parent, false);
        }
        
        SongItem song = mSongs.get(position);
        
        TextView songName = (TextView) convertView.findViewById(R.id.tv_song_name);
        songName.setText(song.getTitle());

//        ImageView fileIcon = (ImageView) convertView.findViewById(R.id.img_file_icon);
//        fileIcon.setImageResource(currentFile.getIconResId());
        
        return convertView;
    }

}
