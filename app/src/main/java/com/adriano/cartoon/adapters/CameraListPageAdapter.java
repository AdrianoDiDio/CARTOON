package com.adriano.cartoon.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.adriano.cartoon.Camera;
import com.adriano.cartoon.fragments.VideoPlayerFragment;

import java.util.ArrayList;

public class CameraListPageAdapter extends FragmentStateAdapter {
    private ArrayList<Camera> cameraArrayList;
    private int numPages;

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        VideoPlayerFragment videoPlayerFragment = VideoPlayerFragment.newInstance(cameraArrayList.get(position));
        return videoPlayerFragment;
    }

    @Override
    public int getItemCount() {
        return numPages;
    }


    public CameraListPageAdapter(@NonNull Fragment f, ArrayList<Camera> cameraArrayList) {
        super(f);
        this.cameraArrayList = cameraArrayList;
        numPages = cameraArrayList.size();
    }
}
