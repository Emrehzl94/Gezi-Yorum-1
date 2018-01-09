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
    private ListView trip_invitation_notifications;
    private ListView friendship_requests;
    private Handler handler;
    private JSONArray trip_invitation_notificationsList;
    private JSONArray friendship_requestsList;
    private User user;

    private Boolean isPaused;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.notification));
        trip_invitation_notifications = view.findViewById(R.id.notifications);
        friendship_requests = view.findViewById(R.id.friend_notifications);
        handler = new Handler();
        user = new User(getContext().getSharedPreferences(Constants.PREFNAME, Context.MODE_PRIVATE));
        new Thread(new Runnable() {
            @Override
            public void run() {

                JSONObject request = new JSONObject();
                try {
                    request.put("token", user.token);
                    request.put("username", user.username);
                    String url = Constants.APP + "checkTripRequest";
                    URLRequestHandler urlhandler = new URLRequestHandler(request.toString(), url);
                    if(urlhandler.getResponseMessage()){
                        String notitificationsResponse = urlhandler.getResponse();
                        trip_invitation_notificationsList = new JSONArray(notitificationsResponse);
                    }
                    url = Constants.APP + "getFriendRequests";
                    urlhandler = new URLRequestHandler(request.toString(), url);
                    if(urlhandler.getResponseMessage()){
                        String notitificationsResponse = urlhandler.getResponse();
                        friendship_requestsList = new JSONArray(notitificationsResponse);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(!isPaused)
                            loadAdapter();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    public void loadAdapter(){
        if(trip_invitation_notificationsList != null && trip_invitation_notificationsList.length() > 0) {
            trip_invitation_notifications.setAdapter(
                    new NotificationsAdapter(getContext(), trip_invitation_notificationsList, NotificationsAdapter.TRIP, this)
            );
        }else {
            getView().findViewById(R.id.trip_not_text).setVisibility(View.GONE);
        }
        if(friendship_requestsList != null && friendship_requestsList.length() > 0) {
            friendship_requests.setAdapter(
                    new NotificationsAdapter(getContext(), friendship_requestsList, NotificationsAdapter.FRIENDSHIP,this)
            );
        }else {
            getView().findViewById(R.id.friend_not_text).setVisibility(View.GONE);
        }
        if((trip_invitation_notificationsList == null || trip_invitation_notificationsList.length() == 0)
                && (friendship_requestsList == null || friendship_requestsList.length() == 0)) {
            getActivity().findViewById(R.id.nothing).setVisibility(View.VISIBLE);
        }
    }
    public void acceptOrDenyFriendRequest(int i){
        friendship_requestsList.remove(i);
        friendship_requests.setAdapter(
                new NotificationsAdapter(getContext(), friendship_requestsList, NotificationsAdapter.FRIENDSHIP,this)
        );
    }
    public void denyTripRequest(int i){
        trip_invitation_notificationsList.remove(i);
        trip_invitation_notifications.setAdapter(
                new NotificationsAdapter(getContext(), trip_invitation_notificationsList, NotificationsAdapter.TRIP,this)
        );
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notifications_fragment, container,false);
    }
}
