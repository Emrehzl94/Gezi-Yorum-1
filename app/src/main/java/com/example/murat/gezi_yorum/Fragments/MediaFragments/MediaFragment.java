package com.example.murat.gezi_yorum.Fragments.MediaFragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.murat.gezi_yorum.Entity.MediaFile;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;

import java.io.File;

/**
 * Media View Adapter
 */

abstract class MediaFragment extends Fragment {
    private LocationDbOpenHelper helper;
    private TextView note;

    private Animation fadeIn;
    private Animation fadeOut;
    protected MediaFile mediaFile;
    FloatingActionButton take_note;
    FloatingActionButton delete;
    FloatingActionButton share_options_button;

    RadioGroup share_options_radio;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = new LocationDbOpenHelper(getContext());

        note = view.findViewById(R.id.note);
        Bundle extras = getArguments();
        final Long mediaFileId = extras.getLong("fileId");
        mediaFile = helper.getMediaFile(mediaFileId);
        note.setText(mediaFile.about_note);

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(500);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                take_note.setVisibility(View.VISIBLE);
                note.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                share_options_button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(500);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                take_note.setVisibility(View.INVISIBLE);
                note.setVisibility(View.INVISIBLE);
                delete.setVisibility(View.INVISIBLE);
                share_options_button.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        take_note = view.findViewById(R.id.take_note);
        delete = view.findViewById(R.id.delete);
        share_options_button = view.findViewById(R.id.share_options);
        take_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.note));
                final EditText noteEdit = new EditText(getContext());
                noteEdit.setText(mediaFile.about_note);
                noteEdit.setId(0);
                builder.setView(noteEdit);
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String noteText = noteEdit.getText().toString();
                        helper.updateMediaNote(mediaFile, noteText);
                        note.setText(noteText);
                        mediaFile.about_note = noteText;
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
                builder.setTitle(getString(R.string.delete_sure));
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        helper.deleteMediaFile(mediaFile.id);
                        File file = new File(mediaFile.path);
                        if(file.exists()){
                            file.delete();
                        }
                    }
                });
                builder.create();
                builder.show();
            }
        });

        share_options_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                        helper.updateShareOption(mediaFile, option);
                        mediaFile.share_option = option;
                    }
                });
                builder.create();
                AlertDialog dialog = builder.show();
                share_options_radio = dialog.findViewById(R.id.share_options_radio);
                int selected = 0;
                switch (mediaFile.share_option){
                    case MediaFile.EVERYBODY:
                        selected = R.id.everybody;
                        break;
                    case MediaFile.MY_FRIENDS:
                        selected = R.id.my_friends;
                        break;
                    case MediaFile.ONLY_ME:
                        selected = R.id.only_me;
                }
                share_options_radio.check(selected);
            }
        });
    }


    protected void toggleControls(){
        if(delete.getVisibility() == View.VISIBLE){
            take_note.setAnimation(fadeOut);
            note.startAnimation(fadeOut);
            delete.startAnimation(fadeOut);
            share_options_button.startAnimation(fadeOut);
        }else {
            take_note.startAnimation(fadeIn);
            note.startAnimation(fadeIn);
            delete.startAnimation(fadeIn);
            share_options_button.startAnimation(fadeIn);
        }
    }
}
