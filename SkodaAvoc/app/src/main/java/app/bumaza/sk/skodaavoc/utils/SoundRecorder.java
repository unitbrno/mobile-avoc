package app.bumaza.sk.skodaavoc.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Budy on 6.4.18.
 */

public class SoundRecorder {
    public MediaRecorder getmRecorder() {
        return mRecorder;
    }

    private MediaRecorder mRecorder;
    private MediaPlayer mediaPlayer;
    private double mEMA;



    private String subor = null;

    private boolean isRecord = false;

    private AudioRecord recorder;

    private int sampleRate = 16000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;


    public SoundRecorder(String nazovSuboru) {

        this.mRecorder = null;
        this.subor = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+nazovSuboru;


        mEMA = 0.0;
    }

    public void recordOnly()  {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(subor);


            try {
                mRecorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
            mEMA = 0.0;
        }
    }

    public void onlineStream(){

        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {

                byte[] buffer = new byte[minBufSize];
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                recorder.startRecording();

                while(status == true) {
                    //reading data from MIC into buffer
                    minBufSize = recorder.read(buffer, 0, buffer.length);
                    System.out.println(buffer.toString());
                    System.out.println(minBufSize);

                }
            }

        });
        streamThread.start();
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void play(){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(subor);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public byte[] getWavByteArray(){
        if(true){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(subor));
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

        }
        return null;
    }
    public double getTheAmplitude(){
        if(mRecorder != null)
            return (mRecorder.getMaxAmplitude());
        else
            return 1;
    }
    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude()/2700.0);
        else
            return 0;

    }

    public String getSubor() {
        return subor;
    }

    public void setSubor(String subor) {
        this.subor = subor;
    }
}
