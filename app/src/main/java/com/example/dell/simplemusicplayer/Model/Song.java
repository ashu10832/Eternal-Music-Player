package com.example.dell.simplemusicplayer.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import static android.R.attr.id;
import static android.R.attr.theme;


/**
 * Created by Dell on 02-Feb-17.
 */
@SuppressWarnings("serial")

/*public class Song implements Parcelable {
    private String title;
    private String artist;
    private String data;
    private byte[] imageByte;
   public Song(String title, String artist, String data, byte[] image)
    {
        this.title = title;
        this.artist = artist;
        this.data = data;
        this.imageByte = image;

    }

    private Song(Parcel in) {
        title = in.readString();
        artist = in.readString();
        imageByte = in.createByteArray();
        data = in.readString();

    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

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



    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeByteArray(imageByte);
        dest.writeString(data);
    }

    public byte[] getImageByte() {
        return imageByte;
    }

    public void setImageByte(byte[] imageByte) {
        this.imageByte = imageByte;
    }
}
*/
public class Song implements Serializable{
    private String title;
    private String artist;
    private String data;
    private byte[] imageByte;
    Song(String title, String artist, String data, byte[] image)
    {
        this.title = title;
        this.artist = artist;
        this.data = data;
        this.imageByte = image;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist){
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte[] getImageByte() {
        return imageByte;
    }

    public void setImageByte(byte[] imageByte) {
        this.imageByte = imageByte;
    }
}