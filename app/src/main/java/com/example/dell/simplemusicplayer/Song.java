package com.example.dell.simplemusicplayer;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Dell on 02-Feb-17.
 */
@SuppressWarnings("serial")

public class Song
{
    private long id;
    private String title;
    private String artist;
    private Bitmap image;
    private Uri uri;
    Song(long id, String title, String artist,Uri uri)
    {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.uri = uri;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
