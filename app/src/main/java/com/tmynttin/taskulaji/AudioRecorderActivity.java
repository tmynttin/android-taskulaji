package com.tmynttin.taskulaji;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AudioRecorderActivity extends AppCompatActivity {
    private final static String TAG = "AudioRecorder";

    private String recordingPath;
    private File outputFile;
    private MediaRecorder recorder;
    private AudioRecord audioRecord;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        ImageButton recordButton = findViewById(R.id.recordAudioButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordAudio();
            }
        });

        ImageButton playButton = findViewById(R.id.replayAudioButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        try {
            File outputDir = AudioRecorderActivity.this.getCacheDir(); // context being the Activity pointer
            outputFile = File.createTempFile("audio", ".3gp", outputDir);
            recordingPath = outputFile.getAbsolutePath();
            outputFile.setWritable(true);
            Log.d(TAG, "onCreate: file path: " + recordingPath);
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: Creating file failed");
        }

    }

    private void recordAudio() {
        try {

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(recordingPath);

            recorder.prepare();
            recorder.start();   // Recording is now started

            TimeUnit.SECONDS.sleep(5);

            recorder.stop();
            recorder.reset();   // You can reuse the object by going back to setAudioSource() step
            recorder.release(); // Now the object cannot be reused
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void playAudio() {
        try {
            player = new MediaPlayer();
            player.setDataSource(recordingPath);
            player.prepare();
            player.start();
            TimeUnit.SECONDS.sleep(5);
            player.stop();
            player.release();


        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}