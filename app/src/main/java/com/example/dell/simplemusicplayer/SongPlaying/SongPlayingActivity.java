package com.example.dell.simplemusicplayer.SongPlaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.dell.simplemusicplayer.MetadataManager;
import com.example.dell.simplemusicplayer.Model.AudioTrack;
import com.example.dell.simplemusicplayer.R;
import com.example.dell.simplemusicplayer.Utils;

import java.util.ArrayList;

import static com.example.dell.simplemusicplayer.R.id.artist_song;
import static com.example.dell.simplemusicplayer.R.id.title_song;

public class SongPlayingActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, SongPlayingContract.SongPlayingView {

    private static final String TAG = "SongPlayingActivity";
    Button play, pause;
    TextView title, artist, currentPositionTextView, durationTextView;
    ImageView mediaArt;
    boolean serviceBound = false;
    SeekBar seekBar;
    Handler handler;
    ArrayList<AudioTrack> songList;
    SongPlayingPresenter presenter;
    String mSelectedSongData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_playing);

        getSupportActionBar().hide();
        initializeUI();
        if (savedInstanceState != null) {
            songList = savedInstanceState.getParcelableArrayList("SongList");
            mSelectedSongData = savedInstanceState.getString("SelectedSongPosition");
            Log.i(TAG, "onCreate: ServiceState " + serviceBound);
        } else {
            songList = getIntent().getParcelableArrayListExtra("SongList");
            mSelectedSongData = getIntent().getStringExtra("SelectedSongPosition");
        }
        presenter = new SongPlayingPresenter(this, songList);
        setMetaData(MetadataManager.getMetadata(mSelectedSongData));
        presenter.startPlaying(mSelectedSongData);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("METADATA-UPDATED"));
        Log.i(TAG, "onStart: ");

    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ServiceBound: " + serviceBound);
        presenter.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        handler.removeCallbacks(runnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");

    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaMetadataCompat metadata = intent.getParcelableExtra("metadata");
            Log.d(TAG, "onReceive: Metadata");
            setMetaData(metadata);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState: ");
        outState.putParcelableArrayList("SongList", songList);
        outState.putString("SelectedSongPosition", mSelectedSongData);
        super.onSaveInstanceState(outState);
    }


    void initializeUI() {
        Log.i(TAG, "initialize: ");
        durationTextView = (TextView) findViewById(R.id.duration_textview);
        currentPositionTextView = (TextView) findViewById(R.id.current_time_textview);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        play = (Button) findViewById(R.id.play);
        pause = (Button) findViewById(R.id.pause);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onPlay();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onPause();
            }
        });
        title = (TextView) findViewById(title_song);
        artist = (TextView) findViewById(artist_song);
        mediaArt = (ImageView) findViewById(R.id.image);
        mediaArt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        handler = new Handler();
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setCurrentPosition(presenter.getCurrentPosition());
            handler.postDelayed(this, 1000);
        }
    };



    @Override
    public void setMetaData(MediaMetadataCompat mediaMetadataCompat) {
        title.setText(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        artist.setText(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        if (mediaMetadataCompat.getBitmap(MediaMetadataCompat.METADATA_KEY_ART) != null)
            mediaArt.setImageBitmap(mediaMetadataCompat.getBitmap(MediaMetadataCompat.METADATA_KEY_ART));
        else {
            mediaArt.setImageResource(R.drawable.ic_song_thumbnail);
        }
        durationTextView.setText(Utils.getFormattedTime((int) mediaMetadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
        seekBar.setMax((int) mediaMetadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        runnable.run();
    }


    public void setCurrentPosition(int progress) {
        currentPositionTextView.setText(Utils.getFormattedTime(progress));
        seekBar.setProgress(progress);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser) {
            setCurrentPosition(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        presenter.seekMusicTo(seekBar.getProgress());
    }
}
