package com.example.murat.gezi_yorum.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.example.murat.gezi_yorum.Fragments.Notifications;
import com.example.murat.gezi_yorum.Fragments.TripControllers.ContinuingTrip;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Notifications ListViewAdapter
 */

public class NotificationsAdapter extends ArrayAdapter {
    private JSONArray notifications;
    private User user;
    private SharedPreferences preferences;
    private Handler handler;
    private int selectedId;
    private int type;
    private Notifications parentFragment;

    public static final int TRIP = 1;
    public static final int FRIENDSHIP = 2;
    public NotificationsAdapter(@NonNull Context context, JSONArray notifications, int type, Notifications parentFragment) {
        super(context, -1);
        this.notifications = notifications;
        preferences = getContext().getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE);
        user = new User(preferences);
        preferences = getContext().getSharedPreferences(Constants.PREFNAME + user.username, Context.MODE_PRIVATE);
        this.type = type;
        handler = new Handler();
        this.parentFragment = parentFragment;
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
            String text = "";
            if(type == TRIP) {
                JSONArray members = notification.getJSONArray("digerKatilimcilar");
                StringBuilder membersBuilder = new StringBuilder();
                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    membersBuilder.append(member.get("name"));
                    membersBuilder.append(" ");
                    membersBuilder.append(member.get("surname"));
                    membersBuilder.append("<br>");
                }
                text = "<b>" + getContext().getString(R.string.invitation) + "</b><br>" +
                        getContext().getString(R.string.invitee) + notification.getString("name") + " " + notification.getString("surname") + "<br>" +
                        getContext().getString(R.string.explain) + notification.getString("explanation") + "<br>" +
                        getContext().getString(R.string.members) + membersBuilder.toString();
            }else if(type == FRIENDSHIP){
                text = getContext().getString(R.string.friendship_request) +"<br>" +
                        notification.getString("name") + " " + notification.getString("surname");
            }
            TextView textMessage = view.findViewById(R.id.notification_text);
            textMessage.setText(Html.fromHtml(text));

            ImageButton acceptButton = view.findViewById(R.id.accept);
            acceptButton.setId(position);
            View.OnClickListener trip_acceptListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(preferences.getString(Trip.TRIPSTATE, "").equals(Trip.STARTED)) {
                        Snackbar.make(view, getContext().getString(R.string.active_trip_warning), Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    Snackbar.make(view, getContext().getString(R.string.trip_accepting), Snackbar.LENGTH_SHORT).show();
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
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), getContext().getString(R.string.error), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    return;
                                }
                                final JSONArray members = notification.getJSONArray("digerKatilimcilar");
                                JSONArray membersAsArray = new JSONArray();
                                membersAsArray.put(notification.get("username"));
                                for (int i = 0; i < members.length(); i++) {
                                    JSONObject member = members.getJSONObject(i);
                                    if(!member.getString("username").equals(user.username))
                                        membersAsArray.put(member.get("username"));
                                }
                                for (int i=0; i<membersAsArray.length(); i++){
                                    try {
                                        String member = membersAsArray.getString(i);
                                        URLRequestHandler handler = new URLRequestHandler(member, Constants.APP+"downloadProfilePhotoPath");
                                        handler.getResponseMessage();
                                        String link = handler.getResponse();
                                        String profilePicturePath = getContext().getFilesDir() + "/"+member+".jpg";
                                        try {
                                            URL website = new URL(Constants.ROOT+link);
                                            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                                            FileOutputStream fos = new FileOutputStream(profilePicturePath);
                                            fos.getChannel().transferFrom(rbc, 0, 5242880);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            };

            View.OnClickListener friendshipAcceptListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedId = view.getId();
                    Snackbar.make(view, R.string.friend_request_accepting, Snackbar.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject request = new JSONObject();
                            try {
                                request.put("token", user.token);
                                request.put("id", notifications.getJSONObject(selectedId).getLong("friendRequestId"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String url = Constants.APP + "acceptFriend"; // accept Friend

                            URLRequestHandler requestHandler = new URLRequestHandler(request.toString(), url);
                            if (!requestHandler.getResponseMessage() || !requestHandler.getResponse().equals("true")) {
                                //Hata
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), R.string.friend_accepted, Toast.LENGTH_SHORT).show();
                                    parentFragment.acceptFriendRequest(selectedId);
                                }
                            });
                        }
                    }).start();
                }
            };
            acceptButton.setOnClickListener(type == TRIP ? trip_acceptListener : friendshipAcceptListener);

            ImageButton denyButton = view.findViewById(R.id.deny);
            denyButton.setId(position);

            View.OnClickListener tripDenyListener = new View.OnClickListener() {
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
            };

            View.OnClickListener friendDenyListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedId = view.getId();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject request = new JSONObject();
                            try {
                                request.put("token", user.token);
                                request.put("id", notifications.getJSONObject(selectedId).getLong("friendRequestId"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String url = Constants.APP + "denyFriend"; // accept Friend

                            URLRequestHandler requestHandler = new URLRequestHandler(request.toString(), url);
                            if (!requestHandler.getResponseMessage() || !requestHandler.getResponse().equals("true")) {
                                //Hata
                                Toast.makeText(getContext(), getContext().getString(R.string.error), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            };

            denyButton.setOnClickListener(type == TRIP ? tripDenyListener : friendDenyListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}
