package com.example.dell.simplemusicplayer.SongPlaying;

import android.content.Context;

import com.example.dell.simplemusicplayer.MusicPlayingService;

/**
 * Created by ashugupta on 14/06/17.
 */

public class SongPlayingPresenter {
    private MusicPlayingService service;


    void attach(MusicPlayingService service) {
        this.service = service;
    }

    void detach() {
        this.service = null;
    }
}
