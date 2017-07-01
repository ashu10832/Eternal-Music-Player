package com.example.dell.simplemusicplayer.Home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by Dell on 02-Feb-17.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private Context context;
    private ArrayList<Song> songList;
    private onSongClickListener songClickListener;



    public SongAdapter(Context context, ArrayList<Song> list,onSongClickListener songClickListener){
        this.context = context;
        this.songList = list;
        this.songClickListener = songClickListener;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_song_list_item,parent,false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, final int position) {
        final int pos =holder.getAdapterPosition();
        final Song song = songList.get(pos);
        Glide.with(context).load(song.getImageByte()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_song_thumbnail).error(R.drawable.ic_song_thumbnail).into(holder.songThumbnail);
        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());
        holder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songClickListener.onSongClick(pos);
            }
        });

    }



    @Override
    public int getItemCount() {
        if(songList == null)
        {
            return 0;
        }
        else {
            return songList.size();
        }
    }

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
        notifyDataSetChanged();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {

        LinearLayout parentView;
        ImageView songThumbnail;
        TextView songTitle;
        TextView songArtist;

        public SongViewHolder(View itemView) {
            super(itemView);
            parentView = (LinearLayout) itemView.findViewById(R.id.parent_view);
            songThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            songTitle = (TextView) itemView.findViewById(R.id.title);
            songArtist = (TextView) itemView.findViewById(R.id.artist);
        }
    }

    interface onSongClickListener{
        void onSongClick(int position);
    }
}

