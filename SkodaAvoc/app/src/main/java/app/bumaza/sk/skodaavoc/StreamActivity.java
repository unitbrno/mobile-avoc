package app.bumaza.sk.skodaavoc;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.library.bubbleview.BubbleTextView;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import app.bumaza.sk.skodaavoc.utils.AsyncTaskCompleteListener;
import app.bumaza.sk.skodaavoc.utils.SoundRecorder;
import app.bumaza.sk.skodaavoc.utils.StreamSpeechFetcher;
import app.bumaza.sk.skodaavoc.utils.UploadSpeechFetcher;

public class StreamActivity extends AppCompatActivity implements AsyncTaskCompleteListener<String> {

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


    private int sampleRate = 16000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    Context context;

    AsyncTaskCompleteListener<String> listener;
    BubbleTextView stream_tv;
    ImageButton stream_button;

    private SoundRecorder soundRecorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        setTitle("Dictate");

        context = getApplicationContext();
        listener = this;

        soundRecorder = new SoundRecorder("janko.wav");

        stream_tv = findViewById(R.id.text_stream);

        final Byte[] array = ArrayUtils.toObject(tobyteArray("daco"));

        stream_button = findViewById(R.id.stream_button);
        stream_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isRecording) {
                    stream_button.setImageDrawable(getResources().getDrawable(R.drawable.red_small));
                } else {
//                    byte[] buffer = new byte[minBufSize];
//                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
//                    recorder.startRecording();
//
//                    while(true) {
//                        //reading data from MIC into buffer
//                        minBufSize = recorder.read(buffer, 0, buffer.length);
//                        new StreamSpeechFetcher(context, listener).execute(ArrayUtils.toObject(buffer));
//
//                    }
                    stream_button.setImageDrawable(getResources().getDrawable(R.drawable.green_small));
                    for (int i = 0; i < array.length - 22051; i += 21900) {

                        Byte[] chunk = Arrays.copyOfRange(array, i, i + 22050);
                        new StreamSpeechFetcher(context, listener).execute(chunk);
                    }
                }
                isRecording = !isRecording;


            }
        });
    }


    @Override
    public void onTaskComplete(String result) {
        stream_tv.setText(result);
        stream_tv.setVisibility(View.VISIBLE);
        Log.d("CHANGE TEXT", "onTaskComplete: " + result);
    }

    private byte[] tobyteArray(String WAV_FILE){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(getApplicationContext().getResources().openRawResource(R.raw.fam2));

            int read = 100;
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
}
