package com.example.murat.gezi_yorum;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.MediaGridViewAdapter;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    ArrayList<MediaFile> mediaFiles;
    private long trip_id;
    GridView gridView;
    LocationDbOpenHelper helper;
    ArrayList<Integer> selectedFiles;

    private AdapterView.OnItemClickListener showListener;
    private AdapterView.OnItemClickListener selectListener;
    private AdapterView.OnItemLongClickListener longClickListener;
    private View controlPanel;

    private ImageButton selectAll;
    private ImageButton setShareOption;
    private ImageButton delete;

    private Boolean selectedAll = false;
    RadioGroup share_options_radio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        trip_id = getIntent().getExtras().getLong(Trip.TRIPID);

        helper = new LocationDbOpenHelper(this);

        showListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mediaFiles.get(i).startActivityForView(GalleryActivity.this);
            }
        };

        selectListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!selectedFiles.contains(i)){
                    selectedFiles.add(i);
                    view.setPadding(15,15,15,15);
                }else {
                    selectedFiles.remove(selectedFiles.indexOf(i));
                    view.setPadding(2,2,2,2);
                }
                setSelectedAll();
            }
        };
        controlPanel = findViewById(R.id.contentPanel);
        selectAll = findViewById(R.id.select_all);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedAll){
                    for(int i=0;i<gridView.getChildCount(); i++){
                        View item = gridView.getChildAt(i);
                        item.setPadding(2,2,2,2);
                    }
                    selectedFiles.clear();
                }else {
                    for(int i=0;i<gridView.getChildCount(); i++){
                        View item = gridView.getChildAt(i);
                        if(!selectedFiles.contains(i)){
                            selectedFiles.add(i);
                            item.setPadding(15,15,15,15);
                        }
                    }
                }
                setSelectedAll();
            }
        });

        delete = findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                builder.setTitle(getString(R.string.delete_sure));
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for(Integer item : selectedFiles){
                            helper.deleteMediaFile(mediaFiles.get(item).id);
                        }
                        mediaFiles = helper.getMediaFiles(trip_id, null, null, null);
                        gridView.setAdapter(new MediaGridViewAdapter(GalleryActivity.this, mediaFiles));
                        selectedFiles.clear();
                    }
                });

                builder.create();
                builder.show();
            }
        });

        setShareOption = findViewById(R.id.share_options);
        setShareOption.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                builder.setTitle(getString(R.string.share_options));
                builder.setView(R.layout.share_options_radio_group);
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int selectedId = share_options_radio.getCheckedRadioButtonId();
                        String option = "";
                        switch (selectedId){
                            case R.id.everybody:
                                option = MediaFile.EVERYBODY;
                                break;
                            case R.id.my_friends:
                                option = MediaFile.MY_FRIENDS;
                                break;
                            case R.id.only_me:
                                option = MediaFile.ONLY_ME;
                        }
                        for(Integer item : selectedFiles){
                            helper.updateShareOption(mediaFiles.get(item), option);
                            mediaFiles.get(item).share_option = option;
                        }
                    }
                });
                builder.create();
                AlertDialog dialog = builder.show();
                share_options_radio = dialog.findViewById(R.id.share_options_radio);
                int selected = R.id.everybody;
                share_options_radio.check(selected);
            }
        });
        gridView = findViewById(R.id.preview);
        gridView.setOnItemClickListener(showListener);
        longClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!selectedFiles.contains(i)){
                    selectedFiles.add(i);
                    view.setPadding(15,15,15,15);
                }else {
                    selectedFiles.remove(selectedFiles.indexOf(i));
                    view.setPadding(2,2,2,2);
                }
                setSelectedAll();
                gridView.setOnItemClickListener(selectListener);
                controlPanel.setVisibility(View.VISIBLE);
                gridView.setOnItemLongClickListener(null);
                return true;
            }
        };
        gridView.setOnItemLongClickListener(longClickListener);

        selectedFiles = new ArrayList<>();
    }

    @Override
    public void onBackPressed() {
        if(gridView.getOnItemClickListener() == selectListener){
            gridView.setOnItemClickListener(showListener);
            for(Integer item : selectedFiles){
                View view = gridView.getChildAt(item);
                view.setPadding(2,2,2,2);
            }
            selectedFiles.clear();
            controlPanel.setVisibility(View.GONE);
            gridView.setOnItemLongClickListener(longClickListener);
            setSelectedAll();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaFiles = helper.getMediaFiles(trip_id, null, null, null);
        MediaGridViewAdapter adapter = new MediaGridViewAdapter(this, mediaFiles);
        gridView.setAdapter(adapter);
    }

    private void setSelectedAll(){
        selectedAll = gridView.getChildCount() == selectedFiles.size();
        if(selectedAll){
            selectAll.setImageResource(R.drawable.ic_action_unselect_all);
        }else {
            selectAll.setImageResource(R.drawable.ic_action_select_all);
        }
    }
}
