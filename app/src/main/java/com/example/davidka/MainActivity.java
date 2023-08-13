package com.example.davidka;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_READ_EXT_STORAGE = 200;
    private boolean permissionToReadAccepted = false;
    static final int REQUEST_RECORD_AUDIO_PERMISSION = 201;
    private boolean permissionToRecordAccepted = false;
    static List<SpeakButton> buttons = new ArrayList<>();
    RecyclerView picture_grid;
    SharedPreferences preferences;
    MediaPlayer speak;
    VideoView video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        picture_grid = findViewById(R.id.picture_grid);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.e("pref", preferences.getAll().toString());

        //preferences are loaded after onResume
//        if (preferences.getBoolean("startupSound", false)) {
//            MediaPlayer startup = MediaPlayer.create(this, R.raw.startup_sound_davidka);
//            startup.start();
//            startup.setOnCompletionListener((mediaPlayer) -> startup.release());
//        }
    }

    ActivityResultLauncher<Intent> getPermission = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == RESULT_OK)
                        Log.e("permission result", "can use");
                }
            });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_EXT_STORAGE:
                permissionToReadAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                preferences.edit().putBoolean("addVideo",true);
                break;
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                preferences.edit().putBoolean("addAudio",true).apply();
                break;
        }
        if (!permissionToRecordAccepted)
            preferences.edit().putBoolean("addAudio",false).apply();

        if(!permissionToReadAccepted)
            preferences.edit().putBoolean("addVideo",false).apply();

    }


    @Override
    protected void onResume() {
        super.onResume();

//        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, ChangeLayoutActivity.REQUEST_RECORD_AUDIO_PERMISSION);

        if(preferences.getBoolean("appVolume",false)) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int originalVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            preferences.edit().putInt("originalVolume", originalVol).apply();
            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float prefVol = preferences.getInt("volumeSetting", 100) / 100f;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (prefVol * maxVol), 0);
        }

        if (preferences.getBoolean("startupSound", false)) {
//            MediaPlayer startup = MediaPlayer.create(this, R.raw.startup_sound_davidka);
//            startup.start();
//            startup.setOnCompletionListener((mediaPlayer) -> startup.release());
        }

        //TODO check and get all required permissions here

        //TODO do all this on a seperate thread
        DatabaseHelper db = DatabaseHelper.getDB(this);
        this.buttons = db.speakButtonDao().getAllButtons();
        if (buttons.size() == 0) {
            for (int i = 0; i < 8; i++) {
                SpeakButton button = new SpeakButton(i);
                buttons.add(button);
                db.speakButtonDao().addSpeakButton(button);
            }
        }

        for (SpeakButton button : buttons)
            Log.d("table contents", button.position + ". vid? " + button.isVideo + " image:" + button.getPicture() + " speech:" + button.getSpeak());

        PictureGridAdapter adapter = new PictureGridAdapter(this, buttons);
        picture_grid.setAdapter(adapter);
        picture_grid.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(preferences.getBoolean("appVolume",false)) {
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