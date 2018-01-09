package com.example.murat.gezi_yorum.Fragments.MediaFragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.example.murat.gezi_yorum.R;

/**
 * Video view fragment
 */

public class VideoFragment extends MediaFragment implements EasyVideoCallback{
    private EasyVideoPlayer player;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        if(mediaFile==null) return;
        player = view.findViewById(R.id.player);
        player.setCallback(this);
        player.setSource(Uri.parse(mediaFile.path));
    }

    @Override
    public void onPause() {
        super.onPause();
        player.pause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.video_fragment,container,false);
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
        toggleControls();
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
        toggleControls();
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {

    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {

    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {

    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {
        toggleControls();
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {

    }
}
