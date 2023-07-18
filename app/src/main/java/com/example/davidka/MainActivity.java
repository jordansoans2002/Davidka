package com.example.davidka;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    MediaPlayer speak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        picture_grid = findViewById(R.id.picture_grid);
//        PictureGridAdapter adapter = new PictureGridAdapter(this,buttons);
//        picture_grid.setAdapter(adapter);
//        picture_grid.setLayoutManager(new GridLayoutManager(this,2));
//        picture_grid.canScrollVertically(View.);


//        MediaPlayer startup = MediaPlayer.create(this, R.raw.davidka);
//        startup.start();

//        MediaPlayer sorry = MediaPlayer.create(this,R.raw.sorry);
//        MediaPlayer thank_you = MediaPlayer.create(this,R.raw.thank_you);
//        MediaPlayer yes = MediaPlayer.create(this,R.raw.yes);

//        findViewById(R.id.grid_1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                yes.start();
//            }
//        });
//        findViewById(R.id.grid_3).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sorry.start();
//            }
//        });
//        findViewById(R.id.grid_4).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                thank_you.start();
//            }
//        });


    }

    ActivityResultLauncher<Intent> getPermission = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == RESULT_OK)
                        Log.e("permision result", "can use");
                }
            });

    @Override
    protected void onResume() {
        super.onResume();

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
            Log.d("table contents", button.position + ". image:" + button.picture + " speech:" + button.speak);

        PictureGridAdapter adapter = new PictureGridAdapter(this, buttons);
        picture_grid.setAdapter(adapter);
        picture_grid.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        speak.release();
        if (id == R.id.edit_layout) {
            Intent intent = new Intent(this, ChangeLayoutActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.change_settings) {
            Intent intent = new Intent(this, ChangeLayoutActivity.class);
            this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}