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

public class TripsAdapter extends ArrayAdapter {
    private ArrayList<Trip> trips;
    private LocationDbOpenHelper helper;
    public TripsAdapter(@NonNull Context context, boolean is_imported) {
        super(context, android.R.layout.simple_spinner_dropdown_item);
        helper = new LocationDbOpenHelper(context);

        if(is_imported){
            this.trips = helper.getImportedTrips();
        }else {
            this.trips = helper.getTrips();
        }

    }

    @Override
    public int getCount() {
        return trips.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        TextView textView = new TextView(getContext());
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Trip info = trips.get(position);
        String infoText = "<b>"+info.name + "</b><br>" +
                getContext().getString(R.string.start) + ": " + info.getStartdate()+"<br>"+
                getContext().getString(R.string.finish) + ": " + info.getFinishdate()+"<br>";
        textView.setText(Html.fromHtml(infoText));
        return textView;
    }
}
