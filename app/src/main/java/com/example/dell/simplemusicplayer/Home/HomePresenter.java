package com.example.dell.simplemusicplayer.Home;

import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.Model.SongLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Handler;

import needle.Needle;
import needle.UiRelatedTask;

import static android.R.id.list;

/**
 * Created by ashugupta on 02/06/17.
 */

public class HomePresenter implements HomeContract.HomePresenter {
    private SongLoader songLoader;
    private HomeContract.HomeView view;
    ArrayList<Song> songArrayList;


    HomePresenter(SongLoader songLoader, HomeContract.HomeView homeView)
    {
        this.songLoader = songLoader;
        view = homeView;
        view.showTitle("Music Library");
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

    @Override
    public void onSongClicked(Song song) {
        view.switchToPlayingActivity(song);
    }
}
