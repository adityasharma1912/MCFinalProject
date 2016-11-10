package com.music.aditya.mcfinalproject.controller;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

/**
 * Created by aditya on 11/6/2016.
 */

public class MusicController extends MediaController {

    public MusicController(Context context) {
        super(context);
    }

    public void hide() {
        return;
    }

    public void realHide() {
        super.hide();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK)
            super.hide();

        return super.dispatchKeyEvent(event);
    }
}
