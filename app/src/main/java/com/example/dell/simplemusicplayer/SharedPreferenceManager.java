package com.example.dell.simplemusicplayer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ashugupta on 12/07/17.
 */

public class SharedPreferenceManager {
    Context context;
    private String SHARED_PREFERENCES_NAME = "Shared_preferences";


    public SharedPreferenceManager(Context context){
        this.context = context;
    }



    public int getPlaybackState(){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getInt("PlaybackState",-1);
    }


    public String getCurrentSong(){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString("CurrentMusicFile",null);
    }

   public void setPlaybackState(int PlaybackState){
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE).edit();
        editor.putInt("PlaybackState", PlaybackState);
        editor.apply();
    }


    public void setCurrentSong(String mediaFile) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE).edit();
        editor.putString("CurrentMusicFile", mediaFile);
        editor.apply();
    }
    public void setCurrentPostion(int position){
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE).edit();
        editor.putInt("CurrentPostion",position);
        editor.apply();
    }

    public int getCurrentPosittion(){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getInt("CurrentPosition",-1);
    }
}
