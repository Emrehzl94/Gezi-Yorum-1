package com.example.murat.gezi_yorum.Utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.murat.gezi_yorum.Entity.MediaFile;

import java.util.ArrayList;


/**
 * Pager Adapter for media files
 */

public class MediaPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<MediaFile> mediaFiles;

    public MediaPagerAdapter(FragmentManager fm, ArrayList<MediaFile> mediaFiles) {
        super(fm);
        this.mediaFiles= mediaFiles;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment mediaFragment = mediaFiles.get(position).getViewer();
        Bundle extras = new Bundle();
        extras.putLong("fileId", mediaFiles.get(position).id);
        mediaFragment.setArguments(extras);
        return mediaFragment;
    }

    @Override
    public int getCount() {
        return mediaFiles.size();
    }
}
