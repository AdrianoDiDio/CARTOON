package com.adriano.cartoon.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.ui.PlayerView;
import com.adriano.cartoon.Camera;
import com.adriano.cartoon.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import timber.log.Timber;

public class VideoPlayerFragment extends Fragment implements View.OnClickListener,
        Player.Listener {
    private static final String CAMERA_KEY = "Camera";
    private TextView cameraIPTextView;
    private TextView cameraPortTextView;
    private TextView cameraLatitudeTextView;
    private TextView cameraLongitudeTextView;
    private LinearLayout playPauseLayout;
    private LinearLayout errorLayout;
    private TextView errorMessageTextView;
    private FloatingActionButton retryButton;
    private Camera camera;
    private PlayerView exoPlayerView;
    private ExoPlayer simpleExoPlayer;
    private ChildParentPlayerErrorNotification childParentExitNotification;

    public static VideoPlayerFragment newInstance(Camera camera) {
        VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(CAMERA_KEY, camera);
        videoPlayerFragment.setArguments(args);
        return videoPlayerFragment;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == retryButton.getId()) {
            errorLayout.setVisibility(View.GONE);
            playPauseLayout.setVisibility(View.VISIBLE);
            simpleExoPlayer.play();
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        simpleExoPlayer.release();
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        int errorResourceID;
        switch (error.errorCode) {
            case ExoPlaybackException.TYPE_SOURCE:
                errorResourceID = R.string.camera_connection_failed;
                break;
            case ExoPlaybackException.TYPE_RENDERER:
                errorResourceID = R.string.camera_render_failed;
                break;

            case ExoPlaybackException.TYPE_UNEXPECTED:
            default:
                errorResourceID = R.string.camera_unexpected_error;
                break;
        }
        showErrorMessage(String.format(getString(errorResourceID), camera.name));
    }

    public void showErrorMessage(String errorMessage) {
        errorLayout.setVisibility(View.VISIBLE);
        playPauseLayout.setVisibility(View.GONE);
        errorMessageTextView.setText(errorMessage);
    }

    private void addPlayer() {
        TrackSelector trackSelector = new DefaultTrackSelector(getContext());
        LoadControl loadControl = new DefaultLoadControl();
        simpleExoPlayer = new ExoPlayer.Builder(getContext())
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build();
        //Disable error logging on exoplayer...we already have our handler...
        Log.setLogLevel(Log.LOG_LEVEL_OFF);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(), "MapsApiTest"));
        //TODO:Use camera.url...
        String url;
        url = "http://" + camera.ip + ":" + camera.port + "/" + camera.streamName;
        Timber.d("Connecting to " + url);
        Uri videoUri = Uri.parse(url);
        MediaSource videoSource =
                new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(videoUri));
        simpleExoPlayer.prepare(videoSource);
        simpleExoPlayer.setPlayWhenReady(true);

        exoPlayerView.setPlayer(simpleExoPlayer);
        simpleExoPlayer.addListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        cameraArrayList = savedInstanceState.getParcelableArrayList(CAMERA_LIST_KEY);
//        Timber.d("Received " + cameraArrayList.size() + " cameras");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        camera = (Camera) getArguments().get(CAMERA_KEY);
        if (getParentFragment() instanceof ChildParentPlayerErrorNotification) {
            childParentExitNotification = (ChildParentPlayerErrorNotification) getParentFragment();
        } else {
            throw new RuntimeException("The parent fragment must implement ChildParentExitNotification");
        }
        return inflater.inflate(
                R.layout.fragment_video_page, container, false);

    }

    private String formatResource(int resourceID,Object ... args) {
        return String.format(getResources().getString(resourceID),args);
    }

    @Override public void onResume() {
        super.onResume();
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override public void onPause() {
        super.onPause();
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraIPTextView = view.findViewById(R.id.CameraIP);
        cameraIPTextView.setText(formatResource(R.string.camera_ip_address,camera.ip));
        cameraPortTextView = view.findViewById(R.id.CameraPort);
        cameraPortTextView.setText(formatResource(R.string.camera_port,camera.port));
        cameraLatitudeTextView = view.findViewById(R.id.CameraLatitude);
        cameraLatitudeTextView.setText(formatResource(R.string.camera_latitude,camera.position.latitude));
        cameraLongitudeTextView = view.findViewById(R.id.CameraLongitude);
        cameraLongitudeTextView.setText(formatResource(R.string.camera_longitude,camera.position.longitude));

        playPauseLayout = view.findViewById(R.id.play_pause_layout);
        errorLayout = view.findViewById(R.id.exo_error_layout);
        errorMessageTextView = view.findViewById(R.id.exo_error_text);
        retryButton = view.findViewById(R.id.exo_error_retry_button);
        retryButton.setOnClickListener(this);
        exoPlayerView = view.findViewById(R.id.ExoPlayerView);
        addPlayer();
    }
}
