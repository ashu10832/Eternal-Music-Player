package com.example.dell.simplemusicplayer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.example.dell.simplemusicplayer.Model.AudioTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;


/**
 * Created by ashugupta on 03/06/17.
 */

public class MusicPlayingService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener
        , MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {


    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";
    private static final String TAG = "MusicPlayingService";
    public static String mediaFile = null;
    public static boolean isServiceStarted = false;
    private static MediaPlayer mediaPlayer;
    private static int resumePosition = 0;
    public int songDuration = -1;
    public int currentTrackPosition = -1;
    AudioManager audioManager;
    SharedPreferenceManager preferenceManager;
    ArrayList<AudioTrack> songList;
    Handler handler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            preferenceManager.setCurrentPostion(getCurrentPosition());
            handler.postDelayed(this, 1000);
        }
    };
    private MediaPlayer mp = null;
    private MediaSessionCompat mediaSessionCompat;
    private boolean dataSourceSet = false;
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pauseMedia();
            }
        }
    };
    MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            if (!successfullyRetrievedAudioFocus()) {
                return;
            }
            if (dataSourceSet) {
                mediaSessionCompat.setActive(true);
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                showPlayingNotification();
                playMedia();
            } else {
                Log.i(TAG, "onPlay: DataSourceSet:" + dataSourceSet);
                mediaFile = preferenceManager.getCurrentSong();
                resumePosition = preferenceManager.getCurrentPosittion();
                mediaSessionCompat.setActive(true);
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                updateMetadata();
                startSongFromCustomPosition();
            }

        }

        @Override
        public void onPause() {
            super.onPause();
            pauseMedia();
            mediaSessionCompat.setActive(false);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            showPausedNotification();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            Log.i(TAG, "onSkipToNext: ");
            //playNextSong();
            crossFade();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.i(TAG, "onSkipToPrevious: ");
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);

            if (mediaId.equals(mediaFile)) {
                return;
            }
            mediaFile = mediaId;
            mediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            if (songList != null) {
                int count = 0;
                for (AudioTrack track : songList) {
                    if (track.getData().equals(mediaId)) {
                        currentTrackPosition = count;
                        break;
                    }
                    count++;
                }
            }
            preferenceManager.setCurrentSong(mediaFile);
            updateMetadata();
            showPlayingNotification();
            prepareMediaPlayer();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            seekMusicTo((int) pos);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
            if (action.equals("AddSongList")) {
                extras.setClassLoader(AudioTrack.class.getClassLoader());
                songList = extras.getParcelableArrayList("SongArrayList");
            } else if (action.equals("STOP_SERVICE")) {
                Log.i(TAG, "onCustomAction: ");
                onDestroy();
            }

        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

    };

    public static int getCurrentPosition() {
        if (mediaPlayer == null) {
            return 0;
        }

        if (mediaPlayer.isPlaying())
            return mediaPlayer.getCurrentPosition();
        else {
            return resumePosition;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");

        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        preferenceManager = new SharedPreferenceManager(this);
        handler = new Handler();
    }

    public void initMediaPlayer() {
        //  Log.i(TAG, "initMediaPlayer: " + mediaFile);
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
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private boolean successfullyRetrievedAudioFocus() {


        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
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
        if (intent != null) {
            Log.i(TAG, "onStartCommand: " + intent.getAction());
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
            handleIntent(intent);
        }


        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (action.equals(ACTION_NEXT)) {
            //playNextSong();
            crossFade();
            return;
        }
        if (action.equals(ACTION_PREVIOUS)) {
            playPreviousSong();
            return;
        }
    }

    private void playPreviousSong() {

        if (currentTrackPosition == 0) {
            currentTrackPosition = songList.size() - 1;
        } else {
            currentTrackPosition--;
        }
        mediaFile = songList.get(currentTrackPosition).getData();
        updateMetadata();
        showPlayingNotification();
        prepareMediaPlayer();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        try {
            this.unregisterReceiver(mNoisyReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mediaFile != null) {
            stopMedia();
            dataSourceSet = false;
            mediaPlayer.release();
        }
        NotificationManagerCompat.from(this).cancelAll();
        handler.removeCallbacks(runnable);
        preferenceManager.setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
        isServiceStarted = false;
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    private void updateMetadata() {
        MediaMetadataCompat metadata = MetadataManager.getMetadata(mediaFile);
        mediaSessionCompat.setMetadata(metadata);
        Log.i(TAG, "METADATA: " + metadata);
        songDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

        Intent intent = new Intent("METADATA-UPDATED");
        intent.putExtra("metadata", metadata);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "onPrepared: ");

    }

    private void showPlayingNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSessionCompat);
        if (builder == null) {
            return;
        }
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_previous_black_24dp, "Previous", buildPendingIntent(ACTION_PREVIOUS)));
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause_black_24dp, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_next_black_24dp, "Next", buildPendingIntent(ACTION_NEXT)));

        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    private PendingIntent buildPendingIntent(String action) {
        Intent intent = new Intent(this, MusicPlayingService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, 0);
    }

    private void showPausedNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSessionCompat);
        if (builder == null) {
            return;
        }
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_previous_black_24dp, "Previous", buildPendingIntent(ACTION_PREVIOUS)));
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_next_black_24dp, "Next", buildPendingIntent(ACTION_NEXT)));
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    private void initNoisyReceiver() {
        Log.i(TAG, "initNoisyReceiver: ");
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(mNoisyReceiver, filter);

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

    public void prepareMediaPlayer() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(mediaFile);
            dataSourceSet = true;
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSongFromCustomPosition() {
        mediaPlayer.reset();
        try {

            mediaPlayer.setDataSource(mediaFile);
            dataSourceSet = true;
            mediaPlayer.prepareAsync();
            showPlayingNotification();
            mediaPlayer.seekTo(resumePosition);
            Log.i(TAG, "startSongFromCustomPosition:Current Position: " + mediaPlayer.getCurrentPosition());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            runnable.run();
            preferenceManager.setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
        }
    }

    public void stopMedia() {
        if (mediaPlayer == null)
            return;
        try {
            handler.removeCallbacks(runnable);
            preferenceManager.setPlaybackState(PlaybackStateCompat.STATE_STOPPED);
            mediaPlayer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            audioManager.abandonAudioFocus(this);
            preferenceManager.setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
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
        if (currentTrackPosition == -1) {
            return;
        }
        playNextSong();
    }

    private void playNextSong() {
        Log.i(TAG, "playNextSong: ");
        if (currentTrackPosition == songList.size() - 1) {
            currentTrackPosition = 0;
        } else {
            currentTrackPosition++;
        }
        mediaFile = songList.get(currentTrackPosition).getData();
        updateMetadata();
        showPlayingNotification();
        prepareMediaPlayer();
    }

    public void crossFade() {

        mp = new MediaPlayer();

        try {
            if (currentTrackPosition == songList.size() - 1) {
                currentTrackPosition = 0;
            } else {
                currentTrackPosition++;
            }
            mp.setDataSource(songList.get(currentTrackPosition).getData());
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer MPlayer) {
                    mp.setVolume(0, 0);
                    mp.start();
                    Log.i(TAG, "onPrepared: ");
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {
                        int counter = 1;

                        @Override
                        public void run() {
                            Log.i(TAG, "run: counter: " + counter);
                            if (counter == 50) {
                                mp.setVolume(1.0f, 1.0f);
                                mediaPlayer.setVolume(0, 0);
                                mediaPlayer.release();
                                mediaPlayer = mp;
                                mp = null;
                                handler.removeCallbacks(this);
                                return;
                            }
                            int maxVolume = 50;
                            float log1 = (float) (Math.log(maxVolume - counter) / Math.log(maxVolume));
                            mp.setVolume(1 - log1, 1 - log1);
                            log1 = (float) (Math.log(maxVolume - (50 - counter)) / Math.log(maxVolume));
                            mediaPlayer.setVolume((1 - log1), 1 - log1);
                            counter++;
                            handler.postDelayed(this, 100);
                        }
                    };
                    runnable.run();
                }
            });
            mp.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {


    }

    public class LocalBinder extends Binder {

        public MusicPlayingService getService() {
            Log.i(TAG, "getService: " + MusicPlayingService.this);
            return MusicPlayingService.this;
        }
    }
}
