package com.example.davidka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SettingsFragment settingsFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_frame,settingsFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_changes:
                AlertBox keepChanges = new AlertBox(
                        this,
                        "Keep changes",
                        "Apply changes to settings?");
                return true;
            case R.id.cancel_changes:
                AlertBox discardChanges = new AlertBox(
                        this,
                        "Discard changes",
                        "Discard changes made to settings?");
                discardChanges.createAlertBox();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}