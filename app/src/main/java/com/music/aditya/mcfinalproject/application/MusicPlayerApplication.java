package com.music.aditya.mcfinalproject.application;

import android.app.Application;

import com.music.aditya.mcfinalproject.utils.Utility;

/**
 * Created by aditya on 11/11/16.
 */
public class MusicPlayerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utility.loadArtistGenreHashMap();
    }
}
