package com.example.dell.simplemusicplayer.SongPlaying;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.dell.simplemusicplayer.Home.MainActivity;
import com.example.dell.simplemusicplayer.Model.AudioTrack;
import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.MusicPlayingService;
import com.example.dell.simplemusicplayer.MusicServiceManager;
import com.example.dell.simplemusicplayer.Utils;

import java.util.ArrayList;

/**
 * Created by ashugupta on 14/06/17.
 */

class SongPlayingPresenter implements SongPlayingContract.SongPlayingPresenter {
    private MusicPlayingService musicService;
    private SongPlayingContract.SongPlayingView view;
    MusicServiceManager manager;


    SongPlayingPresenter(SongPlayingActivity view,ArrayList<AudioTrack> songList,String songData){
        this.view =  view;
        manager = new MusicServiceManager(view,songList,songData);
    }

    /*void attach(MusicPlayingService service) {
        this.musicService = service;
    }

    void detach() {
        this.musicService = null;
        this.view = null;
    }*/

    void startPlaying(String songData){
        if (songData!=null){
            manager.startPlayingFromData(songData);
        }
    }

    @Override
    public void onPlay() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void seekMusicTo(int progress) {
    }

    @Override
    public void setCurrentPosition(int progress) {
        view.setCurrentPosition(Utils.getFormattedTime(progress));
    }

    @Override
    public void setMusicList(ArrayList<AudioTrack> songList) {
        if (songList!=null){
            manager.setMusicList(songList);
        }
    }

    public void disconnect() {
        manager.unbindToService();
    }
}
