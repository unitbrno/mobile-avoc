package app.bumaza.sk.skodaavoc.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import app.bumaza.sk.skodaavoc.BuildConfig;

/**
 * Created by janko on 4/6/18.
 */

public class StreamSpeechFetcher extends AsyncTask<Byte[], Void, String> {

    private static final String SPE_address  = "http://77.240.177.148:8602";
    private byte[] audioData;
    private Context context;
    private AsyncTaskCompleteListener<String> listener;
    String credentials = BuildConfig.CREDENTIALS;
    private String base64 = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);

    public static String stream_id = null;
    private static String task_id = null;

    public StreamSpeechFetcher(Context context, AsyncTaskCompleteListener<String> listener){
        this.context = context;
        this.listener = listener;
    }


    @Override
    protected String doInBackground(Byte[]... audioFile) {
        if(! isOnline()){
            return null;
        }

        audioData = ArrayUtils.toPrimitive(audioFile[0]);


        if( stream_id == null && task_id == null){
            stream_id = setup_stream(48000);
            task_id = attach_stream(stream_id);
        }

        send_chunk(stream_id, audioData);
        SystemClock.sleep(250);
        String response = get_results(task_id);
        String speech_text = null;
        try {
            speech_text = parse_response(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return speech_text;
    }


    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if(response == null){
            response = "ERROR GETTING DATA";
        }

        listener.onTaskComplete(response);
        System.out.println(response);
    }

    private String attach_stream(String stream_id){
        String result = "";
        String task_id = "";
        HttpURLConnection connection;
        try {
            URL url = new URL(SPE_address + "/technologies/dictate?stream=" + stream_id + "&model=ENGLISH");
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Authorization", "Basic " + base64);
            connection.setRequestMethod("POST");

            int code = connection.getResponseCode();
            Log.d("ATTACH", Integer.toString(code));
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()
                    ));
            result = "";
            for (String line; (line = in.readLine()) != null; result += line) ;
            JSONObject jsonObject = new JSONObject(result);
            JSONObject infoObject = jsonObject.getJSONObject("result").getJSONObject("stream_task_info");
            task_id = infoObject.optString("id");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return task_id;
    }
    private void send_chunk(String task_id, byte[] chunk){
        String result = "";

        HttpURLConnection connection;
        try {
            URL url = new URL(SPE_address + "/stream/http?stream=" + task_id);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty ("Authorization", "Basic " + base64);
            connection.setRequestProperty("Content-type", "audio/wav");
            OutputStream os = connection.getOutputStream();
            os.write(chunk);
            os.close();

            int code = connection.getResponseCode();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()
                    ));

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    private String setup_stream(int frequency){
        String request_id = "";
        HttpURLConnection newConnection;
        String result = "";
        try {
            URL url = new URL(SPE_address+ "/stream/http?frequency=" + Integer.toString(frequency));
            newConnection = (HttpURLConnection) url.openConnection();

            newConnection.setRequestProperty ("Authorization", "Basic " + base64);
            newConnection.setRequestMethod("POST");
            newConnection.setInstanceFollowRedirects(false);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            newConnection.getInputStream()
                    ));
            for(String line; (line = in.readLine()) != null; result += line);

            JSONObject jsonObject = new JSONObject(result);
            JSONObject infoObject = jsonObject.getJSONObject("result");
            request_id = infoObject.optString("stream");

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  request_id;
    }

    private String get_results(String task_id){
        String result = "";
        HttpURLConnection connection;
        try {
            URL url = new URL(SPE_address + "/technologies/dictate?task=" + task_id);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Authorization", "Basic " + base64);
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()
                    ));
            result = "";
            for (String line; (line = in.readLine()) != null; result += line) ;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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
