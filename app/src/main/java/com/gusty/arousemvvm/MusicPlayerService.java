package com.gusty.arousemvvm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gusty.arousemvvm.model.RecentTracks;
import com.gusty.arousemvvm.ui.MainActivity;
import com.gusty.arousemvvm.utility.Constants;
import com.gusty.arousemvvm.viewmodel.TrackViewModel;

import java.io.IOException;
//https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/
public class MusicPlayerService extends LifecycleService implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {
    public static final String CHANNEL_ID = "ArouseMediaPlayer";
    public static final String ACTION_TOGGLE = "com.gusty.arousemvvm.mediaplayer.TOGGLE";
    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private String mediaFile;
    private AudioManager audioManager;
    private boolean isPlaying = true;
    private TrackViewModel viewModel;
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private RecentTracks currentlyPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return iBinder;
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        try {
            mediaFile = Constants.Companion.getMUSIC_ENDPOINT();
            initMediaSession();
        } catch (Exception e) {
            stopSelf();
        }
        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf();
        }

        initMediaPlayer();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Arouse Music Player")
                .setContentText("Loading Music")
                .setSmallIcon(R.drawable.music)
                .build();
        startForeground(1123, notification);
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    public void passViewModel(TrackViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void observeLiveData() {
        viewModel.getObservableProject().observe(this, new Observer<RecentTracks>() {
            @Override
            public void onChanged(RecentTracks recentTracks) {
                currentlyPlaying = recentTracks;
                buildNotification();
            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //invoked indicating buffering status of a media resource being streamed over the network
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //invoked when playback of a media source has completed
        stopMedia();
        //stop the service
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.e("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //invoked to communicate some info
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //invoked when the media source is ready for playback
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //invoked idicating the completion of a seek operation
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //invoked when audio focus of the system is updated
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }


    public void toggleAudio() {
        Log.e("test", "audio toggle");
        if (isPlaying) {
            //mediaPlayer.setVolume(0,0);
            mediaPlayer.pause();
            isPlaying = false;
            Log.e("test", "audio is going shhhhh");
        } else {
            //mediaPlayer.setVolume(1,1);
            mediaPlayer.start();
            Log.e("test", "audio going LOUD");
            isPlaying = true;
        }
        buildNotification();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(mediaFile);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    private void initMediaSession() {
        if (mediaSessionManager != null) {
            return;
        }

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.e("test", "onPlay in media session, idfk what this is");
                toggleAudio();
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.e("test", "onPause in media session, idfk what this is");
                toggleAudio();
            }
        });
    }

    private void buildNotification() {
        Bitmap albumArt = currentlyPlaying.getRecentTracksInfo().getTracks().get(0).getAlbumArt();
        String song = currentlyPlaying.getRecentTracksInfo().getTracks().get(0).getName();
        String artist = currentlyPlaying.getRecentTracksInfo().getTracks().get(0).getArtist().getText();
        Intent intent = new Intent(getApplicationContext(), MusicPlayerService.class);
        intent.setAction(ACTION_TOGGLE);
        int action;
        String actionStr;
        if (isPlaying) {
            //build playing pending intent
            action = android.R.drawable.ic_media_pause;
            actionStr = "pause";
        } else {
            action = android.R.drawable.ic_media_play;
            actionStr = "play";
        }
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.music)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mediaSession.getSessionToken()))
                .addAction(action, actionStr, pausePendingIntent)
                .setContentTitle(song)
                .setContentText(artist)
                .setLargeIcon(albumArt)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1123, notification);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) {
            return;
        }
        String action = playbackAction.getAction();
        if (action.equalsIgnoreCase(ACTION_TOGGLE)) {
            Log.e("test", "toggle audio from notification");
            toggleAudio();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Music Player", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}