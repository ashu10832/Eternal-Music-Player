package com.example.dell.simplemusicplayer.SongPlaying;

import com.example.dell.simplemusicplayer.Model.Song;

/**
 * Created by ashugupta on 14/06/17.
 */

public interface SongPlayingContract {



    interface SongPlayingView{


        void setTitle(String title);

        void setMetaData(Song song);


    }
    interface SongPlayingPresenter{

        void onPlay();

        void onPause();

    }
}
