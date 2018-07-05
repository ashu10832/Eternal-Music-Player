package com.example.dell.simplemusicplayer.SongPlaying;

import android.support.v4.media.MediaMetadataCompat;

import com.example.dell.simplemusicplayer.Model.AudioTrack;
import com.example.dell.simplemusicplayer.Model.Song;

import java.util.ArrayList;

/**
 * Created by ashugupta on 14/06/17.
 */

public interface SongPlayingContract {



    interface SongPlayingView{



        void setMetaData(MediaMetadataCompat mediaMetadataCompat);

        void setCurrentPosition(int position);




    }
    interface SongPlayingPresenter{

        void onPlay();

        void onPause();

        void seekMusicTo(int progress);



    }
}
