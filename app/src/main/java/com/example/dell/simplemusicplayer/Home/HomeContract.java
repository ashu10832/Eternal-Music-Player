package com.example.dell.simplemusicplayer.Home;

import com.example.dell.simplemusicplayer.Model.Song;

import java.util.ArrayList;

/**
 * Created by ashugupta on 02/06/17.
 */

public interface HomeContract {
    interface HomePresenter{
        void getAllSongs();

        void onSongClicked(Song song);
    }
    interface HomeView{


        void showSongList(ArrayList<Song> songArrayList);

        void switchToPlayingActivity(Song song);

        void showTitle(String title);

        void showMessage(String message);

        void showRefresh();

        void hideRefresh();



    }
}
