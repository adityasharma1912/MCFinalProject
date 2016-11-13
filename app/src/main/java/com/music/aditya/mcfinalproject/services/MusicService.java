package com.music.aditya.mcfinalproject.services;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.music.aditya.mcfinalproject.database.MusicChoiceDbHelper;
import com.music.aditya.mcfinalproject.utils.Song;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by aditya on 11/2/16.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songsList;
    private int songPosition;
    private boolean shuffle = false;
    private Random rand;
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;

    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition = 0;
        initMusicPlayer();
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

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void startPlayer() {
        player.start();
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
    public void onDestroy() {
        player.stop();
        player.release();
        stopForeground(true);
    }
}
