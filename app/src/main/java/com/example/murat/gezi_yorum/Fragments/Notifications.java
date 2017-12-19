package com.example.murat.gezi_yorum.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.NotificationsAdapter;
import com.example.murat.gezi_yorum.Utils.URLRequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Shows notification
 */

public class Notifications extends Fragment {
    private ListView notifications;
    private Handler handler;
    private JSONArray notificationsList;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.notification));
        notifications = view.findViewById(R.id.notifications);
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                JSONObject trip = new JSONObject();
                User user = new User(getContext().getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE));
                try {
                    trip.put("token", user.token);
                    trip.put("username", user.username);
                    String url = Constants.APP + "checkTripRequest";
                    URLRequestHandler urlhandler = new URLRequestHandler(trip.toString(), url);
                    if(!urlhandler.getResponseMessage()){
                        return;
                    }
                    String notitificationsResponse = urlhandler.getResponse();
                    notificationsList = new JSONArray(notitificationsResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifications.setAdapter(new NotificationsAdapter(getContext(), notificationsList));
                    }
                });
            }
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notifications_fragment, container,false);
    }
}
