package com.example.murat.gezi_yorum.Fragments.TripControllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.URLRequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Trip configuration before start
 */

public class StartTripFragment extends Fragment{

    private EditText trip_name_edit;
    private Spinner friends;
    private ArrayList<String> friendsList;
    private ArrayList<String> selectedFriends;
    private ListView selecteds;
    private User user;
    private Handler handler;


    private Spinner choose_path;
    private CheckBox use_selected_trip;
    private long choosen_trip_id;
    private ArrayList<Trip> importedTrips;

    private SharedPreferences preferences;
    private Boolean isListed = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_trip_fragment, container, false);
        preferences = getActivity().getSharedPreferences(Constants.PREFNAME ,Context.MODE_PRIVATE);
        user = new User(preferences);
        preferences = getActivity().getSharedPreferences(Constants.PREFNAME + user.username ,Context.MODE_PRIVATE);
        handler = new Handler();
        if(preferences.getString(Trip.TRIPSTATE, Trip.ENDED).equals(Trip.STARTED)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().onBackPressed();
                        }
                    });
                }
            }).start();
            return view;
        }
        getActivity().setTitle(R.string.title_activity_start_trip);
        trip_name_edit = view.findViewById(R.id.trip_name);

        friends = view.findViewById(R.id.friends_list);
        selecteds = view.findViewById(R.id.selected_friends);
        choose_path = view.findViewById(R.id.choose_path);

        selectedFriends = new ArrayList<>();

        use_selected_trip = view.findViewById(R.id.use_choosen_trip);
        LocationDbOpenHelper helper = new LocationDbOpenHelper(getContext());
        importedTrips = helper.getImportedTrips(user.username);
        if(importedTrips.size() != 0){
            List<Spanned> texts = new ArrayList<>();
            for (Trip trip : importedTrips){
                String infoText = "<b>"+getContext().getString(R.string.trip_name)+trip.name + "</b><br>" +
                        getContext().getString(R.string.start) + ": " + trip.getStartdate()+"<br>"+
                        getContext().getString(R.string.finish) + ": " + trip.getFinishdate()+"<br>";
                texts.add(Html.fromHtml(infoText));
            }
            choose_path.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, texts));
            choose_path.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    choosen_trip_id = importedTrips.get(i).id;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }else {
            choose_path.setVisibility(View.GONE);
            use_selected_trip.setVisibility(View.GONE);
        }

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        //noinspection ConstantConditions
        if(cm.getActiveNetworkInfo() == null){
            Toast.makeText(getContext(),
                    getString(R.string.friend_list_unsuccessful) +" " + getString(R.string.internet_warning),
                    Toast.LENGTH_LONG).show();
        }else {
            new getUserFriendList(user.token, user.username).execute();
        }

        FloatingActionButton fab = view.findViewById(R.id.start);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String trip_name = trip_name_edit.getText().toString();
                        Long tripIdOnserver = -1L;

                        boolean isPersonalTrip = true;
                        JSONArray members = new JSONArray();
                        members.put(user.username);
                        for (String friend : selectedFriends){
                            members.put(friend);
                            isPersonalTrip = false;
                        }
                        if(!isPersonalTrip){
                            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                            //noinspection ConstantConditions
                            if(cm.getActiveNetworkInfo() == null){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MainActivity) getActivity()).showSnackbarMessage(
                                                getString(R.string.teamTripUnsuccesful) + " " + getString(R.string.internet_warning),
                                                Snackbar.LENGTH_LONG);
                                    }
                                });
                                return;
                            }
                            JSONObject trip_info = new JSONObject();
                            try {
                                trip_info.put("token",user.token);
                                trip_info.put("tripAciklama", trip_name);
                                trip_info.put("arkadaslaraAciklama", trip_name);
                                trip_info.put("usernames", members);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String url = Constants.APP + "createTripDemand";
                            URLRequestHandler handler = new URLRequestHandler(trip_info.toString(), url);
                            if(!handler.getResponseMessage()){
                                return;
                            }
                            tripIdOnserver = Long.parseLong(handler.getResponse());
                            for (int i=0; i<members.length(); i++){
                                try {
                                    String member = members.getString(i);
                                    if(!member.equals(user.username)) {
                                        handler = new URLRequestHandler(member, Constants.APP + "downloadProfilePhotoPath");
                                        handler.getResponseMessage();
                                        String link = handler.getResponse();
                                        String profilePicturePath = getContext().getFilesDir() + "/" + member + ".jpg";
                                        try {
                                            URL website = new URL(Constants.ROOT + link);
                                            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                                            FileOutputStream fos = new FileOutputStream(profilePicturePath);
                                            fos.getChannel().transferFrom(rbc, 0, 5242880);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                        final ContinuingTrip continuingTrip = new ContinuingTrip();
                        Bundle extras = new Bundle();
                        extras.putString(Constants.MESSAGE,Constants.STARTNEWTRIP);
                        extras.putString(Trip.TRIPNAME, trip_name);
                        extras.putString(Trip.MEMBERS, members.toString());
                        extras.putLong(Constants.TRIPIDONSERVER, tripIdOnserver);

                        if(use_selected_trip.isChecked()){
                            extras.putLong(Constants.CHOSEN_TRIPID, choosen_trip_id);
                        }
                        continuingTrip.setArguments(extras);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity parentActivity = (MainActivity) getActivity();
                                parentActivity.changeFragment(continuingTrip);
                            }
                        });
                    }
                }).start();
            }
        });
        fab.setBackgroundColor(Color.GREEN);
        return view;
    }

    @SuppressLint("StaticFieldLeak")
    class getUserFriendList extends AsyncTask<Void, Void, Boolean> {

        private final String uname;
        private final String token;

        getUserFriendList(String token, String uname) {
            this.uname = uname;
            this.token = token;
        }

        /**
         * false -> wrong username or token
         * @param params ...
         * @return ...
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            friendsList = new ArrayList<>();

            URLRequestHandler handler = new URLRequestHandler(
                    "{\"token\" : \""+token+"\",\"username\" : \""+uname+"\"}",
                    Constants.APP+"getFriendsList");
            handler.getResponseMessage();
            try {
                JSONArray friendsJson = new JSONArray(handler.getResponse());
                friendsList.add(getString(R.string.choose));
                for (int i = 0; i < friendsJson.length(); i++){
                    JSONObject friend = friendsJson.optJSONObject(i);
                    friendsList.add(friend.getString("username"));
                }
            } catch (JSONException|NullPointerException e) {
                e.printStackTrace();
            }
            return handler.getResponseMessage() && !friendsList.isEmpty();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,friendsList);
                friends.setAdapter(adapter);
                friends.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if(isListed) {
                            if(i==0) return;
                            selectedFriends.add(friendsList.get(i));
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, selectedFriends);
                            selecteds.setAdapter(adapter);

                            friendsList.remove(i);
                            adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, friendsList);
                            friends.setAdapter(adapter);
                        }else {
                            isListed = true;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                selecteds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        friendsList.add(selectedFriends.get(i));
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,friendsList);
                        friends.setAdapter(adapter);

                        selectedFriends.remove(i);
                        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,selectedFriends);
                        selecteds.setAdapter(adapter);
                    }
                });
                getActivity().findViewById(R.id.add_friends).setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
