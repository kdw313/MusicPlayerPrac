package ca.bcit.comp3717.lab05.a00957091;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import ca.bcit.comp3717.lab05.a00957091.FileActivity.FileUtils;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1234;

    MediaPlayer curMedia;
    ArrayList<HashMap<String, String>> songList;

    Button          folderButton;
    Button          fileButton;
    ToggleButton    toggleButtonPlayStop;
    ToggleButton    toggleButtonLoop;
    TextView        fileName;
    TextView        durationTotal;
    TextView        durationCurrent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (songList != null) {
            for (int i = 0; i < songList.size(); i++) {
                String fileName = songList.get(i).get("file_name");
                String filePath = songList.get(i).get("file_path");
                //here you will get list of file name and file path that present in your device
                Log.d("file details ", " name =" + fileName + " path = " + filePath);
            }
        }

        /*Set Model*/
        folderButton            = (Button) findViewById(R.id.folderButton);
        fileButton              = (Button) findViewById(R.id.fileButton);
        durationCurrent         = (TextView)findViewById(R.id.durationCurrent);
        durationTotal           = (TextView)findViewById(R.id.durationTotal);
        toggleButtonPlayStop    = (ToggleButton)findViewById(R.id.toggleButtonPlay);
        toggleButtonLoop        = (ToggleButton)findViewById(R.id.toggleButtonLoop);


        /*Toggle Button setup*/
        toggleButtonPlayStop.setTextOn("Stop");
        toggleButtonPlayStop.setTextOff("Play");
        toggleButtonLoop.setTextOn("Loop");
        toggleButtonLoop.setTextOff("Loop");
        toggleButtonLoop.setEnabled(false);

        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display the file chooser dialog
                showChooser();
            }
        });

        fileName = (TextView)findViewById(R.id.filename);
    }

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }


    /**
     * Start& Stop playing Button onClick
     */
    public void onClickToggleStartStop(final View view){
        if(toggleButtonPlayStop.isChecked()){ //on
            curMedia = MediaPlayer.create(MainActivity.this,R.raw.tenacious_d_tribute);
            Toast.makeText(MainActivity.this, "Play", Toast.LENGTH_SHORT).show();
            curMedia.start();
            toggleButtonLoop.setEnabled(true);
        } else if(curMedia.isPlaying()){
            Toast.makeText(MainActivity.this, "Stop", Toast.LENGTH_SHORT).show();
            curMedia.stop();
            toggleButtonLoop.setEnabled(false);
        }
    }

    /**
     * Loop button onClick
     */
    public void onClickToggleLoop(final View view){
        if(curMedia != null)
            if(toggleButtonLoop.isChecked()){
                curMedia.setLooping(true);
            } else {
                curMedia.setLooping(false);
            }
    }

    public static ArrayList<HashMap<String,String>> getPlayList(String rootPath) {
        ArrayList<HashMap<String,String>> fileList = new ArrayList<>();

        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (File file : files) {
                if (file.isDirectory()) {
                    if (getPlayList(file.getAbsolutePath()) != null) {
                        fileList.addAll(getPlayList(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    HashMap<String, String> song = new HashMap<>();
                    song.put("file_path", file.getAbsolutePath());
                    song.put("file_name", file.getName());
                    fileList.add(song);
                }
            }
            return fileList;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            Toast.makeText(MainActivity.this,
                                    "File Selected: " + path, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.d("File Select Err: ", e.getMessage());
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //for folder dialog
    private String[] mFileList;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "//yourdir//");
    private String mChosenFile;
    private static final int DIALOG_LOAD_FILE = 1000;


    private void loadFileList() {
        try {
            mPath.mkdirs();
        }
        catch(SecurityException e) {
            Log.e(TAG, "unable to write on the sd card " + e.toString());
        }
        if(mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return sel.isDirectory();
                }
            };

            mFileList = mPath.list(filter);
        }
        else {
            mFileList= new String[0];
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your folder");

                if(mFileList == null) {
                    Log.e(TAG, "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }

                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];
                        //you can do stuff with the file here too
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }
}
