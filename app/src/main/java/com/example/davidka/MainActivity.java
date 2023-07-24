package com.example.davidka;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<SpeakButton> buttons = new ArrayList<>();
    RecyclerView picture_grid;
    SharedPreferences preferences;
    MediaPlayer speak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        picture_grid = findViewById(R.id.picture_grid);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.e("pref", preferences.getAll().toString());

        if (preferences.getBoolean("startupSound", false)) {
            MediaPlayer startup = MediaPlayer.create(this, R.raw.davidka);
            startup.start();
            startup.setOnCompletionListener((mediaPlayer) -> startup.release());
        }
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
    protected void onResume() {
        super.onResume();

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int originalVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        preferences.edit().putInt("originalVolume",originalVol).apply();
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float prefVol = preferences.getInt("appVolume",100)/100f;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (prefVol*maxVol),0);

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
            Log.d("table contents", button.position + ". vid? " + button.isVideo + " image:" + button.picture + " speech:" + button.speak);

        PictureGridAdapter adapter = new PictureGridAdapter(this, buttons);
        picture_grid.setAdapter(adapter);
        picture_grid.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    protected void onStop() {
        super.onStop();
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int originalVol = preferences.getInt("originalVolume",50);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVol,0);
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