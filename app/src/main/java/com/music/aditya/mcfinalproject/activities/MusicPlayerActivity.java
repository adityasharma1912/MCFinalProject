package com.music.aditya.mcfinalproject.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.music.aditya.mcfinalproject.R;
import com.music.aditya.mcfinalproject.database.MusicChoiceDbHelper;
import com.music.aditya.mcfinalproject.fragments.MusicGenreFragment;
import com.music.aditya.mcfinalproject.services.MusicService;
import com.music.aditya.mcfinalproject.utils.Utility;

public class MusicPlayerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private static final int PERMISSIONS_REQUEST_STORAGE = 102;
    Switch flashLightToggle, autoBrightToggle;
    private MusicService musicService;
    private boolean musicBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Utility.checkForPermission(this, permission, PERMISSIONS_REQUEST_STORAGE)) {
            Utility.navigateFragment(new MusicGenreFragment(), MusicGenreFragment.TAG, null, MusicPlayerActivity.this);
        } else {
            Utility.showToast(MusicPlayerActivity.this, "Need storage permissions");
            finish();
        }

        flashLightToggle = (Switch) navigationView.getMenu().findItem(R.id.nav_flash_light_switch).
                getActionView().findViewById(R.id.flash_light_toggle_switch);
        autoBrightToggle = (Switch) navigationView.getMenu().findItem(R.id.nav_auto_bright_switch).
                getActionView().findViewById(R.id.auto_brightness_toggle_switch);
        flashLightToggle.setChecked(true);
        Utility.flashLightBoolean = true;
        autoBrightToggle.setChecked(false);
        flashLightToggle.setOnCheckedChangeListener(this);
        autoBrightToggle.setOnCheckedChangeListener(this);

        Intent playIntent = new Intent(this, MusicService.class);
        startService(playIntent);
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            int count = fragmentManager.getBackStackEntryCount();
            if (count <= 0) {
                finish();
                return;
            }
            String name = fragmentManager.getBackStackEntryAt(count - 1).getName();
            if (name.equalsIgnoreCase(MusicGenreFragment.TAG)) {
                showDialogOnExit();
            } else {
                fragmentManager.popBackStackImmediate();
            }
        }
    }


    private void showDialogOnExit() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.exit_msg))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MusicPlayerActivity.this.finish();
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.music_player, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
    }

    //    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_shuffle:
//                Toast.makeText(this, "action shuffle", Toast.LENGTH_SHORT).show();
//                break;
//
//            case R.id.action_end:
//                Toast.makeText(this, "action end", Toast.LENGTH_SHORT).show();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Utility.showToast(MusicPlayerActivity.this, "Launching MusicGenreFragment");
            Utility.navigateFragment(new MusicGenreFragment(), MusicGenreFragment.TAG, null, MusicPlayerActivity.this);
        } else if (id == R.id.nav_clear_suggestions) {
            MusicChoiceDbHelper musicChoiceDbHelper = MusicChoiceDbHelper.getMusicChoiceDbHelper(getApplicationContext());
            if (musicChoiceDbHelper.clearSuggestionsDb() > 0)
                Utility.showToast(this, "Suggestions Cleared");
            else
                Utility.showToast(this, "No Suggestions already");
            Utility.navigateFragment(new MusicGenreFragment(), MusicGenreFragment.TAG, null, MusicPlayerActivity.this);

        } else if (id == R.id.nav_flash_light_switch) {
            if (flashLightToggle.isChecked())
                flashLightToggle.setChecked(false);
            else
                flashLightToggle.setChecked(true);
        } else if (id == R.id.nav_auto_bright_switch) {
            if (autoBrightToggle.isChecked())
                autoBrightToggle.setChecked(false);
            else
                autoBrightToggle.setChecked(true);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
//        Toast.makeText(MusicPlayerActivity.this, "Launching Audio Player", Toast.LENGTH_SHORT).show();

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.add(R.id.container_fragment, new MusicGenreFragment());
//        fragmentTransaction.commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.flash_light_toggle_switch:
                if (isChecked) {
                    turnOnFlashLightService();
                } else {
                    turnOffFlashLightService();
                }
                break;
            case R.id.auto_brightness_toggle_switch:
                if (isChecked) {
                    turnOnAutoBrightness();
                } else {
                    turnOffAutoBrightness();
                }
                break;
        }
    }

    private void turnOffAutoBrightness() {
        Utility.showToast(this, "turnOffAutoBrightness");
    }

    private void turnOnAutoBrightness() {
        Utility.showToast(this, "turnOnAutoBrightness");
    }

    private void turnOffFlashLightService() {
        Utility.showToast(this, "turnOffFlashLightService");
        Utility.flashLightBoolean = false;
    }

    private void turnOnFlashLightService() {
        Utility.showToast(this, "turnOnFlashLightService");
        if (musicService != null && musicBound != false) {
            Utility.flashLightBoolean = true;
            musicService.startFlashLightThread();
        } else {
            Utility.showToast(this, "service not bonded");
            flashLightToggle.setChecked(false);
        }
    }
}
