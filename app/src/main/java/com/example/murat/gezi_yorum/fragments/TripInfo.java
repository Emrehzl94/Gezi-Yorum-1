package com.example.murat.gezi_yorum.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.classes.Constants;
import com.example.murat.gezi_yorum.classes.MediaFile;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.helpers.MediaGridViewAdapter;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Shows trip info in Timeline fragment under viewpager.
 */

public class TripInfo extends Fragment {
    private int position;
    private ImageButton next, prev;
    TimeLine parentFragment;
    LocationDbOpenHelper helper;

    private GridView photo_preview, video_preview, sound_preview, note_peview;
    private ArrayList<MediaFile> photos;
    private ArrayList<MediaFile> videos;
    private ArrayList<MediaFile> sounds;
    private ArrayList<MediaFile> notes;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("position", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tripinfo_fragment, container, false);
        parentFragment = (TimeLine)getParentFragment();
        next = view.findViewById(R.id.nextbutton);
        prev = view.findViewById(R.id.prevbutton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.setNextPage();
            }
        });
        if(position == 0){
            prev.setVisibility(View.INVISIBLE);
        }else {
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentFragment.setPrevPage();
                }
            });
        }
        TextView header = view.findViewById(R.id.header);
        helper = new LocationDbOpenHelper(getContext());
        long trip_id = parentFragment.getIdAtPosition(position);
        HashMap<String,String> trip_info = helper.getTripInfo(trip_id);
        String headertext = trip_info.get("startdate") + "\n" + trip_info.get("finishdate");
        header.setText(headertext);


        photo_preview = view.findViewById(R.id.photo_preview_grid);
        video_preview = view.findViewById(R.id.video_preview_grid);
        sound_preview = view.findViewById(R.id.sound_preview_grid);
        note_peview = view.findViewById(R.id.note_preview_grid);

        photos = helper.getMediaFiles(trip_id, Constants.PHOTO,"LIMIT 5");
        videos = helper.getMediaFiles(trip_id, Constants.VIDEO,"LIMIT 5");
        sounds = helper.getMediaFiles(trip_id, Constants.SOUNDRECORD,"LIMIT 5");
        notes = helper.getMediaFiles(trip_id, Constants.NOTE,"LIMIT 5");
        photo_preview.setAdapter(new MediaGridViewAdapter(getContext(), MediaFile.getThumbnailArray(getActivity(),photos)));
        photo_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(photos.get(position).path), "image/*");
                startActivity(intent);
            }
        });
        video_preview.setAdapter(new MediaGridViewAdapter(getContext(), MediaFile.getThumbnailArray(getActivity(),videos)));
        video_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            }
        });
        sound_preview.setAdapter(new MediaGridViewAdapter(getContext(), MediaFile.getThumbnailArray(getActivity(), sounds)));
        sound_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            }
        });
        note_peview.setAdapter(new MediaGridViewAdapter(getContext(), MediaFile.getThumbnailArray(getActivity(),notes)));
        note_peview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            }
        });
        return view;
    }
    public void addMarkersToMap(GoogleMap map){
        for (MediaFile photo: photos){
            photo.addToMap(map,getActivity());
        }
        for (MediaFile video: videos){
            video.addToMap(map,getActivity());
        }
        for (MediaFile sound: sounds){
            sound.addToMap(map,getActivity());
        }
        for (MediaFile note: notes){
            note.addToMap(map,getActivity());
        }
    }
}
