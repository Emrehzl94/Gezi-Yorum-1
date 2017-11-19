package com.example.murat.gezi_yorum;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;

public class RecordAudio extends AppCompatActivity {

    private boolean recordStarted = false;
    private MediaRecorder mediaRecorder;
    private Uri outputFile;
    private FloatingActionButton recordStartStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);

        outputFile = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        recordStartStop = (FloatingActionButton) findViewById(R.id.start_record);
        recordStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(RecordAudio.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(RecordAudio.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                    return;
                }
                if(!recordStarted){
                    try {
                        mediaRecorder = new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                        mediaRecorder.setOutputFile(outputFile.getPath());
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recordStarted = true;
                    recordStartStop.setImageResource(android.R.drawable.ic_media_pause);
                    Snackbar.make(view,"Record started.",Snackbar.LENGTH_INDEFINITE).show();
                    RecordAudio.this.setResult(Activity.RESULT_CANCELED);
                }else {
                    mediaRecorder.stop();
                    recordStartStop.setImageResource(android.R.drawable.ic_btn_speak_now);
                    recordStarted = false;
                    RecordAudio.this.setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });
    }

}
