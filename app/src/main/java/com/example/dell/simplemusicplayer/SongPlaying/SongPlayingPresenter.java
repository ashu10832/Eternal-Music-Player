package com.example.dell.simplemusicplayer.SongPlaying;

import android.support.v4.media.MediaMetadataCompat;

import com.example.dell.simplemusicplayer.Model.AudioTrack;
import com.example.dell.simplemusicplayer.MusicManagerContract;
import com.example.dell.simplemusicplayer.MusicServiceManager;

import java.util.ArrayList;

/**
 * Created by ashugupta on 14/06/17.
 */

class SongPlayingPresenter implements SongPlayingContract.SongPlayingPresenter, MusicManagerContract {
    private SongPlayingContract.SongPlayingView view;
    private MusicServiceManager manager;


    SongPlayingPresenter(SongPlayingActivity view, ArrayList<AudioTrack> songList) {
        this.view = view;
        manager = new MusicServiceManager(view, songList);
    }

    void startPlaying(String songData) {
        if (songData != null) {
            manager.startPlayingFromData(songData);
        }
    }


    @Override
    public void onPlay() {
        manager.play();
    }

    @Override
    public void onPause() {
        manager.pause();
    }

    @Override
    public void seekMusicTo(int progress) {
        manager.seekTo(progress);
    }



    void disconnect() {
        manager.unbindToService();
    }

    @Override
    public void setMetaData(MediaMetadataCompat mediaMetadataCompat) {
        view.setMetaData(mediaMetadataCompat);
    }

    int getCurrentPosition() {
        return manager.getCurrentPosition();
    }
}
