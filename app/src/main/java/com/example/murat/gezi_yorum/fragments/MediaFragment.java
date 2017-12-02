package com.example.murat.gezi_yorum.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;

/**
 * Media View Adapter
 */

abstract class MediaFragment extends Fragment {
    protected BottomSheetBehavior behavior;
    private LocationDbOpenHelper helper;
    private TextView note;

    protected MediaFile mediaFile;
    Button take_note;
    Button delete;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = new LocationDbOpenHelper(getContext());

        note = view.findViewById(R.id.note);
        Bundle extras = getArguments();
        Long mediaFileId = extras.getLong("fileId");
        mediaFile = helper.getMediaFile(mediaFileId);
        note.setText(mediaFile.about_note);

        take_note = view.findViewById(R.id.take_note);
        delete = view.findViewById(R.id.delete);
        take_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Note");
                final EditText noteEdit = new EditText(getContext());
                noteEdit.setText(mediaFile.about_note);
                noteEdit.setId(0);
                builder.setView(noteEdit);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String noteText = noteEdit.getText().toString();
                        helper.updateNote(mediaFile, noteText);
                        note.setText(noteText);
                    }
                });
                builder.create();
                builder.show();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete");
                builder.setMessage("Are you sure want to delete?");
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        helper.deleteMediaFile(mediaFile.id);
                    }
                });
                builder.create();
                builder.show();
            }
        });

        View bottomsheet = view.findViewById(R.id.bottomsheet);
        behavior = BottomSheetBehavior.from(bottomsheet);
        behavior.setState(BottomSheetBehavior.STATE_DRAGGING);
        behavior.setHideable(true);
        behavior.setPeekHeight(200);
    }


    protected void toggleBehaviour(){
        if(behavior.getState() == BottomSheetBehavior.STATE_HIDDEN){
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
}
