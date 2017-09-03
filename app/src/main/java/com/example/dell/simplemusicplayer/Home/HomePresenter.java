package com.example.dell.simplemusicplayer.Home;

import android.content.Context;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.support.v4.media.MediaMetadataCompat;
import android.view.View;

import com.example.dell.simplemusicplayer.Model.AudioTrack;
import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.Model.SongLoader;
import com.example.dell.simplemusicplayer.MusicServiceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Handler;

import needle.Needle;
import needle.UiRelatedTask;

import static android.R.id.home;
import static android.R.id.list;

/**
 * Created by ashugupta on 02/06/17.
 */

public class HomePresenter implements HomeContract.HomePresenter {
    private SongLoader songLoader;
    private HomeContract.HomeView view;
    ArrayList<Song> songArrayList;
    MusicServiceManager manager;



    HomePresenter(SongLoader songLoader, HomeContract.HomeView homeView)
    {
        this.songLoader = songLoader;
        view = homeView;
        view.showTitle("Music Library");
    }

    void attachManager(Context context, ArrayList<AudioTrack> songList){
        manager = new MusicServiceManager(context, songList);
    }

    void startPlaying(String songData) {
        if (songData != null) {
            manager.startPlayingFromData(songData);
        }
    }


    @Override
    public void getAllSongs() {
        view.showRefresh();
        Needle.onBackgroundThread().execute(new UiRelatedTask<ArrayList<Song>>() {
            @Override
            protected ArrayList<Song> doWork() {
                songArrayList = songLoader.getSongs();
                Collections.sort(songArrayList, new Comparator<Song>(){
                    public int compare(Song a, Song b){
                        return a.getTitle().compareTo(b.getTitle());
                    }
                });
                return songArrayList;
            }

            @Override
            protected void thenDoUiRelatedWork(ArrayList<Song> songArrayList) {
                if(songArrayList!=null)
                    view.showSongList(songArrayList);
                else {
                    view.showMessage("Unable to Load Music! Try Again");
                }
                view.hideRefresh();
            }
        });
    }

    void disconnect() {
        manager.unbindToService();
    }

    public void onPlay() {
        manager.play();
    }


    public void onPause() {
        manager.pause();
    }



    public void setMetaData(MediaMetadataCompat mediaMetadataCompat) {
        view.setMetaData(mediaMetadataCompat);
    }

    @Override
    public void onSongClicked(int position) {
        view.switchToPlayingActivity(position);
    }
}
