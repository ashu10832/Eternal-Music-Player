package com.example.dell.simplemusicplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.Serializable;

public class SongPlayingActivity extends AppCompatActivity {
    Button play,pause;
    MediaPlayer mp;
    TextView title;
    TextView artist;
    private static final String TAG = "SongPlayingActivity";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseMediaPlayer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_playing);
        play = (Button) findViewById(R.id.play);
        pause = (Button) findViewById(R.id.pause);
        title = (TextView) findViewById(R.id.title_song);
        artist = (TextView) findViewById(R.id.artist_song);

        String title_song = getIntent().getStringExtra("title");
        String artist_song = getIntent().getStringExtra("artist");
        title.setText(title_song);
        artist.setText(artist_song);
        Uri songUri = Uri.parse(getIntent().getStringExtra("uri"));
        Log.i(TAG, "onCreate: " + songUri.toString());
        releaseMediaPlayer();
        mp = MediaPlayer.create(this,songUri);
        mp.start();
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.pause();
            }
        });



    }
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mp != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mp.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mp = null;

        }
    }
}
