package com.example.dell.simplemusicplayer.Model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by ashugupta on 30/06/17.
 */

class AudioTrack : Parcelable {

    var data: String? = null

    constructor(data: String) {
        this.data = data
    }

    private constructor(`in`: Parcel) {
        data = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(data)
    }

    companion object {

        val CREATOR: Parcelable.Creator<AudioTrack> = object : Parcelable.Creator<AudioTrack> {
            override fun createFromParcel(`in`: Parcel): AudioTrack {
                return AudioTrack(`in`)
            }

            override fun newArray(size: Int): Array<AudioTrack> {
                return arrayOfNulls(size)
            }
        }
    }
}
