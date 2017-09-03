package com.example.dell.simplemusicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.example.dell.simplemusicplayer.Model.AudioTrack;

import java.util.ArrayList;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;

/**
 * Created by ashugupta on 29/06/17.
 */

public class MusicServiceManager {
    private static final String TAG = "MusicServiceManager";

    private Context context;
    private static MediaControllerCompat mMediaControllerCompat;
    private int mCurentState;
    private MediaBrowserCompat mMediaBrowserCompat;
    private String currentSongData = null;
    private ArrayList<AudioTrack> songList;


    public MusicServiceManager(Context context, ArrayList<AudioTrack> songList) {
        this.context = context;
        initializeMediaBrowser();
        setMusicList(songList);

    }


    private MediaControllerCompat.Callback mediaControllerCompatCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            Log.d(TAG, "onMetadataChanged: ");
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null) {
                return;
            }
            switch (state.getState()) {
                case STATE_PLAYING: {
                    mCurentState = STATE_PLAYING;
                    break;
                }
                case STATE_PAUSED: {
                    mCurentState = STATE_PAUSED;
                    break;
                }
            }
        }
    };


    private MediaBrowserCompat.ConnectionCallback mMediaBrowerCompatCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            Log.i(TAG, "onConnected: ");
            try {
                mMediaControllerCompat = new MediaControllerCompat(context, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mediaControllerCompatCallback);
                MediaControllerCompat.setMediaController((Activity) context, mMediaControllerCompat);
                Bundle b = new Bundle();
                b.putParcelableArrayList("SongArrayList", songList);
                mMediaControllerCompat.getTransportControls().sendCustomAction("AddSongList", b);
                Intent intent = new Intent(context, MusicPlayingService.class);
                context.startService(intent);
                if (currentSongData != null)
                    mMediaControllerCompat.getTransportControls().playFromMediaId(currentSongData, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    private void initializeMediaBrowser() {
        mMediaBrowserCompat = new MediaBrowserCompat(context, new ComponentName(context, MusicPlayingService.class), mMediaBrowerCompatCallback, null);
        mMediaBrowserCompat.connect();
    }


    public void unbindToService() {
        mMediaBrowserCompat.disconnect();
    }


    public void setMusicList(ArrayList<AudioTrack> songList) {
        this.songList = songList;
        Bundle b = new Bundle();
        b.putParcelableArrayList("SongArrayList", songList);
        if (mMediaBrowserCompat.isConnected()) {
            mMediaControllerCompat.getTransportControls().sendCustomAction("AddSongList", b);
        }
    }

    public void startPlayingFromData(String songData) {
        currentSongData = songData;
        Log.d(TAG, "startPlayingFromData: ");

        if (mMediaBrowserCompat.isConnected()) {
            Log.d(TAG, "startPlayingFromData: Connected!");
            mMediaControllerCompat.getTransportControls().playFromMediaId(songData, null);
        }
    }


    public int getCurrentPosition() {
        return MusicPlayingService.getCurrentPosition();
    }

    public void seekTo(int progress) {
        mMediaControllerCompat.getTransportControls().seekTo(progress);
    }

    public void play() {
        mMediaControllerCompat.getTransportControls().play();
    }

    public void pause() {
        mMediaControllerCompat.getTransportControls().pause();
    }
}
