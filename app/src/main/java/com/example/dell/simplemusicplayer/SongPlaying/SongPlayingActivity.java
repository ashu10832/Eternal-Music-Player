package com.example.dell.simplemusicplayer.SongPlaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.dell.simplemusicplayer.Model.AudioTrack;
import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.MusicPlayingService;
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
    private MusicPlayingService musicService;
    boolean serviceBound = false;
    SeekBar seekBar;
    Handler handler;
    Song song;
    ArrayList<AudioTrack> songList;
    BroadcastReceiver receiver;
    SongPlayingPresenter presenter;
    int mCurentState;
    String mSelectedSongData;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_playing);
        initializeUI();
        if (savedInstanceState != null) {
            songList = savedInstanceState.getParcelableArrayList("SongList");
            mSelectedSongData = savedInstanceState.getString("SelectedSongPosition");
            Log.i(TAG, "onCreate: ServiceState " + serviceBound);
        } else {
            songList = getIntent().getParcelableArrayListExtra("SongList");
            mSelectedSongData = getIntent().getStringExtra("SelectedSongPosition");
        }
        presenter = new SongPlayingPresenter(this,songList,mSelectedSongData);
        //presenter.setMusicList(songList);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");

    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ServiceBound: " + serviceBound);
        presenter.disconnect();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");
        Log.i(TAG, "onResume: ");
        /*receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive: ");
                setSeekBarDuration(musicService.getFileDuration());
                setTotalDuration(musicService.getFileDuration());
            }
        };
        this.registerReceiver(receiver, intentFilter);*/
        //presenter.startPlaying(mSelectedSongData);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        this.unregisterReceiver(receiver);
    }

    void setSeekBarDuration(int millis) {
        seekBar.setMax(millis);
    }


    private void setTotalDuration(int fileDuration) {
        durationTextView.setText(Utils.getFormattedTime(fileDuration));
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
        handler = new Handler();
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            presenter.seekMusicTo(musicService.getCurrentPosition());
            presenter.setCurrentPosition(musicService.getCurrentPosition());
            handler.postDelayed(this, 1000);
        }
    };

    private void playMusic(Song musicFile) {
        setMetaData(musicFile);
    }

    @Override
    public void showTitle(String title) {
        setTitle(title);
    }

    public void setMetaData(Song song) {
        Log.i(TAG, "setMetaData: ");
        title.setText(song.getTitle());
        artist.setText(song.getArtist());
        if (song.getImageByte() != null) {
            Bitmap bm = BitmapFactory.decodeByteArray(song.getImageByte(), 0, song.getImageByte().length);
            mediaArt.setImageBitmap(bm);
        }
    }

    public void setCurrentPosition(String progress) {
        currentPositionTextView.setText(progress);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser) {
            handler.removeCallbacks(runnable);
            presenter.setCurrentPosition(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        presenter.seekMusicTo(seekBar.getProgress());
        runnable.run();
    }
}
