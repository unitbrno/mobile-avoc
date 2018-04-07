package app.bumaza.sk.skodaavoc;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import app.bumaza.sk.skodaavoc.utils.AsyncTaskCompleteListener;
import app.bumaza.sk.skodaavoc.utils.SoundRecorder;
import app.bumaza.sk.skodaavoc.utils.UploadSpeechFetcher;

public class UploadActivity extends AppCompatActivity implements AsyncTaskCompleteListener<String> {


    private TextView speech_tv;
    private FloatingActionButton emailButton;

    private ImageButton picker, recorder;

    private boolean isRecording;

    private SoundRecorder soundRecorder;

    private Context context;
    AsyncTaskCompleteListener<String> listener;
    private static String TAG = "PermissionDemo";
    private static final int RECORD_REQUEST_CODE = 101;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        setContentView(R.layout.activity_upload);
        isRecording = false;
        setTitle("Upload");
        Byte[] array = ArrayUtils.toObject(tobyteArray(0));
        speech_tv = findViewById(R.id.speech_tv);
        //speech_tv.setText("LOADING");
        picker = findViewById(R.id.picker_bt);
        recorder = findViewById(R.id.record_bt);

        context = getApplicationContext();
        listener = this;

        picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(UploadActivity.this)
                        .withRequestCode(1000)
                        //.withFilter(Pattern.compile(".*\\.wav$")) // Filtering files and directories by file name using regexp
                        .withFilterDirectories(true) // Set directories filterable (false by default)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });

        soundRecorder = new SoundRecorder("janko.wav");

        recorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording){
                    recorder.setImageDrawable(getResources().getDrawable(R.drawable.red_small));
                        soundRecorder.stop();
                    new UploadSpeechFetcher(context, listener).execute(ArrayUtils.toObject(soundRecorder.getWavByteArray()));
                }else{
                    recorder.setImageDrawable(getResources().getDrawable(R.drawable.green_small));
                    soundRecorder.recordOnly();

                }


                isRecording = !isRecording;
            }
        });

        emailButton = (FloatingActionButton) findViewById(R.id.email_button);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"budacjozef98@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Prednaska");
                i.putExtra(Intent.EXTRA_TEXT   , speech_tv.getText());
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(UploadActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //new UploadSpeechFetcher(getApplicationContext(), this).execute(array);

    }



    private byte[] tobyteArray(int kolko){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(getResources().openRawResource(R.raw.fam2));

            int read;
            byte[] buff = new byte[1024];
            while ((read = in.read(buff)) > 0)
            {
                out.write(buff, 0, read);
            }
            out.flush();
            byte[] audioBytes = out.toByteArray();

            return audioBytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onTaskComplete(String result) {
        speech_tv.setText(result);
        emailButton.setVisibility(View.VISIBLE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }
}
