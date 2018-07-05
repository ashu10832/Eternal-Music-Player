package com.example.dell.simplemusicplayer.Home;

import android.os.Handler;
import android.support.v4.media.session.PlaybackStateCompat;

import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.Model.SongLoader;
import com.example.dell.simplemusicplayer.MusicServiceManager;
import com.example.dell.simplemusicplayer.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import needle.Needle;
import needle.UiRelatedTask;

/**
 * Created by ashugupta on 02/06/17.
 */

public class HomePresenter implements HomeContract.HomePresenter {
    private SongLoader songLoader;
    private HomeContract.HomeView view;
    private ArrayList<Song> songArrayList;
    private MusicServiceManager manager;
    private SharedPreferenceManager preferenceManager;



    HomePresenter(SongLoader songLoader, HomeContract.HomeView homeView, SharedPreferenceManager preferenceManager)
    {
        this.songLoader = songLoader;
        view = homeView;
        view.showTitle("Music Library");
        this.preferenceManager = preferenceManager;
    }

    void attachManager(MusicServiceManager manager){
        this.manager = manager;
    }

    void startPlaying(String songData) {
        if (songData != null) {
            manager.startPlayingFromData(songData);
            preferenceManager.setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            preferenceManager.setCurrentSong(songData);
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
        preferenceManager.setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
    }


    public void onPause() {
        manager.pause();
        preferenceManager.setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
    }

    boolean isSongNull() {
        String data = preferenceManager.getCurrentSong();
        return data == null;
    }


    @Override
    public void onSongClicked(int position) {
        view.switchToPlayingActivity(position);
    }

    public String getCurrentSong() {
        return preferenceManager.getCurrentSong();
    }

    public boolean isPlaying() {
        return preferenceManager.getPlaybackState()==PlaybackStateCompat.STATE_PLAYING;
    }
}
