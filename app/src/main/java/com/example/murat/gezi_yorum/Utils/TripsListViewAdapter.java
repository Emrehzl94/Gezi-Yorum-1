package com.example.murat.gezi_yorum.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.R;

import java.util.ArrayList;

/**
 * Trips list view adapter
 */

public class TripsListViewAdapter extends ArrayAdapter {
    private ArrayList<Long> trip_ids;
    private LocationDbOpenHelper helper;
    public TripsListViewAdapter(@NonNull Context context) {
        super(context, -1);
        helper = new LocationDbOpenHelper(context);

        this.trip_ids = helper.getTripsIDs();
    }

    @Override
    public int getCount() {
        return trip_ids.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        TextView textView = new TextView(getContext());
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Trip info = helper.getTrip(trip_ids.get(position));
        String infoText = "<b>"+info.name + "</b><br>" +
                getContext().getString(R.string.start) + ": " + info.getStartdate()+"<br>"+
                getContext().getString(R.string.finish) + ": " + info.getFinishdate()+"<br>";
        textView.setText(Html.fromHtml(infoText));
        return textView;
    }
}
