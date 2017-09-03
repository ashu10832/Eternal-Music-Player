package com.example.dell.simplemusicplayer.Home;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.simplemusicplayer.Model.AudioTrack;
import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.Model.SongLoader;
import com.example.dell.simplemusicplayer.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements HomeContract.HomeView {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSION_READ_EXTERNAL_STORAGE = 918;
    ArrayList<Song> songList = null;
    RecyclerView recyclerView;
    SongAdapter songAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    SlidingUpPanelLayout panelLayout;
    HomePresenter presenter;
    RelativeLayout umanoBar;
    ImageView umanoBarThumbnail;
    ImageView umanoBarIcon;
    TextView umanoBarTitle;
    TextView umanoBarArtist;
    boolean isPlaying = false;
    ArrayList<AudioTrack> songs = null;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaMetadataCompat metadata = intent.getParcelableExtra("metadata");
            Log.d(TAG, "onReceive: Metadata");
            setMetaData(metadata);
        }
    };

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
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_up_layout);
        umanoBar = (RelativeLayout) findViewById(R.id.umano_bar);
        umanoBarThumbnail = (ImageView) findViewById(R.id.umano_bar_thumbnail);
        umanoBarIcon = (ImageView) findViewById(R.id.umano_bar_icon);
        umanoBarArtist = (TextView) findViewById(R.id.umano_bar_artist);
        umanoBarTitle = (TextView) findViewById(R.id.umano_bar_title);

        umanoBarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    presenter.onPause();
                    umanoBarIcon.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    isPlaying = false;
                } else {
                    presenter.onPlay();
                    umanoBarIcon.setImageResource(R.drawable.ic_pause_black_24dp);
                    isPlaying = true;
                }
            }
        });

        panelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide: Offset:" + slideOffset);

                final float[] from = new float[3], to = new float[3];
                final float[] hsv = new float[3];
                Color.colorToHSV(Color.parseColor("#FFFFFF"), from);   // from white
                Color.colorToHSV(Color.parseColor("#FF4081"), to); // transition color


                ColorUtils.blendHSL(from, to, slideOffset, hsv);
                umanoBar.setBackgroundColor(Color.HSVToColor(hsv));

                umanoBarIcon.setAlpha(1 - slideOffset);
                umanoBarThumbnail.setAlpha(1 - slideOffset);

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

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
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("METADATA-UPDATED"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void showSongList(ArrayList<Song> songArrayList) {
        songList = songArrayList;
        songAdapter.setSongList(songArrayList);
        songs = new ArrayList<>();
        songs.clear();
        for (Song song : songList) {
            songs.add(new AudioTrack(song.getData()));
        }
        presenter.attachManager(this, songs);
    }

    @Override
    public void switchToPlayingActivity(int position) {
        Log.i(TAG, "switchToPlayingActivity: Position:" + position);
        presenter.startPlaying(songList.get(position).getData());
        umanoBarIcon.setImageResource(R.drawable.ic_pause_black_24dp);
        isPlaying = true;


        //  Intent i = new Intent(MainActivity.this, SongPlayingActivity.class);
        // Log.i(TAG, "switchToPlayingActivity: " + songList);

        //i.putExtra("SongList", songs);
        //i.putExtra("SelectedSongPosition",songList.get(position).getData());
        //  startActivity(i);
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

    @Override
    public void setMetaData(MediaMetadataCompat mediaMetadataCompat) {
        umanoBarThumbnail.setImageBitmap(mediaMetadataCompat.getBitmap(MediaMetadataCompat.METADATA_KEY_ART));
        umanoBarTitle.setText(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        umanoBarArtist.setText(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
    }
}
