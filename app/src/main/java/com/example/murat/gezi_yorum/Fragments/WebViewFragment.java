package com.example.murat.gezi_yorum.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.ZipFileDownloader;

import java.util.Stack;

/**
 * Social Media WebViewFragment Page Environment
 */

public class WebViewFragment extends Fragment {

    private WebView webView;
    private ProgressBar progressBar;

    private Stack<String> urlStack;
    String currentUrl;

    private FrameLayout customViewContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private WebChromeClient client;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        customViewContainer = view.findViewById(R.id.customViewContainer);
        Bundle extras = getArguments();
        String url = Constants.APP + extras.getString(Constants.PAGE);
        switch (extras.getString(Constants.PAGE)){
            case Constants.HOME:
                getActivity().setTitle(getString(R.string.news_feed));
                break;
            case Constants.PROFILE:
                getActivity().setTitle(getString(R.string.profile));
                break;
            case Constants.SEARCH:
                getActivity().setTitle(getString(R.string.search));
                break;
        }

        webView = view.findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if(url.startsWith(Constants.ROOT)){
                    webView.loadUrl(url);
                    urlStack.push(currentUrl);
                    currentUrl = url;
                    return true;
                }else {
                    return super.shouldOverrideUrlLoading(view, request);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });
        client = new WebChromeClient(){
            private View mVideoProgressView;
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if(customView != null){
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                webView.setVisibility(View.GONE);
                customViewContainer.setVisibility(View.VISIBLE);
                customViewContainer.addView(view);
                customViewCallback = callback;
            }

            @Override
            public View getVideoLoadingProgressView() {
                if(mVideoProgressView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
                }
                return mVideoProgressView;
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                if(customView == null)
                    return;
                webView.setVisibility(View.VISIBLE);
                customViewContainer.setVisibility(View.GONE);
                customView.setVisibility(View.GONE);
                customViewContainer.removeView(customView);
                customViewCallback.onCustomViewHidden();
                customView = null;
            }
        };
        webView.setWebChromeClient(client);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                Intent intent = new Intent(getContext(), ZipFileDownloader.class);
                intent.putExtra("url", url);
                getActivity().startService(intent);
            }
        });
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        webView.loadUrl(url);

        urlStack = new Stack<>();
        currentUrl = url;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment,container,false);
    }

    /**
     * Activity send goBack onBackPressed
     * @return if can go back
     */
    public boolean goBack(){
        if(customView != null){
            webView.onPause();
            client.onHideCustomView();
            return true;
        }
        if(!urlStack.isEmpty()){
            currentUrl = urlStack.pop();
            webView.loadUrl(currentUrl);
            return true;
        }
        return false;
    }
}
