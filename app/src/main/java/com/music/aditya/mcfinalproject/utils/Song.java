package com.music.aditya.mcfinalproject.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aditya on 11/2/16.
 */
public class Song implements Parcelable {

    private long id;
    private String title;
    private String artist;
    private String genre;

    public Song(long songID, String songTitle, String songArtist, String songGenre) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        genre = songGenre;
    }

    public Song(Parcel parcel) {
        id = parcel.readLong();
        title = parcel.readString();
        artist = parcel.readString();
        genre = parcel.readString();
    }

    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public static Creator<Song> CREATOR = new Creator<Song>() {

        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.artist);
        dest.writeString(this.genre);
    }
}
