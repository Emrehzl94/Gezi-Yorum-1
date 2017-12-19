package com.example.murat.gezi_yorum.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.Fragments.TripControllers.ContinuingTrip;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Notifications ListViewAdapter
 */

public class NotificationsAdapter extends ArrayAdapter {
    private JSONArray notifications;
    private User user;
    private SharedPreferences preferences;
    private Handler handler;
    private int selectedId;

    public NotificationsAdapter(@NonNull Context context, JSONArray notifications) {
        super(context, -1);
        this.notifications = notifications;
        preferences = getContext().getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE);
        user = new User(preferences);
        handler = new Handler();
    }
    @Override
    public int getCount() {
        return notifications.length();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.notification_template_layout, null);
        try {
            JSONObject notification = notifications.getJSONObject(position);
            JSONArray members = notification.getJSONArray("digerKatilimcilar");
            StringBuilder membersBuilder = new StringBuilder();
            for(int i = 0; i<members.length(); i++){
                JSONObject member = members.getJSONObject(i);
                membersBuilder.append(member.get("name"));
                membersBuilder.append(" ");
                membersBuilder.append(member.get("surname"));
                membersBuilder.append("<br>");
            }
            String text = "<b>"+getContext().getString(R.string.invitation)+"</b><br>"+
                getContext().getString(R.string.invitee) + notification.getString("name")+" " +notification.getString("surname")+ "<br>"+
                getContext().getString(R.string.explain) + notification.getString("explanation") + "<br>" +
                getContext().getString(R.string.members) + membersBuilder.toString();
            TextView textMessage = view.findViewById(R.id.notification_text);
            textMessage.setText(Html.fromHtml(text));

            ImageButton acceptButton = view.findViewById(R.id.accept);
            acceptButton.setId(position);

            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(preferences.getString(Trip.TRIPSTATE, "").equals(Trip.STARTED)) {
                        Toast.makeText(getContext(), getContext().getString(R.string.active_trip_warning), Toast.LENGTH_LONG).show();
                        return;
                    }
                    selectedId = view.getId();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final JSONObject notification = notifications.getJSONObject(selectedId);
                                JSONObject trip = new JSONObject();
                                trip.put("token", user.token);
                                trip.put("id", notification.get("id"));

                                String url = Constants.APP + "acceptTripRequest"; // for deny denyTripRequest

                                URLRequestHandler requestHandler = new URLRequestHandler(trip.toString(), url);
                                if (!requestHandler.getResponseMessage() || !requestHandler.getResponse().equals("true")) {
                                    //Hata
                                    return;
                                }
                                final JSONArray members = notification.getJSONArray("digerKatilimcilar");
                                JSONArray membersAsArray = new JSONArray();
                                membersAsArray.put(notification.get("username"));
                                for (int i = 0; i < members.length(); i++) {
                                    JSONObject member = members.getJSONObject(i);
                                    membersAsArray.put(member.get("username"));
                                }
                                final ContinuingTrip continuingTrip = new ContinuingTrip();
                                Bundle extras = new Bundle();
                                extras.putString(Constants.MESSAGE, Constants.STARTNEWTRIP);
                                extras.putString(Trip.TRIPNAME, notification.getString("explanation"));
                                extras.putString(Trip.MEMBERS, members.toString());
                                extras.putBoolean(Trip.CREATOR, false);
                                extras.putLong(Constants.TRIPIDONSERVER, notification.getLong("tripId"));
                                continuingTrip.setArguments(extras);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MainActivity) getContext()).changeFragment(continuingTrip);
                                    }
                                });

                               // Toast.makeText(getContext(), getContext().getString(R.string.accept) + selectedId, Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });

            ImageButton denyButton = view.findViewById(R.id.deny);
            denyButton.setId(position);

            denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedId = view.getId();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(preferences.getString(Trip.TRIPSTATE, "").equals(Trip.ACTIVE)) return;
                                JSONObject notification = notifications.getJSONObject(selectedId);
                                JSONObject trip = new JSONObject();
                                trip.put("token", user.token);
                                trip.put("id", notification.get("id"));
                                String url = Constants.APP+"denyTripRequest";
                                URLRequestHandler handler = new URLRequestHandler(trip.toString(), url);
                                if(!handler.getResponseMessage() || !handler.getResponse().equals("true")) {
                                    //Hata
                                    return;
                                }
                                //Toast.makeText(getContext(), getContext().getString(R.string.deny) + selectedId, Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}
