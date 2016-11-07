package com.music.aditya.mcfinalproject.fragments;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.music.aditya.mcfinalproject.R;
import com.music.aditya.mcfinalproject.controller.MusicController;
import com.music.aditya.mcfinalproject.services.MusicService;
import com.music.aditya.mcfinalproject.utils.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aditya on 11/2/16.
 */
public class MusicLibraryFragment extends Fragment implements MediaController.MediaPlayerControl {

    public static final String TAG = MusicLibraryFragment.class.getCanonicalName();
    private ArrayList<Song> songList;
    private RecyclerView songView;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;

    private boolean paused = false;
    private boolean playbackPaused = false;

    private void setController() {
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }

        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(getActivity().findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    //play next
    private void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
//            setController();
            playbackPaused = false;
        }
        if (!controller.isShown())
            controller.show();
    }

    //play previous
    private void playPrev() {
        musicSrv.playPrevious();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        if (!controller.isShown())
            controller.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
        setController();
    }

    @Override

    public void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.music_library, container, false);
        songView = (RecyclerView) rootView.findViewById(R.id.song_list);
        songView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //small testing going on
        controller = new MusicController(getActivity());
        songList = new ArrayList();
        getSongList();
        Log.v(TAG, "" + songList.size());
        songView.setAdapter(new RecyclerListAdapter(songList));
        return rootView;
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void getSongList() {
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, "mime_type = 'audio/mpeg'", null, null);

        //checking something
        int columnCount = musicCursor.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            Log.v(TAG, musicCursor.getColumnName(i));
        }
        //checking done

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);
            int songId = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int mimeTypeColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.MIME_TYPE);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(songId);
                String thisMimeType = musicCursor.getString(mimeTypeColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String songInfo = "";

                for (String columnName : musicCursor.getColumnNames()) {
                    songInfo += columnName + " ";
                    songInfo += musicCursor.getString(musicCursor.getColumnIndex(columnName)) + " ";
                }

                Log.v(TAG, songInfo);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    @Override
    public void start() {
        playbackPaused = false;
        musicSrv.startPlayer();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPlaying())
            return musicSrv.getDuration();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPlaying())
            return musicSrv.getPosition();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

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
                    musicSrv.setSong(this.getAdapterPosition());
                    musicSrv.playSong();
                    if (playbackPaused) {
                        setController();
                        playbackPaused = false;
                    }
                    if (!controller.isShown())
                        controller.show();
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
                musicSrv.setShuffle();
                break;

            case R.id.action_end:
                getActivity().stopService(playIntent);
                musicSrv = null;
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
            holder.songTitle.setText(songsList.get(position).getTitle());
            holder.songArtist.setText(songsList.get(position).getArtist());
        }

        @Override
        public int getItemCount() {
            return songsList.size();
        }
    }
}
