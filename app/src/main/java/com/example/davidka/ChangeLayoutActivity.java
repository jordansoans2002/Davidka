package com.example.davidka;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gowtham.library.utils.CompressOption;
import com.gowtham.library.utils.TrimVideo;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChangeLayoutActivity extends AppCompatActivity {

    List<SpeakButton> buttons;
    MediaPlayer speak;
    RecyclerView edit_grid;
    CardView add_picture;

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
            } catch (Exception e) {
            }
            pos = -1;
        }
    });
    ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            Intent intent = o.getData();
//            if (o.getResultCode() == RESULT_OK) {
            try {
                SpeakButton button = buttons.get(pos);
                Uri uri = intent == null ? temp_uri : intent.getData();
                Log.e("return uri", uri.toString());
                if (uri.toString().contains("image") || uri.toString().contains("jpg")) {
                    //if image crop it using
                    Uri dest_uri;
                    if(button.getPicture()!=null && !button.isVideo)
                        dest_uri = Uri.parse(button.getPicture());
                    else{
                        if(button.isVideo)
                            new File(Uri.parse(button.getPicture()).getPath()).delete();
                        dest_uri = Uri.fromFile(new File(getFilesDir(), new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()));
                    }

                    Log.e("dest uri", dest_uri.toString());
                    UCrop.of(uri, dest_uri)
                            .withAspectRatio(1, 1)
                            .start(ChangeLayoutActivity.this);
                } else if (uri.toString().contains("video") || uri.toString().contains("mp4")) {//VID,Movies,mp4

                    TrimVideo.activity(uri.toString())
                            .setHideSeekBar(true)
//                            .setAccurateCut(true)
                            .setCompressOption(new CompressOption(30,"1M",1280,1280))
                            .start(ChangeLayoutActivity.this,trimVideo);
                    button.setPicture(uri.toString(), true);
                    adapter.notifyItemChanged(pos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }
        }
    });

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: " + uri);
                    String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
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
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));
                    Log.e("dest uri", uri.toString());
                    SpeakButton button = buttons.get(pos);
                    if(button.getPicture() != null)
                        new File(Uri.parse(button.getPicture()).getPath()).delete();
                    button.setPicture(uri.toString(), true);
                    adapter.notifyItemChanged(pos);
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
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        edit_grid = findViewById(R.id.edit_grid);
        add_picture = findViewById(R.id.add_picture);
        DatabaseHelper db = DatabaseHelper.getDB(this);
        buttons = db.speakButtonDao().getAllButtons();

        adapter = new ChangeGridAdapter(this, pickAudio, pickImage);
        edit_grid.setAdapter(adapter);
        edit_grid.setLayoutManager(new GridLayoutManager(this, 2));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(edit_grid);
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
                keepChanges.createAlertBox(buttons);
                return true;
            case R.id.cancel_changes:
                AlertBox discardChanges = new AlertBox(
                        adapter.changeLayoutActivity,
                        "Discard changes",
                        "Discard changes made to all buttons?");
                discardChanges.createAlertBox();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAbsoluteAdapterPosition();
            int toPosition = target.getAbsoluteAdapterPosition();

            Collections.swap(buttons, fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }

    };
}