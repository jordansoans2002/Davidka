package com.example.davidka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class EditLayoutActivity extends AppCompatActivity {

    RecyclerView edit_grid;
    CardView add_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_layout);
        edit_grid = findViewById(R.id.edit_grid);
        add_picture = findViewById(R.id.add_picture);

        EditGridAdapter adapter = new EditGridAdapter(this);
        edit_grid.setAdapter(adapter);
        edit_grid.setLayoutManager(new GridLayoutManager(this,2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_action_bar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Intent intent = new Intent(this,MainActivity.class);

        if(id== R.id.save_changes){
            //TODO create a popup to confirm then go back to home
        } else if (id == R.id.cancel_changes) {
            //TODO create a popup to confirm
            //if back is pressed use same popup
        }
        return super.onOptionsItemSelected(item);
    }
}