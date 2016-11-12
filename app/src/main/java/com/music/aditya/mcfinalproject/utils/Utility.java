package com.music.aditya.mcfinalproject.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.music.aditya.mcfinalproject.R;

import java.util.HashMap;

/**
 * Created by aditya on 11/9/2016.
 */

public class Utility {

    // hash-map of artists as key and Genre as value...
    private static HashMap<String, String> mapArtistGenre = null;


    public static void loadArtistGenreHashMap() {
        mapArtistGenre = new HashMap<>();
        mapArtistGenre.put("Eminem", "Hip Hop");
        mapArtistGenre.put("Ken Kaniff", "Hip Hop");
        mapArtistGenre.put("Jay-Z", "Hip Hop");
        mapArtistGenre.put("Maroon 5", "Pop");
        mapArtistGenre.put("Breaking Benjamin", "Hard Rock");
        mapArtistGenre.put("System Of A Down", "Hard Rock");
        mapArtistGenre.put("Metallica", "Metal");
        mapArtistGenre.put("Children of Bodom", "Metal");
        mapArtistGenre.put("3 Doors Down", "Soft Rock");
        mapArtistGenre.put("Imagine Dragons", "Soft Rock");
        mapArtistGenre.put("Eagles", "Soft Rock");
        mapArtistGenre.put("Jimi Hendrix", "Blues");
    }

    public static void navigateFragment(Fragment fragment, String tag, Bundle bundle, FragmentActivity fragmentActivity) {
        boolean fragmentPopped = false;
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        try {
            fragmentPopped = fragmentManager.popBackStackImmediate(tag, 0);
        } catch (IllegalStateException e) {

        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        if (!fragmentPopped && fragmentManager.findFragmentByTag(tag) == null) {
            if (bundle != null) {
                fragment.setArguments(bundle);
            }
            fragmentTransaction.replace(R.id.container_fragment, fragment, tag);
            fragmentTransaction.addToBackStack(tag);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    public static void showToast(Context context, String msg) {
        if (msg != null && !msg.trim().equalsIgnoreCase("")) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean checkForPermission(Activity activity, String[] permission, int request_code) {
        for (String perm : permission) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, permission, request_code);
                return false;
            }
        }
        return true;
    }

    public static String getGenre(String artist) {
        if (artist.contains("Eminem"))
            artist = "Eminem";
        if (mapArtistGenre != null && mapArtistGenre.containsKey(artist))
            return mapArtistGenre.get(artist);
        return "Unknown";
    }
}
