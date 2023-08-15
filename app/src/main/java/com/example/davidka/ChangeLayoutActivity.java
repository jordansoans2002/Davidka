package com.example.davidka;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.gowtham.library.utils.TrimVideo;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChangeLayoutActivity extends AppCompatActivity {

    List<SpeakButton> buttons;
    RecyclerView edit_grid;
    CardView add_button;
    ImageView add_button_image;

    static int pos = -1;
    static Uri temp_uri = null;
    private boolean permissionToRecordAccepted = false;
    static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    ChangeGridAdapter adapter;
    SharedPreferences preferences;


    ActivityResultLauncher<Intent> pickAudio = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            Intent intent = o.getData();
            try {
                Uri uri = intent.getData();
                Log.d("choose intent", "Selected URI: " + uri + " " + uri.getPath() + " position: " + pos);
                SpeakButton button = buttons.get(pos);
                button.setSpeak(uri.toString());
                adapter.notifyItemChanged(pos);
//            } else {
//                Log.d("choose intent", "no uri in data");
            } catch (Exception ignored) {
            }
            pos = -1;
        }
    });
    ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            Intent intent = o.getData();
            Uri uri = intent == null ? temp_uri : intent.getData();
            if (o.getResultCode() == RESULT_OK && uri != null) {
                Log.e("return uri", uri.toString());
                if (uri.toString().contains("image") || uri.toString().contains("jpeg") || uri.toString().contains("jpg")) {
                    Uri dest_uri = Uri.fromFile(new File(getFilesDir(), UUID.randomUUID().toString() + ".jpg"));
                    Log.e("dest uri", dest_uri.toString());
                    UCrop.of(uri, dest_uri)
                            .withAspectRatio(1, 1)
                            .start(ChangeLayoutActivity.this);
                } else if (uri.toString().contains("video") || uri.toString().contains("mp4")) {//VID,Movies,mp4

                    TrimVideo.activity(uri.toString())
                            .setHideSeekBar(true)
                            .setAccurateCut(true)
                            .start(ChangeLayoutActivity.this, trimVideo);

                }
            }
        }
    });

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: " + uri);
                    String dest_uri = UUID.randomUUID().toString() + ".jpg";
                    UCrop.of(uri, Uri.fromFile(new File(getFilesDir(), dest_uri)))
                            .withAspectRatio(1, 1)
                            .start(ChangeLayoutActivity.this);
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });


    ActivityResultLauncher<Intent> trimVideo = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));
                    Log.e("dest uri", uri.toString());
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
            Log.e("result uri", resultUri.toString());
            adapter.notifyItemChanged(pos);
            pos = -1;
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_layout);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, ChangeLayoutActivity.REQUEST_RECORD_AUDIO_PERMISSION);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        edit_grid = findViewById(R.id.edit_grid);
        add_button = findViewById(R.id.add_button);
        add_button_image = findViewById(R.id.add_button_image);

        new Thread(() -> {
            DatabaseHelper db = DatabaseHelper.getDB(this);
            buttons = db.speakButtonDao().getAllButtons();

            new Handler(Looper.getMainLooper()).post(() -> {
                adapter = new ChangeGridAdapter(this, pickAudio, pickImage);
                edit_grid.setAdapter(adapter);
                edit_grid.setLayoutManager(new GridLayoutManager(this, 2));
            });
        }).start();

        //doesnt allow the mic button to detect long press release
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
//        itemTouchHelper.attachToRecyclerView(edit_grid);

        add_button.setOnClickListener((view) -> {
            buttons.add(new SpeakButton(buttons.size(),null,null,"",false));
            adapter.notifyItemInserted(buttons.size() - 1);

            new Thread(() -> {
                DatabaseHelper db = DatabaseHelper.getDB(this);
                db.speakButtonDao().addSpeakButton(buttons.get(buttons.size() - 1));
            }).start();
        });
        add_button.setOnDragListener((view, dragEvent) -> {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    add_button_image.setImageResource(R.drawable.baseline_delete_24);
                    add_button_image.setBackgroundColor(Color.RED);
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    add_button.setScaleY(1.3f);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    add_button.setScaleX(1f);
                    add_button.setScaleY(1f);
                    return true;

                case DragEvent.ACTION_DROP:
                    int pos = Integer.parseInt(dragEvent.getClipData().getItemAt(0).getText().toString());
                    String title, msg;
                    if (buttons.size() >= 8) {
                        title = "Delete button";
                        msg = "Are you sure you want to delete this button?";
                    } else {
                        title = "Clear button";
                        msg = "Are you sure you want to remove all media from this button?";
                    }

                    AlertBox removeButton = new AlertBox(ChangeLayoutActivity.this, title, msg);
                    removeButton.deleteButton(adapter, buttons, pos);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    add_button_image.setImageResource(R.drawable.baseline_add_24);
                    add_button_image.setBackgroundColor(Color.WHITE);
                    return true;

                default:
                    return false;
            }
        });
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
                        "Save changes made to all buttons?");
                keepChanges.gridChanges(buttons, true);
                return true;
            case R.id.cancel_changes:
                AlertBox discardChanges = new AlertBox(
                        adapter.changeLayoutActivity,
                        "Discard changes",
                        "Discard changes made to all buttons?");
                discardChanges.gridChanges(buttons, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        AlertBox discardChanges = new AlertBox(
                adapter.changeLayoutActivity,
                "Discard changes",
                "Discard changes made to all buttons?");
        discardChanges.gridChanges(buttons, false);
    }

    //TODO causes the record button to not detect the finger up
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            if (!ChangeGridAdapter.longPress) {
                int fromPosition = viewHolder.getAbsoluteAdapterPosition();
                int toPosition = target.getAbsoluteAdapterPosition();

                Collections.swap(buttons, fromPosition, toPosition);
//                SpeakButton btn = new SpeakButton(-1);
//                btn.swap(buttons.get(fromPosition));
//                buttons.get(fromPosition).swap(buttons.get(toPosition));
//                buttons.get(toPosition).swap(btn);

                adapter.notifyItemMoved(fromPosition, toPosition);
            }
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }

    };

}