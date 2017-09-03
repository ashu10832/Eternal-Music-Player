package com.example.dell.simplemusicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v4.media.MediaMetadataCompat;

/**
 * Created by ashugupta on 03/07/17.
 */

public class MetadataManager {


   public static MediaMetadataCompat getMetadata(String mediaFile){

       MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
       mediaMetadataRetriever.setDataSource(mediaFile);
       String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
       String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
       int songDuration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

       MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

       builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,songDuration);
       builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artist);
       builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,title);
       builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title);
       builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist);
       builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
       builder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);



       if (mediaMetadataRetriever.getEmbeddedPicture() != null) {
           Bitmap bitmap;
           bitmap = BitmapFactory.decodeByteArray(mediaMetadataRetriever.getEmbeddedPicture(), 0, mediaMetadataRetriever.getEmbeddedPicture().length);
           builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap);
           builder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap);
           builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);


       }
       mediaMetadataRetriever.release();
       return builder.build();
   }
}
