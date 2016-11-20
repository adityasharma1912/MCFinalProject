package com.music.aditya.mcfinalproject.services;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.music.aditya.mcfinalproject.database.MusicChoiceDbHelper;
import com.music.aditya.mcfinalproject.utils.Song;
import com.music.aditya.mcfinalproject.utils.Utility;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by aditya on 11/2/16.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, SensorEventListener {

    private static final String TAG = MusicService.class.getCanonicalName();
    private MediaPlayer player;
    private ArrayList<Song> songsList;
    private int songPosition;
    private boolean shuffle = false;
    private Random rand;
    private Handler handler;
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;

    public static final String SONG_TITLE = "song_title";
    public static final int SET_SONG_TITLE = 221;

    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean isTorchOn;

    private SensorManager senSensorManager;
    private Sensor sensorAccelerometer;

    //Declaring variables to detect speed
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private long timerValue = 500;
    private static final int SHAKE_THRESHOLD = 2000;
    private static final int SPEEDUP_THRESHOLD = 3000;

    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition = 0;
        initMusicPlayer();
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    boolean isFlashLightAvailable() {
        Boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        return isFlashAvailable;
    }

    public void startFlashLightThread() {
        if (isFlashLightAvailable()) {
            FlashLightThread flashLightThread = new FlashLightThread();
            flashLightThread.start();
        } else {
            Log.v(TAG, "FlashLight not available in device");
        }
    }

    public String getShareMessage() {
        return "Hey Check out this " + songsList.get(songPosition).getGenre() + " song with Title: " + songTitle
                + " by " + songsList.get(songPosition).getArtist();
    }

    class FlashLightThread extends Thread {
        @Override
        public void run() {
            while (Utility.flashLightBoolean == true && isPlaying()) {
                try {
                    if (isTorchOn) {
                        turnOffFlashLight();
                        isTorchOn = false;
                    } else {
                        turnOnFlashLight();
                        isTorchOn = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    sleep(timerValue);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            turnOffFlashLight();
        }
    }

    public void turnOnFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void turnOffFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setList(ArrayList<Song> theSongs) {
        songsList = theSongs;
    }

    public void setSong(int songIndex) {
        songPosition = songIndex;
    }

    public void setShuffle() {
        if (shuffle) shuffle = false;
        else shuffle = true;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0] + 10;
            float y = sensorEvent.values[1] + 10;
            float z = sensorEvent.values[2] + 10;
            //updating after every 100 milliseconds
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    Log.v(TAG, "Speed = " + speed);
                    Log.v(TAG, "SpeedLight = " + timerValue);
                    if (speed > SPEEDUP_THRESHOLD) {
                        timerValue = 100;
                    } else {
                        timerValue = 1000;
                    }
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public void initMusicPlayer() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        player.stop();
//        player.release();
        return false;
    }

    public void playSong() {
        player.reset();
        Song playSong = songsList.get(songPosition);
        songTitle = playSong.getTitle();
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public int getPosition() {
        return player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public boolean isPlaying() {
        try {
            return player.isPlaying();
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            return false;
        }
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int position) {
        player.seekTo(position);
    }

    public void startPlayer() {
        player.start();
        startFlashLightThread();
    }

    public void playPrevious() {
        songPosition--;
        if (songPosition < 0) songPosition = songsList.size() - 1;
        playSong();
    }

    public void playNext() {
        if (shuffle) {
            int newSong = songPosition;
            while (newSong == songPosition) {
                newSong = rand.nextInt(songsList.size());
            }
            songPosition = newSong;
        } else {
            songPosition++;
            if (songPosition > (songsList.size() - 1)) songPosition = 0;
        }
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(this, "onCompletion Called", Toast.LENGTH_SHORT).show();
        MusicChoiceDbHelper musicChoiceDbHelper = MusicChoiceDbHelper.getMusicChoiceDbHelper(getApplicationContext());
        int count = musicChoiceDbHelper.getSongCount(songsList.get(songPosition).getID());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicChoiceDbHelper.MUSIC_COLUMN_SONG_ID, songsList.get(songPosition).getID());
        contentValues.put(MusicChoiceDbHelper.MUSIC_COLUMN_SONG_ARTIST, songsList.get(songPosition).getArtist());
        contentValues.put(MusicChoiceDbHelper.MUSIC_COLUMN_SONG_TITLE, songsList.get(songPosition).getTitle());
        contentValues.put(MusicChoiceDbHelper.MUSIC_COLUMN_SONG_GENRE, songsList.get(songPosition).getGenre());
        contentValues.put(MusicChoiceDbHelper.MUSIC_COLUMN_LISTEN_COUNT, count + 1);
        musicChoiceDbHelper.insertOrUpdateMusicData(contentValues);

//        if (player.getCurrentPosition() > 0) {
//            mp.reset();
        playNext();
//        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Bundle bundle = new Bundle();
        Message message = handler.obtainMessage();
        bundle.putString(SONG_TITLE, songTitle);
        message.what = SET_SONG_TITLE;
        message.setData(bundle);
        handler.sendMessage(message);

        startFlashLightThread();
        /*Intent notIntent = new Intent(this, MusicPlayerActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Player")
                .setContentText(songTitle);

        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);*/
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.v(TAG, "Flash Light Turned off");
        senSensorManager.unregisterListener(this);
        turnOffFlashLight();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        stopForeground(true);
    }
}
