package com.example.murat.gezi_yorum.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.helpers.PhotoListViewAdapter;

import java.util.ArrayList;

/**
 * Show photos
 */

public class PhotoFragment extends Fragment {
    private int CAMERA_REQUEST = 1;
    private ListView listView;
    private ArrayList<Bitmap> photos;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_fragment, container, false);
        listView = view.findViewById(R.id.photoList);
        photos = new ArrayList<>();
        startNewCameraIntent();
        return view;
    }

    public void startNewCameraIntent(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            photos.add((Bitmap) data.getExtras().get("data"));
            startNewCameraIntent();
        }else {
            listView.setAdapter(new PhotoListViewAdapter(getContext(),photos));
        }

    }

}
