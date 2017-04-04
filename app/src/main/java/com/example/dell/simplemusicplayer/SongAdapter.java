package com.example.dell.simplemusicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static android.R.id.list;

/**
 * Created by Dell on 02-Feb-17.
 */

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs=theSongs;
        songInf=LayoutInflater.from(c);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLayout = (LinearLayout)songInf.inflate(R.layout.list_item,parent,false);
        TextView title = (TextView)songLayout.findViewById(R.id.title);
        TextView artist = (TextView)songLayout.findViewById(R.id.artist);

        title.setText(songs.get(position).getTitle());
        artist.setText(songs.get(position).getArtist());
        songLayout.setTag(position);
        return songLayout;
    }
    @Override
    public int getCount() {
        return  songs.size();
    }


    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
