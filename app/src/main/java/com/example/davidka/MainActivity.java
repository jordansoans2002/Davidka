package com.example.davidka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    RecyclerView picture_grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        picture_grid = findViewById(R.id.picture_grid);
        PictureGridAdapter adapter = new PictureGridAdapter();
        picture_grid.setAdapter(adapter);
        picture_grid.setLayoutManager(new GridLayoutManager(this,2));
//        picture_grid.canScrollVertically(View.);



        MediaPlayer startup = MediaPlayer.create(this,R.raw.davidka);
        startup.start();

        MediaPlayer sorry = MediaPlayer.create(this,R.raw.sorry);
        MediaPlayer thank_you = MediaPlayer.create(this,R.raw.thank_you);
        MediaPlayer yes = MediaPlayer.create(this,R.raw.yes);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.edit_layout){
            Intent intent = new Intent(this,EditLayoutActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.change_settings) {
            Intent intent = new Intent(this,EditLayoutActivity.class);
            this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}