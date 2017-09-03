package com.example.dell.simplemusicplayer.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ashugupta on 30/06/17.
 */

public class AudioTrack implements Parcelable {

    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public AudioTrack(String data) {
        this.data = data;
    }

    private AudioTrack(Parcel in) {
        data = in.readString();
    }

    public static final Creator<AudioTrack> CREATOR = new Creator<AudioTrack>() {
        @Override
        public AudioTrack createFromParcel(Parcel in) {
            return new AudioTrack(in);
        }

        @Override
        public AudioTrack[] newArray(int size) {
            return new AudioTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data);
    }
}
