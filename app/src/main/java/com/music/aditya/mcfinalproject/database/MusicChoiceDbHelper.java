package com.music.aditya.mcfinalproject.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * Created by aditya on 11/12/16.
 */
public class MusicChoiceDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    private static final String TAG = MusicChoiceDbHelper.class.getCanonicalName();
    private static String externalStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DATABASE_NAME = externalStorageDirectory + "/databaseFolder/musicChoice.db";
    public static final String TABLE_NAME = "user_music_history";
    public static final String MUSIC_COLUMN_DB_ID = "_id";
    public static final String MUSIC_COLUMN_SONG_ID = "songId";
    public static final String MUSIC_COLUMN_SONG_ARTIST = "songArtist";
    public static final String MUSIC_COLUMN_SONG_TITLE = "songTitle";
    public static final String MUSIC_COLUMN_SONG_GENRE = "songGenre";
    public static final String MUSIC_COLUMN_LISTEN_COUNT = "listenCount";
    public static final String MUSIC_COLUMN_GENRE_COUNT = "genreCount";
    private static MusicChoiceDbHelper musicChoiceDbHelper;

    private MusicChoiceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static MusicChoiceDbHelper getMusicChoiceDbHelper(Context context) {
        if (musicChoiceDbHelper == null) {
            musicChoiceDbHelper = new MusicChoiceDbHelper(context);
        }
        return musicChoiceDbHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "onUpgrade");
    }

    public void createMusicTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("create table if not exists " + TABLE_NAME + " ("
                + MUSIC_COLUMN_DB_ID + " integer PRIMARY KEY autoincrement, "
                + MUSIC_COLUMN_SONG_ID + " integer, "
                + MUSIC_COLUMN_SONG_ARTIST + " text, "
                + MUSIC_COLUMN_SONG_TITLE + " text, "
                + MUSIC_COLUMN_SONG_GENRE + " text, "
                + MUSIC_COLUMN_LISTEN_COUNT + " integer); ");
    }

    public boolean insertOrUpdateMusicData(ContentValues contentValues) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db.updateWithOnConflict(TABLE_NAME, contentValues, MUSIC_COLUMN_SONG_ID + "=" + contentValues.get(MUSIC_COLUMN_SONG_ID),
                null, SQLiteDatabase.CONFLICT_REPLACE) == 0)
            db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return true;
    }

    public int getSongCount(long songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {MUSIC_COLUMN_LISTEN_COUNT};
        Cursor cursor = db.query(TABLE_NAME, projection,
                MUSIC_COLUMN_SONG_ID + "=" + songId, null, null, null, null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return cursor.getInt(cursor.getColumnIndex(MUSIC_COLUMN_LISTEN_COUNT));
        } else
            return 0;
    }

    public Cursor getAllSongsData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, MUSIC_COLUMN_LISTEN_COUNT + " DESC");
        return cursor;
    }

    public Cursor getTopGenres() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + MUSIC_COLUMN_SONG_GENRE + ",sum(" + MUSIC_COLUMN_LISTEN_COUNT + ") as "
                + MUSIC_COLUMN_GENRE_COUNT + " from " + TABLE_NAME + " group by " + MUSIC_COLUMN_SONG_GENRE + " order by " + MUSIC_COLUMN_GENRE_COUNT + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    //Clear whole table
    public int clearSuggestionsDb() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, null, null);
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
