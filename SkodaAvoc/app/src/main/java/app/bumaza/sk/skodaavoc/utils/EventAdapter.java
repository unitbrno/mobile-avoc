package app.bumaza.sk.skodaavoc.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import app.bumaza.sk.skodaavoc.R;

/**
 * Created by Budy on 6.4.18.
 */

public class EventAdapter extends ArrayAdapter<EventItem> {

    public List<EventItem> eventItems;
    private Context context;


    public EventAdapter(Context context, List<EventItem> eventItems) {
        super(context, R.layout.event_item, eventItems);
        this.eventItems = eventItems;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItem = convertView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listItem = inflater.inflate(R.layout.event_item, null);

        EventItem currentEvent = eventItems.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.textViewHead);
        name.setText(currentEvent.getTitle());

        TextView release = (TextView) listItem.findViewById(R.id.textViewDesc);
        release.setText(currentEvent.getAttendances().size());

        return listItem;
    }

}
