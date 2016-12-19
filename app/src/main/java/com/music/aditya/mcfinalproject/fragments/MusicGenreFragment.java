package com.music.aditya.mcfinalproject.fragments;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.music.aditya.mcfinalproject.R;
import com.music.aditya.mcfinalproject.database.MusicChoiceDbHelper;
import com.music.aditya.mcfinalproject.utils.Song;
import com.music.aditya.mcfinalproject.utils.Utility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by aditya on 11/2/16.
 */
public class MusicGenreFragment extends Fragment {

    public static final String TAG = MusicGenreFragment.class.getCanonicalName();
    private LinkedHashMap<String, LinkedHashSet<Song>> mapGenreSongs;
    private RecyclerView genresView;
    private LinkedHashSet<Song> suggestedSongsList;

//    private MusicService musicSrv;
//    private MusicController controller;
//    private Intent playIntent;
//    private boolean musicBound = false;

//    private boolean paused = false;
//    private boolean playbackPaused = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Music by Genre");
        View rootView = inflater.inflate(R.layout.fragment_music_library, container, false);
        genresView = (RecyclerView) rootView.findViewById(R.id.genres_list);
        genresView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mapGenreSongs = new LinkedHashMap<>();

        suggestedSongsList = new LinkedHashSet<>();


        //small testing going on
//        controller = new MusicController(getActivity());
        mapGenreSongs.put("Suggested Songs", suggestedSongsList);
        fetchMusicLibrary();
        populateSuggestionList();
        genresView.setAdapter(new RecyclerListAdapter(new ArrayList(mapGenreSongs.keySet())));
        return rootView;
    }

    private void populateSuggestionList() {
        MusicChoiceDbHelper musicChoiceDbHelper = MusicChoiceDbHelper.getMusicChoiceDbHelper(getContext());
        Cursor cursor = musicChoiceDbHelper.getAllSongsData();
        if (cursor != null && cursor.getCount() != 0) {
            int window_size = loadSuggestionFromDb(Utility.SUGGESTION_LIMIT, cursor);
            //but if suggestion window is still empty
            if (window_size > 0) {
                cursor = musicChoiceDbHelper.getTopGenres();
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    while (window_size != 0 && !cursor.isAfterLast()) {
                        window_size = loadSuggestionFromHashMap(window_size,
                                cursor.getString(cursor.getColumnIndex(MusicChoiceDbHelper.MUSIC_COLUMN_SONG_GENRE)));
                        cursor.moveToNext();
                    }
                }
            }
        }
    }

    private int loadSuggestionFromHashMap(int window_limit, String topGenre) {
        int counter = 0;
        ArrayList<Song> genreSongs = new ArrayList<>(mapGenreSongs.get(topGenre));
        while (counter < genreSongs.size() && window_limit != 0) {
            if (suggestedSongsList.add(genreSongs.get(counter))) {
                window_limit = window_limit - 1;
            }
            counter = counter + 1;
        }
        return window_limit;
    }

    private int loadSuggestionFromDb(int window_limit, Cursor cursor) {
        int loopCount = (cursor.getCount() > window_limit) ? window_limit : cursor.getCount();
        cursor.moveToFirst();
        while (loopCount > 0) {
            long songId = Long.parseLong(cursor.getString(cursor.getColumnIndex(MusicChoiceDbHelper.MUSIC_COLUMN_SONG_ID)));
            String songTitle = cursor.getString(cursor.getColumnIndex(MusicChoiceDbHelper.MUSIC_COLUMN_SONG_TITLE));
            String songArtist = cursor.getString(cursor.getColumnIndex(MusicChoiceDbHelper.MUSIC_COLUMN_SONG_ARTIST));
            String songGenre = cursor.getString(cursor.getColumnIndex(MusicChoiceDbHelper.MUSIC_COLUMN_SONG_GENRE));
            suggestedSongsList.add(new Song(songId, songTitle, songArtist, songGenre));
            cursor.moveToNext();
            loopCount = loopCount - 1;
        }
        return window_limit - suggestedSongsList.size();
    }

//    private ServiceConnection musicConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
//            //get service
//            musicSrv = binder.getService();
//            //pass list
//            musicSrv.setList(songsList);
//            musicBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            musicBound = false;
//        }
//    };

    public void fetchMusicLibrary() {
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, "mime_type = 'audio/mpeg'", null, null);

        //checking something
//        int columnCount = musicCursor.getColumnCount();
        //checking done

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);
            int songId = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(songId);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisGenre = Utility.getGenre(thisArtist);

                Log.v(TAG, "Artist*" + thisArtist + "*");

                if (mapGenreSongs.containsKey(thisGenre)) {
                    mapGenreSongs.get(thisGenre).add(new Song(thisId, thisTitle, thisArtist, thisGenre));
                } else {
                    LinkedHashSet<Song> songArrayList = new LinkedHashSet<>();
                    songArrayList.add(new Song(thisId, thisTitle, thisArtist, thisGenre));
                    mapGenreSongs.put(thisGenre, songArrayList);
                }
            }
            while (musicCursor.moveToNext());

            Log.v(TAG, "Total Songs = " + mapGenreSongs.size());
            for (String str : mapGenreSongs.keySet())
                Log.v(TAG, "Genre : " + str + " songs = " + mapGenreSongs.get(str).size());

        } else {
            Utility.showToast(getActivity(), "Add some music files to your device first!!!");
        }
    }

    private class PersonalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView genre;
        LinearLayout genreLayout;

        public PersonalViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            genreLayout = (LinearLayout) itemView.findViewById(R.id.genre_layout);
            genre = (TextView) itemView.findViewById(R.id.genre);
            genreLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.genre_layout:
                    Utility.showToast(getActivity(), "genre with title : " + genre.getText() + " requested");
                    Bundle bundle = new Bundle();
                    bundle.putString(MusicSongsFragment.ARG_GENRE, genre.getText().toString());
                    bundle.putParcelableArrayList(MusicSongsFragment.ARG_GENRE_SONG_LIST, new ArrayList(mapGenreSongs.get(genre.getText())));
                    Utility.navigateFragment(new MusicSongsFragment(), MusicSongsFragment.TAG, bundle, getActivity());
                    break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.music_player, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
//                musicSrv.setShuffle();
                break;

            case R.id.action_end:
//                getActivity().stopService(playIntent);
//                musicSrv = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class RecyclerListAdapter extends RecyclerView.Adapter<PersonalViewHolder> {

        ArrayList<String> genreList = null;

        public RecyclerListAdapter(ArrayList<String> genreList) {
            this.genreList = genreList;
        }

        @Override
        public PersonalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.genre_list_item, parent, false);
            PersonalViewHolder personalViewHolder = new PersonalViewHolder(itemView);
            return personalViewHolder;
        }

        @Override
        public void onBindViewHolder(PersonalViewHolder holder, int position) {
            holder.genre.setText(genreList.get(position));
        }

        @Override
        public int getItemCount() {
            return genreList.size();
        }
    }
}
