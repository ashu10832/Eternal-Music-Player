package com.example.dell.simplemusicplayer.SongPlaying;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.media.MediaBrowserService;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.dell.simplemusicplayer.Model.Song;
import com.example.dell.simplemusicplayer.MusicPlayingService;
import com.example.dell.simplemusicplayer.R;
import com.example.dell.simplemusicplayer.Utils;

import static android.R.attr.name;
import static android.media.session.PlaybackState.STATE_PAUSED;
import static android.media.session.PlaybackState.STATE_PLAYING;
import static com.example.dell.simplemusicplayer.R.id.artist_song;
import static com.example.dell.simplemusicplayer.R.id.title_song;

public class SongPlayingActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "SongPlayingActivity";
    Button play, pause;
    TextView title, artist, currentPositionTextView, durationTextView;
    ImageView mediaArt;
    private MusicPlayingService musicService;
    boolean serviceBound = false;
    SeekBar seekBar;
    Handler handler;
    Song song;
    BroadcastReceiver receiver;
    SongPlayingPresenter presenter;
    int mCurentState;
    MediaControllerCompat mMediaControllerCompat;
    MediaBrowserCompat mMediaBrowserCompat;
    MediaControllerCompat.Callback mMediaControllerCompatCallback;

    MediaControllerCompat.Callback mediaControllerCompatCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null){
                return;
            }
            switch(state.getState()){
                case PlaybackStateCompat.STATE_PLAYING:{
                    mCurentState = STATE_PLAYING;
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED:{
                    mCurentState = STATE_PAUSED;
                    break;
                }
            }
        }
    };

    MediaBrowserCompat.ConnectionCallback mMediaBrowerCompatCallback = new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mMediaControllerCompat = new MediaControllerCompat(SongPlayingActivity.this,mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                setSupportMediaController(mMediaControllerCompat);
                getSupportMediaController().getTransportControls().play();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };



    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayingService.LocalBinder binder = (MusicPlayingService.LocalBinder) service;
            musicService = binder.getService();
            presenter.attach(binder.getService());
            Log.i(TAG, "onServiceConnected: ");
            if (musicService.getFileDuration() != -1) {
                setTotalDuration(musicService.getFileDuration());
                setSeekBarDuration(musicService.getFileDuration());
            }
            runnable.run();
            serviceBound = true;
            //Log.i(TAG, "onServiceConnected: MusicService:" + musicService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            presenter.detach();
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_playing);
        if (savedInstanceState != null) {
            song = (Song) savedInstanceState.getSerializable("Song");
            serviceBound = savedInstanceState.getBoolean("ServiceState");
            Log.i(TAG, "onCreate: ServiceState " + serviceBound);
        } else {
            song = (Song) getIntent().getSerializableExtra("SelectedSong");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
        initialize();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ServiceBound: " + serviceBound);
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");
        Log.i(TAG, "onResume: ");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive: ");
                setSeekBarDuration(musicService.getFileDuration());
                setTotalDuration(musicService.getFileDuration());
            }
        };
        this.registerReceiver(receiver, intentFilter);
        playMusic(song);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(receiver);
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
        outState.putBoolean("ServiceState", false);
        outState.putSerializable("Song", song);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
        Log.i(TAG, "onRestoreInstanceState: ServiceBound" + serviceBound);
        song = (Song) savedInstanceState.getSerializable("Song");
    }


    void initialize() {
        Log.i(TAG, "initialize: ");
        presenter = new SongPlayingPresenter();
        durationTextView = (TextView) findViewById(R.id.duration_textview);
        currentPositionTextView = (TextView) findViewById(R.id.current_time_textview);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        play = (Button) findViewById(R.id.play);
        pause = (Button) findViewById(R.id.pause);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playMedia();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.pauseMedia();
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
            seekBar.setProgress(musicService.getCurrentPosition());
            setCurrentPosition(musicService.getCurrentPosition());
            handler.postDelayed(this, 1000);
        }
    };

    private void playMusic(Song musicFile) {
        setMetaData(musicFile);
        Intent intent = new Intent(this, MusicPlayingService.class);
        intent.putExtra("media", musicFile.getData());
        startService(intent);

        if (!serviceBound) {
            Log.i(TAG, "playMusic: Service Bound False");
            // Intent intent = new Intent(this, MusicPlayingService.class);
            //intent.putExtra("media", musicFile.getData());
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            Log.i(TAG, "playMusic: Service Already Bound");
        }
    }

    private void setMetaData(Song song) {
        Log.i(TAG, "setMetaData: ");
        title.setText(song.getTitle());
        artist.setText(song.getArtist());
        if (song.getImageByte() != null) {
            Bitmap bm = BitmapFactory.decodeByteArray(song.getImageByte(), 0, song.getImageByte().length);
            mediaArt.setImageBitmap(bm);
        }
    }

    private void setCurrentPosition(int progress) {
        currentPositionTextView.setText(Utils.getFormattedTime(progress));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser) {
            handler.removeCallbacks(runnable);
            setCurrentPosition(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        musicService.seekMusicTo(seekBar.getProgress());
        runnable.run();
    }
}
