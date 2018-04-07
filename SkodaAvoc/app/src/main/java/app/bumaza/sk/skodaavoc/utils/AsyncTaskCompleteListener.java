package app.bumaza.sk.skodaavoc.utils;

/**
 * Created by janko on 4/6/18.
 */

public interface AsyncTaskCompleteListener<T> {
    void onTaskComplete(T result);
}
