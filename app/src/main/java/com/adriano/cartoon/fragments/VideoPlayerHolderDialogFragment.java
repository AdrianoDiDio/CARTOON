package com.adriano.cartoon.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.adriano.cartoon.Camera;
import com.adriano.cartoon.Constants;
import com.adriano.cartoon.R;
import com.adriano.cartoon.adapters.CameraListPageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class VideoPlayerHolderDialogFragment extends DialogFragment implements
        View.OnClickListener, ChildParentPlayerErrorNotification {
    private static final String CAMERA_OBJECT_KEY = "CameraObject";
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private CameraListPageAdapter cameraListPageAdapter;
    private ArrayList<Camera> cameraArrayList;
    private ImageButton closeButton;

    public static void PrepareFragmentManager(FragmentManager fragmentManager) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
    }

    public static VideoPlayerHolderDialogFragment newInstance(ArrayList<Camera> cameraArrayList) {
        VideoPlayerHolderDialogFragment f = new VideoPlayerHolderDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(CAMERA_OBJECT_KEY, cameraArrayList);
        f.setArguments(args);
        return f;
    }

    private void Exit(VideoPlayerExitCode exitCode) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.DIALOG_EXIT_CODE, exitCode);
        Intent intent = new Intent().putExtras(bundle);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }

    @Override
    public void onChildPlayerError(VideoPlayerExitCode exitCode) {
        Exit(exitCode);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == closeButton.getId()) {
            Exit(VideoPlayerExitCode.EXIT_OK);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE,
                R.style.Theme_Black_TitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        cameraArrayList = getArguments().getParcelableArrayList(CAMERA_OBJECT_KEY);
        View view = inflater.inflate(R.layout.dialog_video_player_holder, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        int numPages = Utils.RoundUp(cameraArrayList.size(),4);
        cameraListPageAdapter = new CameraListPageAdapter(this, cameraArrayList);
        viewPager = view.findViewById(R.id.CameraPager);
        viewPager.setAdapter(cameraListPageAdapter);
        tabLayout = view.findViewById(R.id.CameraPagerTabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(cameraArrayList.get(position).name)
        ).attach();
        closeButton = view.findViewById(R.id.video_player_holder_dialog_button);
        closeButton.setOnClickListener(this);
    }
}
