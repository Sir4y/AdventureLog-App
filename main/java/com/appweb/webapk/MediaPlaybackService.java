package com.appweb.webapk;

import com.appweb.webapk.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.Base64;
import android.media.AudioManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat.Action;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.app.NotificationCompat.MediaStyle;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaPlaybackService extends Service {
    public static final String NOTIFICATION_CHANNEL_ID = "web_app_notifications";
    public static final String ACTION_UPDATE_METADATA = "com.appweb.webapk.UPDATE_METADATA";
    public static final String ACTION_UPDATE_STATE = "com.appweb.webapk.UPDATE_STATE";
    public static final String ACTION_SET_HANDLERS = "com.appweb.webapk.SET_HANDLERS";
    public static final String ACTION_STOP_SERVICE = "com.appweb.webapk.STOP_SERVICE";
    public static final String ACTION_UPDATE_POSITION = "com.appweb.webapk.UPDATE_POSITION";

    public static final String ACTION_PLAY = "com.appweb.webapk.PLAY";
    public static final String ACTION_PAUSE = "com.appweb.webapk.PAUSE";
    public static final String ACTION_NEXT = "com.appweb.webapk.NEXT";
    public static final String ACTION_PREVIOUS = "com.appweb.webapk.PREVIOUS";

    public static final String BROADCAST_MEDIA_ACTION = "com.appweb.webapk.BROADCAST_MEDIA_ACTION";
    public static final String EXTRA_MEDIA_ACTION = "EXTRA_MEDIA_ACTION";


    private static final int NOTIFICATION_ID = 101;
    private MediaSessionCompat mediaSession;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private BroadcastReceiver becomingNoisyReceiver;

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                sendActionToWebView("pause");
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, "WebToApkMediaSession");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        PlaybackStateCompat initialState = new PlaybackStateCompat.Builder().setActions(0).setState(PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0).build();
        mediaSession.setPlaybackState(initialState);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override public void onPlay() { sendActionToWebView("play"); }
            @Override public void onPause() { sendActionToWebView("pause"); }
            @Override public void onSkipToNext() { sendActionToWebView("nexttrack"); }
            @Override public void onSkipToPrevious() { sendActionToWebView("previoustrack"); }
            @Override public void onStop() { sendActionToWebView("stop"); }
        });
        mediaSession.setActive(true);
        becomingNoisyReceiver = new BecomingNoisyReceiver();
        registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_STICKY;
        String action = intent.getAction();
        switch (action) {
            case ACTION_UPDATE_METADATA: updateMetadata(intent.getStringExtra("title"), intent.getStringExtra("artist"), intent.getStringExtra("album"), intent.getStringExtra("artworkUrl")); break;
            case ACTION_UPDATE_STATE: updatePlaybackState(intent.getStringExtra("state")); break;
            case ACTION_UPDATE_POSITION: updatePositionState(intent.getDoubleExtra("duration", 0), intent.getDoubleExtra("playbackRate", 1.0), intent.getDoubleExtra("position", 0)); break;
            case ACTION_SET_HANDLERS: setMediaActionHandlers(intent.getStringArrayExtra("actions")); break;
            case ACTION_STOP_SERVICE: stopSelf(); break;
            case ACTION_PLAY: sendActionToWebView("play"); break;
            case ACTION_PAUSE: sendActionToWebView("pause"); break;
            case ACTION_NEXT: sendActionToWebView("nexttrack"); break;
            case ACTION_PREVIOUS: sendActionToWebView("previoustrack"); break;
        }
        return START_STICKY;
    }

    private void updateMetadata(String title, String artist, String album, @Nullable String artworkUrl) {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_TITLE, title).putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist).putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);
        if (artworkUrl != null && !artworkUrl.isEmpty()) {
            try {
                String base64String = artworkUrl.substring(artworkUrl.indexOf(',') + 1);
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap artworkBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artworkBitmap);
            } catch (Exception e) { }
        }
        mediaSession.setMetadata(metadataBuilder.build());
        updateNotification();
    }

    private void updatePositionState(double duration, double playbackRate, double position) {
        PlaybackStateCompat currentState = mediaSession.getController().getPlaybackState();
        if (currentState == null || currentState.getState() == PlaybackStateCompat.STATE_NONE) return;
        long durationMs = (long) (duration * 1000);
        long positionMs = (long) (position * 1000);
        MediaMetadataCompat currentMetadata = mediaSession.getController().getMetadata();
        MediaMetadataCompat.Builder metadataBuilder = (currentMetadata == null) ? new MediaMetadataCompat.Builder() : new MediaMetadataCompat.Builder(currentMetadata);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs);
        mediaSession.setMetadata(metadataBuilder.build());
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder(currentState);
        stateBuilder.setState(currentState.getState(), positionMs, (float) playbackRate);
        mediaSession.setPlaybackState(stateBuilder.build());
        updateNotification();
    }

    private void updatePlaybackState(String stateStr) {
        PlaybackStateCompat currentState = mediaSession.getController().getPlaybackState();
        if (currentState == null) currentState = new PlaybackStateCompat.Builder().setActions(0).setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f).build();
        int state = "playing".equals(stateStr) ? PlaybackStateCompat.STATE_PLAYING : ("paused".equals(stateStr) ? PlaybackStateCompat.STATE_PAUSED : PlaybackStateCompat.STATE_STOPPED);
        PlaybackStateCompat.Builder newStateBuilder = new PlaybackStateCompat.Builder(currentState);
        newStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        mediaSession.setPlaybackState(newStateBuilder.build());
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) startForeground(NOTIFICATION_ID, buildNotification());
        else { stopForeground(false); NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID); if (state == PlaybackStateCompat.STATE_STOPPED) stopSelf(); }
    }

    private void setMediaActionHandlers(String[] actions) {
        PlaybackStateCompat currentState = mediaSession.getController().getPlaybackState();
        if (currentState == null) return;
        long supportedActions = 0;
        if (actions != null) {
            for (String action : actions) {
                switch (action) {
                    case "play": supportedActions |= PlaybackStateCompat.ACTION_PLAY; break;
                    case "pause": supportedActions |= PlaybackStateCompat.ACTION_PAUSE; break;
                    case "previoustrack": supportedActions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS; break;
                    case "nexttrack": supportedActions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT; break;
                }
            }
        }
        if ((supportedActions & PlaybackStateCompat.ACTION_PLAY) != 0 && (supportedActions & PlaybackStateCompat.ACTION_PAUSE) != 0) supportedActions |= PlaybackStateCompat.ACTION_PLAY_PAUSE;
        PlaybackStateCompat.Builder newStateBuilder = new PlaybackStateCompat.Builder(currentState);
        newStateBuilder.setActions(supportedActions);
        mediaSession.setPlaybackState(newStateBuilder.build());
        updateNotification();
    }

    private Notification buildNotification() {
        MediaMetadataCompat metadata = mediaSession.getController().getMetadata();
        PlaybackStateCompat playbackState = mediaSession.getController().getPlaybackState();
        if (playbackState == null || (playbackState.getState() != PlaybackStateCompat.STATE_PLAYING && playbackState.getState() != PlaybackStateCompat.STATE_PAUSED)) return null;
        boolean isPlaying = playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        List<Integer> compactActionIndices = new ArrayList<>();
        if ((playbackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) { builder.addAction(R.drawable.ic_skip_previous, "Previous", createActionIntent(ACTION_PREVIOUS)); compactActionIndices.add(compactActionIndices.size()); }
        if ((playbackState.getActions() & PlaybackStateCompat.ACTION_PLAY_PAUSE) != 0) { builder.addAction(new Action(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow, isPlaying ? "Pause" : "Play", createActionIntent(isPlaying ? ACTION_PAUSE : ACTION_PLAY))); compactActionIndices.add(compactActionIndices.size()); }
        if ((playbackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) { builder.addAction(R.drawable.ic_skip_next, "Next", createActionIntent(ACTION_NEXT)); compactActionIndices.add(compactActionIndices.size()); }
        int[] compactIndices = new int[compactActionIndices.size()];
        for (int i = 0; i < compactActionIndices.size(); i++) compactIndices[i] = compactActionIndices.get(i);
        builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle(metadata != null ? metadata.getDescription().getTitle() : "Radio").setContentText(metadata != null ? metadata.getDescription().getSubtitle() : "...").setLargeIcon(metadata != null ? metadata.getDescription().getIconBitmap() : null).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)).setDeleteIntent(PendingIntent.getService(this, 0, new Intent(this, MediaPlaybackService.class).setAction(ACTION_STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setOnlyAlertOnce(true).setStyle(new MediaStyle().setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(compactIndices));
        return builder.build();
    }

    private void updateNotification() {
        PlaybackStateCompat state = mediaSession.getController().getPlaybackState();
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING || state.getState() == PlaybackStateCompat.STATE_PAUSED)) { Notification n = buildNotification(); if (n != null) NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, n); }
        else NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
    }
    
    private PendingIntent createActionIntent(String action) {
        return PendingIntent.getService(this, action.hashCode(), new Intent(this, MediaPlaybackService.class).setAction(action), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    
    private void sendActionToWebView(String action) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_MEDIA_ACTION).putExtra(EXTRA_MEDIA_ACTION, action));
    }
    
    @Override public void onDestroy() { super.onDestroy(); mediaSession.release(); executor.shutdown(); unregisterReceiver(becomingNoisyReceiver); }
    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
