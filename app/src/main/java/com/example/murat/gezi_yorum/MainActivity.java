package com.example.murat.gezi_yorum;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Fragments.Home;
import com.example.murat.gezi_yorum.Fragments.Search;
import com.example.murat.gezi_yorum.Fragments.TripControllers.ContinuingTrip;
import com.example.murat.gezi_yorum.Fragments.TripControllers.StartTripFragment;
import com.example.murat.gezi_yorum.Fragments.TripControllers.TimeLine;
import com.example.murat.gezi_yorum.Fragments.TripControllers.Trips;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences preferences;
    private Fragment currentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getPreferences(Context.MODE_PRIVATE);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        String isActive = preferences.getString(Constants.RECORDSTATE, Constants.PASSIVE);
        if(isActive.equals(Constants.ACTIVE)){
            changeFragment(new ContinuingTrip());
        }else {
            changeFragment(new Home());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finishAffinity();
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
            }case (R.id.nav_trips): {
                fragment = new Trips();
                break;
            }
            case (R.id.nav_timeline): {
                fragment = new TimeLine();
                break;
            }case (R.id.nav_search): {
                fragment = new Search();
                break;
            }case (R.id.nav_trip):{
                if(Constants.STARTED.equals(preferences.getString(Constants.TRIPSTATE, Constants.ENDED))){
                    fragment = new ContinuingTrip();
                }else {
                    fragment = new StartTripFragment();
                }
                break;
            }case (R.id.nav_settings): {
                break;
            }case (R.id.nav_log_out): {
                setResult(Activity.RESULT_CANCELED);
                finish();
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
}
