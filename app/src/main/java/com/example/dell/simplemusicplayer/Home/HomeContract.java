package com.example.dell.simplemusicplayer.Home;

import android.support.v4.media.MediaMetadataCompat;

import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.SongPlaying.SongPlayingContract;

import java.util.ArrayList;

/**
 * Created by ashugupta on 02/06/17.
 */

public interface HomeContract {
    interface HomePresenter{
        void getAllSongs();

        void onSongClicked(int position);
    }
    interface HomeView{


        void showSongList(ArrayList<Song> songArrayList);

        void switchToPlayingActivity(int position);

        void showTitle(String title);

        void showMessage(String message);

        void showRefresh();

        void hideRefresh();


        void setMetaData(MediaMetadataCompat mediaMetadataCompat);
    }
}
