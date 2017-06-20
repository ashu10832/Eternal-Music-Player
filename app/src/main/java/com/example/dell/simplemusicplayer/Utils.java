package com.example.dell.simplemusicplayer;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by ashugupta on 14/06/17.
 */

public class Utils {

    public static String  getFormattedTime(int millis){
        String time;
        if (TimeUnit.MILLISECONDS.toHours(millis)>0){
            time = String.format(Locale.getDefault(),"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        }
        else {
            time = String.format(Locale.getDefault(),"%02d:%02d",(millis/1000)/60,(millis/1000)%60);
        }
        return time;
    }
}
