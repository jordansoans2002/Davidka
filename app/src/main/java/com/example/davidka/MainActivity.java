package com.example.davidka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static List<SpeakButton> buttons = new ArrayList<>();
    RecyclerView picture_grid;
//    CardView picture;
//    ImageView img;
//    VideoView vid;
    PictureGridAdapter adapter;
    SharedPreferences preferences;
    MediaPlayer speak;
    VideoView video;
    boolean isStartup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        picture_grid = findViewById(R.id.picture_grid);
//        picture = findViewById(R.id.picture);
//        img = findViewById(R.id.img);
//        vid = findViewById(R.id.vid);
        isStartup = true;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        picture_grid.setAdapter(null);

        new Thread(() -> {
            DatabaseHelper db = DatabaseHelper.getDB(this);
            MainActivity.buttons = db.speakButtonDao().getAllButtons();

            if (buttons.size() == 0) {
                for (int i = 0; i < 8; i++) {
                    SpeakButton button = new SpeakButton(i,null,null,"",false);
                    buttons.add(button);
                    db.speakButtonDao().addSpeakButton(button);
                }
            }

            for (SpeakButton button : buttons)
                Log.d("table contents", button.position + ". vid? " + button.isVideo + " image:" + button.picture + " speech:" + button.speak);


            new Handler(Looper.getMainLooper()).post(() -> {
                adapter = new PictureGridAdapter(this, buttons);
                picture_grid.setAdapter(adapter);
//                vid.setVisibility(View.VISIBLE);
//                vid.setVideoURI(Uri.parse(buttons.get(0).getPicture()));
                picture_grid.setLayoutManager(new GridLayoutManager(this, 2));
            });
        }).start();

        if (preferences.getBoolean("appVolume", false)) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int originalVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            preferences.edit().putInt("originalVolume", originalVol).apply();
            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float prefVol = preferences.getInt("volumeSetting", 100) / 100f;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (prefVol * maxVol), 0);
        }

        if (isStartup && preferences.getBoolean("startupSound", false)) {
            speak = MediaPlayer.create(this, R.raw.startup_sound_plain);
            speak.setOnCompletionListener((mediaplayer) -> {
                mediaplayer.reset();
                mediaplayer.release();
                mediaplayer = null;
            });
            speak.start();
            isStartup = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (preferences.getBoolean("appVolume", false)) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int originalVol = preferences.getInt("originalVolume", 50);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVol, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (speak != null)
            speak.release();
        if (id == R.id.edit_layout) {
            Intent intent = new Intent(this, ChangeLayoutActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.change_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}