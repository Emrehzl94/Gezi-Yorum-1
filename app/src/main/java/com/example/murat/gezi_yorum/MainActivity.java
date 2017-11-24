package com.example.murat.gezi_yorum;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.fragments.ContinuingTrip;
import com.example.murat.gezi_yorum.fragments.Home;
import com.example.murat.gezi_yorum.fragments.Search;
import com.example.murat.gezi_yorum.fragments.StartTripFragment;
import com.example.murat.gezi_yorum.fragments.TimeLine;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int MAP_PERMISSION_REQUEST = 1;
    private SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private Fragment currentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeFragment(new Home());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        int id = item.getItemId();
        switch (id){
            case(R.id.nav_home): {
                fragment = new Home();
                break;
                // Handle the camera action
            }case (R.id.nav_timeline): {
                fragment = new TimeLine();
                break;
            }case (R.id.nav_search): {
                fragment = new Search();
                break;
            }case (R.id.nav_trip):{
                if(Constants.ACTIVE.equals(preferences.getString(Constants.TRIPSTATE, Constants.PASSIVE))){
                    ContinuingTrip continuingTrip = new ContinuingTrip();
                    continuingTrip.setTrip_id(preferences.getLong(Constants.TRIPID,-1));
                    fragment = continuingTrip;
                }else {
                    fragment = new StartTripFragment();
                }
                break;
            }case (R.id.nav_settings): {

            }case  (R.id.nav_share) :{

            }case (R.id.nav_send): {

            }
        }

        if(fragment != null){
            changeFragment(fragment);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void changeFragment(Fragment fragment){
        currentFragment =fragment;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_main,currentFragment);
                fragmentTransaction.commit();
            }
        }).start();
    }
    public void showSnackbarMessage(String message, int length){
        Snackbar.make(getCurrentFocus(),message,length).show();
    }
    public void startTrip(){
        showSnackbarMessage("Trip started", Snackbar.LENGTH_LONG);
        long trip_id = (new LocationDbOpenHelper(this)).insertTripInfo(new Date().getTime(),Long.MAX_VALUE);
        editor.putLong(Constants.TRIPID,trip_id);
        editor.putString(Constants.TRIPSTATE,Constants.ACTIVE);
        editor.apply();
        startRecording(trip_id);
        ContinuingTrip trip = new ContinuingTrip();
        trip.setTrip_id(preferences.getLong(Constants.TRIPID,-1));
        changeFragment(trip);
    }
    public void endTrip(){
        showSnackbarMessage("Trip stopped", Snackbar.LENGTH_LONG);
        stopRecording();
        long trip_id = preferences.getLong(Constants.TRIPID,-1);
        new LocationDbOpenHelper(this).endTrip(trip_id,System.currentTimeMillis());
        editor.putLong(Constants.TRIPID,-1);
        editor.putString(Constants.TRIPSTATE,Constants.PASSIVE);
        editor.apply();
    }
    public void startRecording(long trip_id){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MAP_PERMISSION_REQUEST);
            return;
        }
        Intent intent = new Intent(getBaseContext(),LocationSaveService.class);
        intent.putExtra("trip_id",trip_id);

        startService(intent);
    }
    public void stopRecording(){
        Intent intent = new Intent(this,LocationSaveService.class);
        stopService(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MAP_PERMISSION_REQUEST:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startTrip();
                }else {
                    showSnackbarMessage("Gezi başlatılamadı.",Snackbar.LENGTH_LONG);
                }
                break;
        }
    }
}
