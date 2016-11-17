package com.music.aditya.mcfinalproject.application;

import android.app.Application;
import android.util.Log;

import com.music.aditya.mcfinalproject.database.MusicChoiceDbHelper;
import com.music.aditya.mcfinalproject.utils.Utility;

/**
 * Created by aditya on 11/11/16.
 */
public class MusicPlayerApplication extends Application {

    private static final String TAG = MusicPlayerApplication.class.getCanonicalName();

    @Override
    public void onCreate() {
        super.onCreate();
        Utility.loadArtistGenreHashMap();
        MusicChoiceDbHelper musicChoiceDbHelper = MusicChoiceDbHelper.getMusicChoiceDbHelper(getApplicationContext());
        musicChoiceDbHelper.getWritableDatabase();
        musicChoiceDbHelper.createMusicTable();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.v(TAG,"Application Terminated");
    }
}
