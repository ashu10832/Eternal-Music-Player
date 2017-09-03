package com.example.dell.simplemusicplayer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ashugupta on 12/07/17.
 */

public class SharedPreferenceManager {
    Context context;

    void get(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Ashu",Context.MODE_PRIVATE);
        sharedPreferences.getFloat("Asfasf",0);
    }

    void put(Context context){

        SharedPreferences.Editor editor = context.getSharedPreferences("",Context.MODE_PRIVATE).edit();
        editor.putString("key", "value");
        editor.commit();
    }


}
