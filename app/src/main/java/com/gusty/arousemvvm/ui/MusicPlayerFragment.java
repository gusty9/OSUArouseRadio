package com.gusty.arousemvvm.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gusty.arousemvvm.MusicPlayerService;
import com.gusty.arousemvvm.R;
import com.gusty.arousemvvm.model.RecentTracks;
import com.gusty.arousemvvm.viewmodel.TrackViewModel;

public class MusicPlayerFragment extends Fragment {
    //VIEWS
    private ImageView albumImageView;
    private TextView songTextView;
    private TextView albumTextView;
    private TextView artistTextView;
    private ImageView backgroundImage;
    private ImageView pausePlayOverlay;
    private ImageView artistIcon;
    private ImageView songIcon;
    private ImageView albumIcon;

    //data
    private boolean isRotating;
    private RotatingAlbumCover rab;
    private DynamicThemeFromAlbum background;

    //music player service
    private MusicPlayerService player;
    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            player = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRotating = true;
        Intent playerIntent = new Intent(getContext(), MusicPlayerService.class);
        getActivity().startService(playerIntent);
        getActivity().bindService(playerIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.music_player_fragment, container, false);

        //view binding
        albumImageView = v.findViewById(R.id.cover_album);
        songTextView = v.findViewById(R.id.name_song);
        albumTextView = v.findViewById(R.id.album_name);
        artistTextView = v.findViewById(R.id.name_artist);
        backgroundImage = v.findViewById(R.id.backgroundImage);
        pausePlayOverlay = v.findViewById(R.id.pause_play_vector_overlay);
        artistIcon = v.findViewById(R.id.artist_icon);
        songIcon = v.findViewById(R.id.music_icon);
        albumIcon = v.findViewById(R.id.album_icon);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TrackViewModel.Factory factory = new TrackViewModel.Factory(getActivity().getApplication());
        final TrackViewModel viewModel = ViewModelProviders.of(this, factory).get(TrackViewModel.class);
        observeViewModel(viewModel);
    }

    private void observeViewModel(TrackViewModel viewModel) {
        viewModel.getObservableProject().observe(this, new Observer<RecentTracks>() {
            @Override
            public void onChanged(RecentTracks recentTracks) {
                Bitmap albumart = recentTracks.getRecentTracksInfo().getTracks().get(0).getAlbumArt();
                String song = recentTracks.getRecentTracksInfo().getTracks().get(0).getName();
                String artist = recentTracks.getRecentTracksInfo().getTracks().get(0).getArtist().getText();
                String album = recentTracks.getRecentTracksInfo().getTracks().get(0).getAlbum().getText();
                songTextView.setText(song);
                artistTextView.setText(artist);
                albumTextView.setText(album);
                if (rab != null) {
                    rab.endAnimation();
                    rab = null;
                    albumImageView.setOnClickListener(null);
                }
                rab = new RotatingAlbumCover(albumImageView, pausePlayOverlay, albumart, getContext());
                background = new DynamicThemeFromAlbum(albumart, getContext());
                if (isRotating) {
                    if (rab.isStarted()){
                        rab.resumeAnimation(background.getTriadicColor());
                    } else {
                        rab.startAnimation(background.getTriadicColor());
                    }
                } else {
                    rab.pauseAnimation(background.getTriadicColor());
                }
                setViewColors(background);
                backgroundImage.setImageBitmap(background.getBlurredBitmap());
                albumImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!isRotating){
                            if(rab.isStarted()){
                                rab.resumeAnimation(background.getTriadicColor());
                            }else{
                                rab.startAnimation(background.getTriadicColor());
                            }
                            isRotating = true;
                        } else {
                            rab.pauseAnimation(background.getTriadicColor());
                            isRotating = false;
                        }
                        player.toggleAudio();
                    }
                });
            }
        });
    }

    /**
     * Set the color of the background and the text views
     * based on the album art
     * @param background ->
     *            the backround object to extract the colors from
     */
    private void setViewColors(DynamicThemeFromAlbum background) {
        //set the text color based on the background image
        background.setTextviewStyles(songTextView, artistTextView, albumTextView);
        background.setIconColrs(artistIcon, songIcon, albumIcon);
        ((MainActivity) getActivity()).setStatusBarColor(background.getStatusBarColorFromBackground());
    }

}
