package com.example.dell.simplemusicplayer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.jar.Manifest;


/**
 * Created by ashugupta on 03/06/17.
 */

public class MusicPlayingService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener
        , MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {


    private static final String TAG = "MusicPlayingService";
    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    public static String mediaFile = null;
    private int resumePosition;
    AudioManager audioManager;
    public int songDuration = -1;
    public static boolean isServiceStarted = false;


    MediaSessionCompat mediaSessionCompat;
    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;
    PlaybackStateCompat.Builder playbackStateBuilder;
    PlaybackStateCompat playbackStateCompat;
    MediaMetadataCompat mediaMetadataCompat;
    MediaMetadataCompat.Builder mediaMetaDataBuilder;



    MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            if (!successfullyRetrievedAudioFocus()) {
                return;
            }
            mediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            playMedia();
        }

        @Override
        public void onPause() {
            super.onPause();
            pauseMedia();
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);

        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
        }
    };


    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private void showPlayingNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(MusicPlayingService.this, mediaSessionCompat);
        if( builder == null ) {
            return;
        }
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat.from(MusicPlayingService.this).notify(1, builder.build());
    }


    private void showPausedNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSessionCompat);
        if( builder == null ) {
            return;
        }
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    private boolean successfullyRetrievedAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.i(TAG, "onBind: ");
        return iBinder;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: StartId:" + startId + "Flags:" + flags);
        if (isServiceStarted) {
            Log.i(TAG, "onStartCommand: Service Already Running!");
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
            String newMediaFile = null;
            try {
                newMediaFile = intent.getExtras().getString("media");
                if (!newMediaFile.equals(mediaFile)) {
                    mediaFile = newMediaFile;
                    //mediaPlayer.release();
                    /*mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnBufferingUpdateListener(this);
                    mediaPlayer.setOnCompletionListener(this);
                    mediaPlayer.setOnErrorListener(this);
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setOnSeekCompleteListener(this);
                    mediaPlayer.setOnInfoListener(this);
                    mediaPlayer.reset();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);*/
                    mediaPlayer.reset();
                    try {
                        mediaPlayer.setDataSource(newMediaFile);
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        Log.i(TAG, "onStartCommand: Exception");
                        e.printStackTrace();
                    }
                }
            } catch (NullPointerException e) {
                Log.i(TAG, "onStartCommand: Exception");
                stopSelf();
            }
        } else {
            Log.i(TAG, "onStartCommand: Service Not Running!");
            try {
                mediaFile = intent.getExtras().getString("media");

            } catch (NullPointerException e) {
                Log.i(TAG, "onStartCommand: Exception");
                stopSelf();
            }
            initMediaPlayer();
            isServiceStarted = true;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mNoisyReceiver);
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        if (mediaFile != null) {
            stopMedia();
            mediaPlayer.release();
        }
        isServiceStarted = false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Intent i = new Intent("android.intent.action.MAIN").putExtra("some_msg", "I will be sent!");
        this.sendBroadcast(i);
        playMedia();
        songDuration = mp.getDuration();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();

        mediaSessionCompat.setCallback(callback);
        playbackStateCompat = playbackStateBuilder.build();
        mediaSessionCompat.setPlaybackState(playbackStateCompat);
        mediaMetadataCompat = mediaMetaDataBuilder.build();
        mediaSessionCompat.setMetadata(mediaMetadataCompat);
        mediaSessionCompat.getSessionToken();

    }

    private void initNoisyReceiver() {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);

    }

    private void initMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(this, MediaButtonReceiver.class);
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), Manifest.class.getPackage().toString(), mediaButtonReceiver, null);
        mediaSessionCompat.setCallback(callback);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mediaSessionCompat.setMediaButtonReceiver(pendingIntent);
        setSessionToken(mediaSessionCompat.getSessionToken());
    }

    void setMetaData() {

    }

    public void initMediaPlayer() {
        Log.i(TAG, "initMediaPlayer: " + mediaFile);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        try {
            mediaPlayer.setDataSource(mediaFile);
            Log.i(TAG, "initMediaPlayer: ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "initMediaPlayer: StopSelf");
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
        }
    }

    public void stopMedia() {
        if (mediaPlayer == null)
            return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            audioManager.abandonAudioFocus(this);
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    public int getFileDuration() {
        return songDuration;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                stopMedia();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                pauseMedia();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                playMedia();
            }
            break;
        }
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "onCompletion: ");
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    public void seekMusicTo(int position) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(position);
            pauseMedia();
            resumePosition = position;
        } else {
            Log.i(TAG, "seekMusicTo: Progress:" + position);
            mediaPlayer.seekTo(position);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }


    @Override
    public void onSeekComplete(MediaPlayer mp) {


    }

    public int getCurrentPosition() {
        if (mediaPlayer.isPlaying())
            return mediaPlayer.getCurrentPosition();
        else {
            return resumePosition;
        }
    }

public class LocalBinder extends Binder {
    public MusicPlayingService getService() {
        Log.i(TAG, "getService: " + MusicPlayingService.this);
        return MusicPlayingService.this;
    }

}

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pauseMedia();
            }
        }
    };
}
