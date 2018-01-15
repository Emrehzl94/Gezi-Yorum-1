package com.example.murat.gezi_yorum.Fragments.TripControllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Path;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.GalleryActivity;
import com.example.murat.gezi_yorum.MediaActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.ZipFileUploader;

import java.io.File;
import java.util.ArrayList;

/**
 * Shows trip info in Timeline fragment under viewpager.
 */

public class TripInfo extends TripSummary {
    private static int CHOOSE_COVER_REQUEST = 1;

    private int position;
    TimeLine parentFragment;
    private Boolean isLast;
    private ImageView cover;

    private Button share_trip;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        helper = new LocationDbOpenHelper(getContext());
        Long trip_id = bundle.getLong(Trip.TRIPID, -1);
        trip = helper.getTrip(trip_id);
        position = bundle.getInt("position",0);
        isLast = bundle.getBoolean("islast", false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpView(getView());
        if(parentFragment.currentPosition == position && parentFragment.getMap() != null)
            requestToDrawPathOnMap(parentFragment.getMap(), false, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tripinfo_fragment, container, false);
        parentFragment = (TimeLine)getParentFragment();
        ImageButton next = view.findViewById(R.id.nextbutton);
        ImageButton prev = view.findViewById(R.id.prevbutton);
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
        if(isLast){
            next.setVisibility(View.INVISIBLE);
        }else {
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentFragment.setNextPage();
                }
            });
        }
        TextView header = view.findViewById(R.id.header);
        Trip trip_info = helper.getTrip(trip.id);
        String headertext = trip_info.name;
        header.setText(headertext);
        setUpView(view);

        cover = view.findViewById(R.id.cover);
        Button changeCover = view.findViewById(R.id.change_cover);

        Button more = view.findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), GalleryActivity.class);
                intent.putExtra(Trip.TRIPID, trip.id);
                startActivity(intent);
            }
        });

        Button deleteTrip = view.findViewById(R.id.delete_trip);
        deleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.delete_sure));
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Long> paths = helper.getPathsIDs(trip.id);
                        for(Long path_id : paths ){
                            Path path = helper.getPath(path_id);
                            File pathfile = path.getFile();
                            if(pathfile.exists())
                                path.getFile().delete();
                            helper.deletePath(path_id);
                        }
                        ArrayList<MediaFile> mediaFiles = helper.getMediaFiles(trip.id, null, null, null);
                        for(MediaFile file: mediaFiles){
                            File media = new File(file.path);
                            if(media.exists()){
                                media.delete();
                            }
                            helper.deleteMediaFile(file.id);
                        }
                        helper.deleteTrip(trip.id);
                        parentFragment.loadAdapter();
                    }
                });
                builder.create();
                builder.show();
            }
        });
        share_trip = view.findViewById(R.id.share_trip);
        String start_end_text = trip.getStartdate() + " - " + trip.getFinishdate();
        Button start_end = view.findViewById(R.id.start_end);
        start_end.setText(start_end_text);

        if(trip.isImported){
            cover.setVisibility(View.GONE);
            share_trip.setVisibility(View.GONE);
            changeCover.setVisibility(View.GONE);
            edit_name.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.last_added).setVisibility(View.INVISIBLE);
            return view;
        }else if(trip.cover_media_id != -1){
            cover.setImageBitmap(ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeFile(helper.getMediaFile(trip.cover_media_id).path)
                    , 400, 400
            ));
        }
        changeCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(helper.getMediaFileCount(trip.id, MediaFile.PHOTO) > 0) {
                    Intent intent = new Intent(getContext(), MediaActivity.class);
                    intent.putExtra(Trip.TRIPID, trip.id);
                    startActivityForResult(intent, CHOOSE_COVER_REQUEST);
                }else {
                    Snackbar.make(view, getString(R.string.no_media), Snackbar.LENGTH_LONG).show();
                }
            }
        });


        if(trip.isShared){
            share_trip.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
            share_trip.setText(R.string.shared_before);
        }else {
            share_trip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!trip.isShared) {
                        long totalLength = 0;
                        ArrayList<MediaFile> mediaFiles = helper.getMediaFiles(trip.id, null, null, null);
                        for(MediaFile mediaFile : mediaFiles){
                            File media = new File(mediaFile.path);
                            totalLength += media.length();
                        }
                        String message = getString(R.string.size) + (totalLength/(1024*1024)) + "MB";
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(message);
                        builder.setMessage(R.string.sure_to_upload);
                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Snackbar.make(getView(), getString(R.string.trip_sharing), Snackbar.LENGTH_LONG).show();
                                Intent intent = new Intent(getContext(), ZipFileUploader.class);
                                intent.putExtra(Trip.TRIPID, trip.id);
                                getActivity().startService(intent);
                                share_trip.setText(R.string.sharing);
                                share_trip.setOnClickListener(null);
                            }
                        });
                        builder.create();
                        builder.show();
                    }else {
                        Snackbar.make(view, getString(R.string.shared_before), Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
        return view;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CHOOSE_COVER_REQUEST) {
                trip = helper.getTrip(trip.id);
                cover.setImageBitmap(ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(helper.getMediaFile(trip.cover_media_id).path)
                        , 400, 400
                ));
            }
        }
    }
}
