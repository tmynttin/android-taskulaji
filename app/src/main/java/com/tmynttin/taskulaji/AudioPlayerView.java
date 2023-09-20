package com.tmynttin.taskulaji;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tmynttin.taskulaji.document.Helpers;
import com.tmynttin.taskulaji.listeners.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * TODO: document your custom view class.
 */
public class AudioPlayerView extends ConstraintLayout {

    private static final String TAG = "AudioPlayer";

    private JSONArray audioDataset;
    private MediaPlayer mediaPlayer;
    private int audioIndex;

    private ImageButton previousButton;
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private TextView audioDescription;

    public AudioPlayerView(Context context) {
        super(context);
        init(context);
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.audio_player_view, this);

        setVisibility(GONE);

        previousButton = (ImageButton)findViewById(R.id.previousAudioButton);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audioIndex > 0) {
                    audioIndex--;
                    mediaPlayer.stop();
                    setAudioUrl(audioIndex);
                    mediaPlayer.start();
                    setPlayAudioButton(false);
                }
            }
        });

        playPauseButton = (ImageButton)findViewById(R.id.playAudioButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    setPlayAudioButton(true);
                }
                else {
                    mediaPlayer.start();
                    setPlayAudioButton(false);
                }
            }
        });

        nextButton = (ImageButton)findViewById(R.id.nextAudioButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioIndex < audioDataset.length() - 1) {
                    audioIndex++;
                    mediaPlayer.stop();
                    setAudioUrl(audioIndex);
                    mediaPlayer.start();
                    setPlayAudioButton(false);
                }
            }
        });

        audioDescription = (TextView)findViewById(R.id.audioDescription);
    }

    public void updateDataset(JSONObject taxoInformation) {
        audioDataset = new JSONArray();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        setVisibility(GONE);

        String scientificName = taxoInformation.optString("scientificName");
        String synonymNames = taxoInformation.optString("synonymNames");

        fetchAudio(scientificName);
        if (synonymNames != "") {
            fetchAudio(synonymNames);
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            setPlayAudioButton(true);
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    private void fetchAudio(String name) {
        Log.d(TAG, "fetchAudio: " + name);
        Communication.getXenoCantoAudio(name, new RequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: Xeno Canto: " + response);
                try {
                    JSONObject responseJSON = new JSONObject(response);
                    JSONArray recordings = responseJSON.getJSONArray("recordings");
                    if (recordings.length() > 0) {
                        audioDataset = Helpers.concatArray(audioDataset, recordings);
                        if (mediaPlayer == null) {
                            initializeMediaplayer();
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "onError: " + message);
            }
        });
    }

    private void initializeMediaplayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setPlayAudioButton(true);
            }
        });
        setVisibility(VISIBLE);
        setAudioUrl(0);
    }

    private void setAudioUrl(int index) {
        if (audioDataset.length() > 0) {
            JSONObject audioData = audioDataset.optJSONObject(index);
            String sonoPath = audioData.optJSONObject("sono").optString("small");
            String fileName = audioData.optString("file-name");
            String baseUrl = sonoPath.substring(0, sonoPath.indexOf("/", 40));
            String audioUrl = "https:" + baseUrl + "/" + fileName;
            String gen = audioData.optString("gen");
            String sp = audioData.optString("sp");
            String type = audioData.optString("type");
            String rec = audioData.optString("rec");
            String cnt = audioData.optString("cnt");
            String loc = audioData.optString("loc");
            audioDescription.setText(String.format("%s\nRecorder: %s\n%s, %s", type, rec, loc, cnt));

            try {
                setPlayAudioButton(true);
                mediaPlayer.reset();
                if(isUrlValid(audioUrl)) {
                    mediaPlayer.setDataSource(audioUrl);
                    mediaPlayer.prepare();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setPlayAudioButton(boolean isPlayButton) {
        int id = getResources().getIdentifier("android:drawable/ic_media_pause", null, null);
        if (isPlayButton) {
            id = getResources().getIdentifier("android:drawable/ic_media_play", null, null);
        }
        playPauseButton.setImageResource(id);
    }

    private boolean isUrlValid(String url) {
            if (!url.contains("mp3")) {
                Log.d(TAG, "isUrlValid: NO file");
                return false;
            }
            else {
                Log.d(TAG, "isUrlValid: YES");
                return true;
            }

    }
}