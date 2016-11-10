package com.music.aditya.mcfinalproject.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.Toast;

import com.music.aditya.mcfinalproject.R;
import com.music.aditya.mcfinalproject.fragments.MusicLibraryFragment;
import com.music.aditya.mcfinalproject.utils.Utility;

public class MusicPlayerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSIONS_REQUEST_STORAGE = 102;

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
            Utility.navigateFragment(new MusicLibraryFragment(), MusicLibraryFragment.TAG, null, MusicPlayerActivity.this);
        } else {
            Utility.showToast(MusicPlayerActivity.this, "Need storage permissions");
            finish();
        }
    }

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
            if (name.equalsIgnoreCase(MusicLibraryFragment.TAG)) {
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
        getMenuInflater().inflate(R.menu.music_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                Toast.makeText(this, "action shuffle", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_end:
                Toast.makeText(this, "action end", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Utility.showToast(MusicPlayerActivity.this, "Launching MusicLibraryFragment");
            Utility.navigateFragment(new MusicLibraryFragment(), MusicLibraryFragment.TAG, null, MusicPlayerActivity.this);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
//        Toast.makeText(MusicPlayerActivity.this, "Launching Audio Player", Toast.LENGTH_SHORT).show();

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.add(R.id.container_fragment, new MusicLibraryFragment());
//        fragmentTransaction.commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
