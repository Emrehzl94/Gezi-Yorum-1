package com.example.murat.gezi_yorum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import com.example.murat.gezi_yorum.classes.Constants;
import com.example.murat.gezi_yorum.fragments.Home;
import com.example.murat.gezi_yorum.fragments.Search;
import com.example.murat.gezi_yorum.fragments.StartTripFragment;
import com.example.murat.gezi_yorum.fragments.TimeLine;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences preferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeFragment(new Home());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
    public boolean onNavigationItemSelected(MenuItem item) {
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
                if(preferences.getString(Constants.TRIPSTATE,Constants.PASSIVE).equals(Constants.ACTIVE)){
                    ContinuingTrip continuingTrip = new ContinuingTrip();
                    continuingTrip.setStartDate(preferences.getLong(Constants.STARTDATE,Long.MAX_VALUE));
                    fragment = continuingTrip;
                }else {
                    fragment = new StartTripFragment();
                }
                break;
            }
            case (R.id.nav_settings): {

            }case  (R.id.nav_share) :{

            }case (R.id.nav_send): {

            }
        }

        if(fragment != null){
            changeFragment(fragment);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void changeFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_main,fragment);
        fragmentTransaction.commit();
    }
    public void showSnackbarMessage(String message, int lenght){
        Snackbar.make(getCurrentFocus(),message,lenght).show();
    }
    public void startTrip(){
        Snackbar.make(getCurrentFocus(), "Trip started", Snackbar.LENGTH_LONG).show();
        startRecording();
        editor.putLong(Constants.STARTDATE, new Date().getTime());
        editor.putString(Constants.TRIPSTATE,Constants.ACTIVE);
        editor.apply();
    }
    public void endTrip(){
        Snackbar.make(getCurrentFocus(), "Trip stopped", Snackbar.LENGTH_LONG).show();
        stopRecording();
        long starttime = preferences.getLong(Constants.STARTDATE,0);
        long insert_id = (new LocationDbOpenHelper(this)).insertTripInfo(starttime,new Date().getTime());
        editor.putString(Constants.TRIPSTATE,Constants.PASSIVE);
        editor.apply();
    }
    public void startRecording(){
        Intent intent = new Intent(this,LocationSaveService.class);
        startService(intent);
    }
    public void stopRecording(){
        Intent intent = new Intent(this,LocationSaveService.class);
        stopService(intent);
    }

}
