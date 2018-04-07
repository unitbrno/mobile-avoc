package app.bumaza.sk.skodaavoc.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;


import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import app.bumaza.sk.skodaavoc.BuildConfig;

/**
 * Created by janko on 4/6/18.
 */

public class UploadSpeechFetcher extends AsyncTask<Byte[], Void, String> {

    private static final String SPE_address  = "http://77.240.177.148:8602";
    private Byte[] audioData;
    private Context context;
    private AsyncTaskCompleteListener<String> listener;
    String credentials = BuildConfig.CREDENTIALS;
    private String base64 = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);



    public UploadSpeechFetcher(Context context, AsyncTaskCompleteListener<String> listener){
        this.context = context;
        this.listener = listener;
    }


    @Override
    protected String doInBackground(Byte[]... audioFile) {


        if(! isOnline()){
            return null;
        }
        audioData = audioFile[0];

        uploadAudioFile("/testfile1.wav");
        String request_id = process_recording("/testfile1.wav");
        String result = get_speech(request_id);

        String speech_text = "";
        try {
            speech_text = parse_response(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return speech_text;

    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if(response == null)
            response = "ERROR GETTING DATA";
        listener.onTaskComplete(response);
    }

    private String get_speech(String urlString){
        String result = "";
        HttpURLConnection connection;
        while( true) {
            try {
                URL url = new URL(SPE_address + "/pending/" +urlString);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("Authorization", "Basic " + base64);
                connection.setRequestMethod("GET");

                int code = connection.getResponseCode();
                if (code >= 300) {
                    break;
                }
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
                        ));
                result = "";
                for (String line; (line = in.readLine()) != null; result += line) ;
                Log.d("RESPONSE_SPEECH", result);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            SystemClock.sleep(500);
        }
        return result;
    }
    private void uploadAudioFile(String path){
        String result = "";

        HttpURLConnection connection;
        try {
            URL url = new URL(SPE_address + "/audiofile?path=" + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty ("Authorization", "Basic " + base64);
            connection.setRequestProperty("Content-type", "audio/wav");
            OutputStream os = connection.getOutputStream();
            byte[] byteArray = ArrayUtils.toPrimitive(audioData);
            os.write(byteArray);
            os.close();

            int code = connection.getResponseCode();
            Log.d("CODE1", Integer.toString(code));

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()
                    ));
            result = "";
            for (String line; (line = in.readLine()) != null; result += line) ;
            Log.d("RESPONSE1",result );

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    private String process_recording(String path){
        String request_id = "";
        HttpURLConnection newConnection;
        String result = "";
        try {
            URL url = new URL(SPE_address+ "/technologies/stt?path=" + path + "&model=ENGLISH");
            newConnection = (HttpURLConnection) url.openConnection();

            newConnection.setRequestProperty ("Authorization", "Basic " + base64);
            newConnection.setRequestMethod("GET");
            newConnection.setInstanceFollowRedirects(false);


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            newConnection.getInputStream()
                    ));
            for(String line; (line = in.readLine()) != null; result += line);

            Log.d("RESPONSE2",result );
            JSONObject jsonObject = new JSONObject(result);
            JSONObject infoObject = jsonObject.getJSONObject("result").getJSONObject("info");
            request_id = infoObject.optString("id");

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  request_id;
    }

    public String parse_response(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject result = jsonObject.getJSONObject("result").getJSONObject("one_best_result");
        JSONArray jsonArray = result.getJSONArray("segmentation");

        ArrayList<String> words = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject wordObject = jsonArray.getJSONObject(i);
            String word = wordObject.optString("word");
            words.add(word);
        }

        String result_text = "";
        for (String s : words)
        {
            if(s.contains("<")) continue;
            result_text += s + " ";
        }
        return result_text;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}