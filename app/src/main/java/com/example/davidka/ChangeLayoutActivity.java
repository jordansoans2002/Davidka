package com.example.davidka;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gowtham.library.utils.TrimType;
import com.gowtham.library.utils.TrimVideo;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class ChangeLayoutActivity extends AppCompatActivity {

    List<SpeakButton> buttons;
    static int addedButtons = 0, deletedButtons = 0;
    static boolean cleanUp = false;
    RecyclerView edit_grid;
    CardView add_button;
    ImageView add_button_image;

    static int pos = -1;
    static Uri temp_uri = null;
    //    private boolean permissionToRecordAccepted = false;
//    private boolean  permissionToReadExtStorage = false;
    static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 201;
    static final long MIN_VID_DURATION = 2;
    static final long MAX_VID_DURATION = 30;
    ChangeGridAdapter adapter;
    GridLayoutManager gridLayoutManager;
    SharedPreferences preferences;


    ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            try {
                Intent intent = o.getData();
                Uri uri = intent == null ? temp_uri : intent.getData();
                if (o.getResultCode() == RESULT_OK && uri != null) {
                    if (uri.toString().contains("image") || uri.toString().contains("jpeg") || uri.toString().contains("jpg")) {
                        Uri dest_uri = Uri.fromFile(new File(getFilesDir(), UUID.randomUUID().toString() + ".jpg"));
                        UCrop.of(uri, dest_uri)
                                .withAspectRatio(1, 1)
                                .start(ChangeLayoutActivity.this);
                    } else if (uri.toString().contains("video") || uri.toString().contains("mp4")) {//VID,Movies,mp4

                        TrimVideo.activity(uri.toString())
                                .setHideSeekBar(false)
                                .setAccurateCut(true)
                                .setTrimType(TrimType.MIN_MAX_DURATION)
                                .setMinToMax(MIN_VID_DURATION, MAX_VID_DURATION)
                                .start(ChangeLayoutActivity.this, trimVideo);

                    }
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    });

    ActivityResultLauncher<Intent> trimVideo = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));
                    buttons.get(pos).setPicture(uri.toString(), true);
                    adapter.notifyItemChanged(pos);
                    pos = -1;
                }
            }
    );


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            SpeakButton button = buttons.get(pos);
            button.setPicture(resultUri.toString(), false);
            adapter.notifyItemChanged(pos);
            pos = -1;
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(
                    this,
                    "Video was not recorded",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case REQUEST_RECORD_AUDIO_PERMISSION:
//                permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                break;
//            case REQUEST_READ_EXTERNAL_STORAGE_PERMISSION:
//                 permissionToReadExtStorage = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                 break;
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_layout);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        edit_grid = findViewById(R.id.edit_grid);
        add_button = findViewById(R.id.add_button);
        add_button_image = findViewById(R.id.add_button_image);

        new Thread(() -> {
            DatabaseHelper db = DatabaseHelper.getDB(this);
            buttons = db.speakButtonDao().getAllButtons();

            new Handler(Looper.getMainLooper()).post(() -> {
                adapter = new ChangeGridAdapter(this, pickImage);
                edit_grid.setAdapter(adapter);
                gridLayoutManager = new GridLayoutManager(this, 2);
                edit_grid.setLayoutManager(gridLayoutManager);

            });
        }).start();

        add_button.setOnClickListener((view) -> {
            SpeakButton button = new SpeakButton(buttons.size(), null, null, "", false);
            buttons.add(button);
            adapter.notifyItemInserted(buttons.size() - 1);
            edit_grid.scrollToPosition(buttons.size() - 1);

            addedButtons++;
            deletedButtons--;
//            new Thread(() -> {
//                DatabaseHelper db = DatabaseHelper.getDB(this);
//                db.speakButtonDao().addSpeakButton(buttons.get(buttons.size() - 1));
//                deletedButtons--;
//            }).start();
        });

        add_button.setOnDragListener((view, dragEvent) -> {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    add_button_image.setImageResource(R.drawable.baseline_delete_24);
                    add_button_image.setBackgroundColor(Color.RED);
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    add_button.setScaleY(1.2f);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    add_button.setScaleX(1f);
                    add_button.setScaleY(1f);
                    return true;

                case DragEvent.ACTION_DROP:
                    add_button.setScaleX(1f);
                    add_button.setScaleY(1f);
                    int pos = Integer.parseInt(dragEvent.getClipData().getItemAt(0).getText().toString());
                    SpeakButton button = buttons.get(pos);
                    deletedButtons++;
                    addedButtons--;
                    button.deleteButton();
                    if (buttons.size() > 8) {
                        buttons.remove(pos);
                        adapter.notifyItemRemoved(pos);
                    } else
                        adapter.notifyItemChanged(pos);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    add_button_image.setImageResource(R.drawable.baseline_add_24);
                    add_button_image.setBackgroundColor(Color.WHITE);
                    add_button.setScaleX(1f);
                    add_button.setScaleY(1f);
                    return true;

                default:
                    return false;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!cleanUp) {
            new Thread(() -> {
                DatabaseHelper db = DatabaseHelper.getDB(this);
                for (int i = 0; i < buttons.size(); i++) {
                    SpeakButton button = buttons.get(i);

                    if (button.rootSpeak.getNext() == null && button.rootPicture.getNext() == null)
                        continue;
                    if (button.rootSpeak.getNext() != null)
                        button.deleteUpdates(getFilesDir(), button.rootSpeak, false);

                    if (button.rootPicture.getNext() != null) {
                        if (button.leafSpeak.getUri() != null && button.isVideo)
                            SpeakButton.deleteFile(getFilesDir(), button.rootSpeak.getUri(), false);
                        button.deleteUpdates(getFilesDir(), button.rootPicture, false);
                    }
                }
                for (int i = buttons.size(); i < buttons.size() + ChangeLayoutActivity.deletedButtons; i++)
                    db.speakButtonDao().deleteButtonByPosition(i);
            }).start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save_changes) {
            AlertBox keepChanges = new AlertBox(
                    this,
                    "Keep changes",
                    "Save changes made to all buttons?");
            keepChanges.gridChanges(buttons, true);
            return true;
        } else if (item.getItemId() == R.id.cancel_changes) {
            AlertBox discardChanges = new AlertBox(
                    adapter.changeLayoutActivity,
                    "Discard changes",
                    "Discard changes made to all buttons?");
            discardChanges.gridChanges(buttons, false);
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK)
            Log.e("back", "back button pressed keydown");
        AlertBox discardChanges = new AlertBox(
                adapter.changeLayoutActivity,
                "Discard changes",
                "Discard changes made to all buttons?");
        discardChanges.gridChanges(buttons, false);
        return false;
    }

    @Override
    public void onBackPressed() {
        Log.e("back", "back button pressed onBackPressed");
        AlertBox discardChanges = new AlertBox(
                adapter.changeLayoutActivity,
                "Discard changes",
                "Discard changes made to all buttons?");
        discardChanges.gridChanges(buttons, false);
    }
}