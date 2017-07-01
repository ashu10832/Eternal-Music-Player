package com.example.dell.simplemusicplayer.SongPlaying;

import com.example.dell.simplemusicplayer.Model.AudioTrack;
import com.example.dell.simplemusicplayer.Model.Song;

import java.util.ArrayList;

/**
 * Created by ashugupta on 14/06/17.
 */

public interface SongPlayingContract {



    interface SongPlayingView{


        void showTitle(String title);

        void setMetaData(Song song);

        void setCurrentPosition(String position);




    }
    interface SongPlayingPresenter{

        void onPlay();

        void onPause();

        void seekMusicTo(int progress);

        void setCurrentPosition(int progress);


        void setMusicList(ArrayList<AudioTrack> songList);
    }
}
