package com.example.dell.simplemusicplayer.Model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Created by ashugupta on 02/06/17.
 */

public class SongLoader {
    Context context;

    public SongLoader(Context context)
    {
        this.context = context;
    }


    public ArrayList<Song> getSongs() {
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<Song> audioList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(data);
                byte[] musicArt = mediaMetadataRetriever.getEmbeddedPicture();
                audioList.add(new Song(title, artist, data,musicArt));
            }
        }
        cursor.close();
        return audioList;
    }



}
