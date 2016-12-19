package com.music.aditya.mcfinalproject.fragments;

import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.Toast;

import com.music.aditya.mcfinalproject.R;
import com.music.aditya.mcfinalproject.utils.Song;
import com.music.aditya.mcfinalproject.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aditya on 11/2/16.
 */
public class MusicSongsFragment extends Fragment {

    public static final String TAG = MusicSongsFragment.class.getCanonicalName();
    public static final String ARG_GENRE = "genre";
    public static final String ARG_GENRE_SONG_LIST = "genre_songs";
    private String genreSelected;
    private ArrayList<Song> songsList;
    private RecyclerView songView;
    private HashMap<String, Integer> songsMap = new HashMap<>();

//    private MusicService musicSrv;
//    private MusicController controller;
//    private Intent playIntent;
//    private boolean musicBound = false;

//    private boolean paused = false;
//    private boolean playbackPaused = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            songsList = getArguments().getParcelableArrayList(ARG_GENRE_SONG_LIST);
            genreSelected = getArguments().getString(ARG_GENRE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(genreSelected);
        View rootView = inflater.inflate(R.layout.fragment_genre_songs, container, false);
        songView = (RecyclerView) rootView.findViewById(R.id.song_list);
        songView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //small testing going on
//        controller = new MusicController(getActivity());
//        songsList = new ArrayList();
//        getSongList();
        Log.v(TAG, "" + songsList.size());
        songView.setAdapter(new RecyclerListAdapter(songsList));
        return rootView;
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

//    public void getSongList() {
//        ContentResolver musicResolver = getActivity().getContentResolver();
//        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        Cursor musicCursor = musicResolver.query(musicUri, null, "mime_type = 'audio/mpeg'", null, null);
//
//        //checking something
//        int columnCount = musicCursor.getColumnCount();
//        //checking done
//
//        if (musicCursor != null && musicCursor.moveToFirst()) {
//            //get columns
//            int titleColumn = musicCursor.getColumnIndex
//                    (MediaStore.Audio.Media.DISPLAY_NAME);
//            int songId = musicCursor.getColumnIndex
//                    (android.provider.MediaStore.Audio.Media._ID);
//            int artistColumn = musicCursor.getColumnIndex
//                    (android.provider.MediaStore.Audio.Media.ARTIST);
//            int mimeTypeColumn = musicCursor.getColumnIndex
//                    (android.provider.MediaStore.Audio.Media.MIME_TYPE);
//            //add songs to list
//            do {
//                long thisId = musicCursor.getLong(songId);
//                String thisMimeType = musicCursor.getString(mimeTypeColumn);
//                String thisTitle = musicCursor.getString(titleColumn);
//                String thisArtist = musicCursor.getString(artistColumn);
//                /*String songInfo = "";
//
//                for (String columnName : musicCursor.getColumnNames()) {
//                    songInfo += columnName + " ";
//                    songInfo += musicCursor.getString(musicCursor.getColumnIndex(columnName)) + " ";
//                }
//
//                Log.v(TAG, songInfo);*/
//                if (songsMap.containsKey(thisArtist)) {
//                    int value = songsMap.get(thisArtist);
//                    value = value + 1;
//                    songsMap.put(thisArtist, value);
//                } else
//                    songsMap.put(thisArtist, 1);
////                songsList.add(new Song(thisId, thisTitle, thisArtist));
//            }
//            while (musicCursor.moveToNext());
//
//            Log.v(TAG, "Total Songs = " + songsList.size());
//            Log.v(TAG, "Map Size = " + songsMap.size());
//            for (String str : songsMap.keySet())
//                Log.v(TAG, "Artist : " + str + " songs = " + songsMap.get(str));
//
//
//        } else {
//            Utility.showToast(getActivity(), "Add some music files to your device first!!!");
//        }
//    }

    private class PersonalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView songTitle;
        TextView songArtist;
        LinearLayout songLayout;

        public PersonalViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            songLayout = (LinearLayout) itemView.findViewById(R.id.song_layout);
            songTitle = (TextView) itemView.findViewById(R.id.song_title);
            songArtist = (TextView) itemView.findViewById(R.id.song_artist);
            songLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.song_layout:
                    Toast.makeText(getActivity(), "song with title : " + songTitle.getText() + " requested", Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putInt(MusicPlayerFragment.ARG_SONG_POSITION, this.getAdapterPosition());
                    bundle.putParcelableArrayList(MusicPlayerFragment.ARG_SONG_LIST, songsList);
                    Utility.navigateFragment(new MusicPlayerFragment(), MusicPlayerFragment.TAG, bundle, getActivity());
//                    musicSrv.setSong(this.getAdapterPosition());
//                    musicSrv.playSong();
//                    if (playbackPaused) {
//                        setController();
//                        playbackPaused = false;
//                    }
//                    if (!controller.isShown())
//                        controller.show();
                    break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

        List<Song> songsList = null;

        public RecyclerListAdapter(List<Song> songsList) {
            this.songsList = songsList;
        }

        @Override
        public PersonalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_list_item, parent, false);
            PersonalViewHolder personalViewHolder = new PersonalViewHolder(itemView);
            return personalViewHolder;
        }

        @Override
        public void onBindViewHolder(PersonalViewHolder holder, int position) {
            if (position % 2 == 0)
                holder.songLayout.setBackgroundColor(Color.LTGRAY);
            holder.songTitle.setText(songsList.get(position).getTitle());
            holder.songArtist.setText(songsList.get(position).getArtist());
        }

        @Override
        public int getItemCount() {
            return songsList.size();
        }
    }
}
