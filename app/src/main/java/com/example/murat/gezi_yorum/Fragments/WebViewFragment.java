package com.example.murat.gezi_yorum.Fragments;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.ZipFileDownloader;

/**
 * Social Media WebViewFragment Page Environment
 */

public class WebViewFragment extends Fragment {

    private WebView webView;
    private ProgressBar progressBar;

    private int REQUEST_SELECT_FILE = 1;
    private ValueCallback<Uri[]> mUploadMessage;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);

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
                if(url.startsWith(Constants.APP+"trip-download")) {
                    Intent intent = new Intent(getContext(), ZipFileDownloader.class);
                    intent.putExtra("url", url);
                    getActivity().startService(intent);
                    return true;
                }else if(url.startsWith(Constants.ROOT)){
                    webView.loadUrl(url);
                }else {
                    return super.shouldOverrideUrlLoading(view, request);
                }
                return false;
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
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Intent intent = fileChooserParams.createIntent();
                try
                {
                    mUploadMessage = filePathCallback;
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    mUploadMessage = null;
                    return false;
                }
                return true;

            }

        });
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        try {
            webView.loadUrl(url);
        }catch (Exception e){
            e.printStackTrace();
        }

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
        if(webView.canGoBack()){
            webView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (requestCode == REQUEST_SELECT_FILE) {
            if (mUploadMessage == null)
                return;
            mUploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
        }
        mUploadMessage = null;
    }
}
