package com.music.aditya.mcfinalproject.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.music.aditya.mcfinalproject.R;
import com.music.aditya.mcfinalproject.controller.MusicController;
import com.music.aditya.mcfinalproject.services.MusicService;
import com.music.aditya.mcfinalproject.utils.Song;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MusicPlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MusicPlayerFragment extends Fragment implements MediaController.MediaPlayerControl {
    public static final String ARG_SONG_POSITION = "songPosition";
    public static final String ARG_SONG_LIST = "songList";
    public static final String TAG = MusicPlayerFragment.class.getCanonicalName();

    private ArrayList<Song> songsList;
    private int songPosition;
    private OnFragmentInteractionListener mListener;
    private Intent playIntent;
    private MusicService musicSrv;
    private MusicController controller;
    private boolean musicBound = false;

    private boolean paused = false;
    private boolean playbackPaused = true;

    public MusicPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        controller.show();
    }

    @Override
    public void onStop() {
        controller.realHide();
        super.onStop();
    }


    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songsList);
            musicBound = true;
            setAndPlaySong();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


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
        if (getActivity() != null)
            controller.setAnchorView(getActivity().findViewById(R.id.music_screen_bg));
        controller.setEnabled(true);
    }

    //play next
    private void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
            setController();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (playIntent == null) {
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().startService(playIntent);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }

        controller = new MusicController(getActivity());
        setController();

        if (getArguments() != null) {
            songPosition = getArguments().getInt(ARG_SONG_POSITION);
            songsList = getArguments().getParcelableArrayList(ARG_SONG_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_music_screen_control, container, false);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.music_screen_bg);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controller != null && !controller.isShowing())
                    controller.show();
            }
        });
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(imageView);
        Glide.with(this).load(R.drawable.dance).into(imageViewTarget);
//        controller = new MusicController(getActivity());
        return rootView;
    }


    private void setAndPlaySong() {
        musicSrv.setSong(songPosition);
        musicSrv.playSong();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        if (!controller.isShown())
            controller.show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (controller != null && !controller.isShown())
            controller.show();
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (controller != null && controller.isShown())
            controller.realHide();
        if (musicBound == true) {
            getActivity().unbindService(musicConnection);
            musicBound = false;
        }
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
