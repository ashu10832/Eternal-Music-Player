package com.example.dell.simplemusicplayer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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


    private static final String TAG = "MusicPlayingService";
    private static MediaPlayer mediaPlayer;
    public static String mediaFile = null;
    private static int resumePosition;
    AudioManager audioManager;
    public int songDuration = -1;
    public int currentTrackPosition = -1;
    public static boolean isServiceStarted = false;

    MediaSessionCompat mediaSessionCompat;
    ArrayList<AudioTrack> songList;




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

            if (mediaId.equals(mediaFile)){
                updateMetadata();
                return;
            }
            mediaFile = mediaId;
            mediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            if (songList!=null){
                int count = 0;
                for (AudioTrack track: songList) {
                    if (track.getData().equals(mediaId)){
                        currentTrackPosition = count;
                        break;
                    }
                    count++;
                }
            }
            updateMetadata();
            prepareMediaPlayer();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            seekMusicTo((int)pos);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
            if (action.equals("AddSongList")){
                extras.setClassLoader(AudioTrack.class.getClassLoader());
                songList = extras.getParcelableArrayList("SongArrayList");
            }

        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }


    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

       /* mediaSessionCompat.setCallback(callback);
        playbackStateCompat = playbackStateBuilder.build();
        mediaSessionCompat.setPlaybackState(playbackStateCompat);
        mediaMetadataCompat = mediaMetaDataBuilder.build();
        mediaSessionCompat.setMetadata(mediaMetadataCompat);
        mediaSessionCompat.getSessionToken();
        mediaSessionCompat.setMetadata(mediaMetadataCompat);*/

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
       /* try {
            mediaPlayer.setDataSource(mediaFile);
            Log.i(TAG, "initMediaPlayer: ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "initMediaPlayer: StopSelf");
            stopSelf();
        }*/
       // mediaPlayer.prepareAsync();
    }




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

   /* private void showPlayingNotification() {
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
    }*/

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
        Log.i(TAG, "onStartCommand: StartId:" + startId + "Flags:" + flags);

      /*  if (mediaFile == null){
            mediaFile = intent.getStringExtra("SelectedSongData");
            prepareMediaPlayer();
        }
        else if (mediaFile.equals(intent.getStringExtra("SelectedSongData"))){

        }
        else{
            mediaFile = intent.getStringExtra("SelectedSongData");
                prepareMediaPlayer();
            }
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);*/
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
    }

    private void updateMetadata(){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(mediaFile);
        String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        songDuration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        Bitmap bitmap = BitmapFactory.decodeByteArray(mediaMetadataRetriever.getEmbeddedPicture(),0,mediaMetadataRetriever.getEmbeddedPicture().length);

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,songDuration);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artist);
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,title);
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,bitmap);

        MediaMetadataCompat metadata = builder.build();

        Intent intent = new Intent("METADATA-UPDATED");
        intent.putExtra("metadata",metadata);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "onPrepared: ");

        mediaMetadataRetriever.release();
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

    public void prepareMediaPlayer(){
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(mediaFile);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        if (currentTrackPosition == -1){
            return;
        }
        playNextSong();

    }

    private void playNextSong() {
        if (currentTrackPosition == songList.size()-1){
            currentTrackPosition = 0;
        }
        else{
            currentTrackPosition++;
        }
        mediaFile = songList.get(currentTrackPosition).getData();
        updateMetadata();
        prepareMediaPlayer();
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

    public static int getCurrentPosition() {
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
