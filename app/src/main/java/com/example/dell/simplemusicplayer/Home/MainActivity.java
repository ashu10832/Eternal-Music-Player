package com.example.dell.simplemusicplayer.Home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.dell.simplemusicplayer.Model.AudioTrack;
import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.Model.SongLoader;
import com.example.dell.simplemusicplayer.R;
import com.example.dell.simplemusicplayer.SongPlaying.SongPlayingActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements HomeContract.HomeView {

    ArrayList<Song> songList = null;
    RecyclerView recyclerView;
    SongAdapter songAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSION_READ_EXTERNAL_STORAGE = 918;
    HomePresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presenter = new HomePresenter(new SongLoader(this), this);
        askForPermission();

    }


    void askForPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_READ_EXTERNAL_STORAGE);
        } else {
            init();
            presenter.getAllSongs();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                    presenter.getAllSongs();

                    break;
                } else {
                    Toast.makeText(this, "Permission Denied! Closing App!", Toast.LENGTH_SHORT).show();
                    finish();
                    System.exit(0);
                    break;
                }
        }
    }

    void init() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.getAllSongs();
            }
        });
        songAdapter = new SongAdapter(this, songList, new SongAdapter.onSongClickListener() {
            @Override
            public void onSongClick(int pos) {
                presenter.onSongClicked(pos);
            }
        });
        recyclerView.setAdapter(songAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void showSongList(ArrayList<Song> songArrayList) {
        songList = songArrayList;
        songAdapter.setSongList(songArrayList);
    }

    @Override
    public void switchToPlayingActivity(int position) {
        Log.i(TAG, "switchToPlayingActivity: Position:" + position);
        Intent i = new Intent(MainActivity.this, SongPlayingActivity.class);
        Log.i(TAG, "switchToPlayingActivity: " + songList);
        ArrayList<AudioTrack> songs = new ArrayList<>();
        for (Song song: songList) {
            songs.add(new AudioTrack(song.getData()));
        }
        i.putExtra("SongList", songs);
        i.putExtra("SelectedSongPosition",songList.get(position).getData());
        startActivity(i);
    }

    @Override
    public void showTitle(String title) {
        setTitle(title);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRefresh() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideRefresh() {
        swipeRefreshLayout.setRefreshing(false);

    }
}
