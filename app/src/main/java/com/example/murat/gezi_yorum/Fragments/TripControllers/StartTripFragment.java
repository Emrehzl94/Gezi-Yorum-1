package com.example.murat.gezi_yorum.Fragments.TripControllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;
import com.example.murat.gezi_yorum.Utils.TripsAdapter;
import com.example.murat.gezi_yorum.Utils.URLRequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Trip configuration before start
 */

public class StartTripFragment extends Fragment{

    private EditText trip_name_edit;
    private Spinner friends;
    private ArrayList<String> friendsList;
    private ArrayList<String> selectedFriends;
    private ListView selecteds;
    private Spinner choose_path;
    private ArrayList<Trip> importedTrips;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_trip_fragment, container, false);

        trip_name_edit = view.findViewById(R.id.trip_name);

        friends = view.findViewById(R.id.friends_list);
        selecteds = view.findViewById(R.id.selected_friends);
        choose_path = view.findViewById(R.id.choose_path);

        LocationDbOpenHelper helper = new LocationDbOpenHelper(getContext());
        importedTrips = helper.getImportedTrips();
        if(importedTrips.size() != 0){
            choose_path.setAdapter(new TripsAdapter(getContext(), true));
        }else {
            choose_path.setVisibility(View.INVISIBLE);
        }

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.PREFNAME ,Context.MODE_PRIVATE);
        String token = preferences.getString(Constants.TOKEN, "");
        String uname = preferences.getString(Constants.USERNAME,"");

        new getUserFriendList(token, uname).execute();

        FloatingActionButton fab = view.findViewById(R.id.start);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContinuingTrip continuingTrip = new ContinuingTrip();
                Bundle extras = new Bundle();
                extras.putString(Constants.MESSAGE,Constants.STARTNEWTRIP);
                extras.putString(Constants.TRIPNAME, trip_name_edit.getText().toString());
                continuingTrip.setArguments(extras);
                MainActivity parentActivity = (MainActivity) getActivity();
                parentActivity.changeFragment(continuingTrip);
            }
        });
        fab.setBackgroundColor(Color.GREEN);
        return view;
    }

    class getUserFriendList extends AsyncTask<Void, Void, Boolean> {

        private final String uname;
        private final String token;

        getUserFriendList(String token, String uname) {
            this.uname = uname;
            this.token = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            friendsList = new ArrayList<>();

            URLRequestHandler handler = new URLRequestHandler(
                    "{\"token\" : \""+token+"\",\"username\" : \""+uname+"\"}",
                    "http://163.172.176.169:8080/Geziyorum/getFriendsList");
            handler.getResponseMessage();
            try {
                JSONArray friendsJson = new JSONArray(handler.getResponse());
                for (int i = 0; i < friendsJson.length(); i++){
                    JSONObject friend = friendsJson.optJSONObject(i);
                    friendsList.add(friend.getString("username"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(!handler.getResponseMessage()){
                //wrong user name or token
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,friendsList);
                friends.setAdapter(adapter);
                selectedFriends = new ArrayList<>();
                friends.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedFriends.add(friendsList.get(i));
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,selectedFriends);
                        selecteds.setAdapter(adapter);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                selecteds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedFriends.remove(i);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,selectedFriends);
                        selecteds.setAdapter(adapter);
                    }
                });
            } else {
                friends.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
