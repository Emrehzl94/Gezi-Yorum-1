package com.example.murat.gezi_yorum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.murat.gezi_yorum.fragments.Home;
import com.example.murat.gezi_yorum.fragments.Search;
import com.example.murat.gezi_yorum.fragments.TimeLine;
import com.example.murat.gezi_yorum.helpers.LocationDbOpenHelper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TRIP_STATE = "tripState";
    private static final boolean TRIP_STARTED = true;
    private static final boolean TRIP_NOT_STARTED = false;
    private static final String STARTED_TRIP_ID= "tripId";
    private static final long ENDED = 0;
    private SharedPreferences sharedPreferences;
    private FloatingActionButton fab;
    private SharedPreferences.Editor editor;
    private LocationDbOpenHelper helper;
    private View.OnClickListener startTrip;
    private View.OnClickListener stopTrip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        helper = new LocationDbOpenHelper(this);
        editor = sharedPreferences.edit();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        startTrip = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Trip started", Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this,LocationSaveService.class);
                startService(intent);
                long insert_id = helper.insertStartEntry();
                editor.putLong(STARTED_TRIP_ID,insert_id);
                editor.putBoolean(TRIP_STATE,TRIP_STARTED);
                editor.apply();
                fab.setImageResource(android.R.drawable.btn_dialog);
                fab.setOnClickListener(stopTrip);
            }
        };
        stopTrip = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Trip stopped", Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this,LocationSaveService.class);
                stopService(intent);
                long inserted_id = sharedPreferences.getLong(STARTED_TRIP_ID,0);
                helper.updateTripFinish(inserted_id);
                editor.putLong(STARTED_TRIP_ID,0);
                editor.putBoolean(TRIP_STATE,TRIP_NOT_STARTED);
                editor.apply();
                fab.setImageResource(android.R.drawable.ic_dialog_map);
                fab.setOnClickListener(startTrip);
            }
        };
        if(!sharedPreferences.getBoolean(TRIP_STATE,false)){
            fab.setOnClickListener(startTrip);
            fab.setImageResource(android.R.drawable.ic_dialog_map);
        }else {
            fab.setImageResource(android.R.drawable.btn_dialog);
            fab.setOnClickListener(stopTrip);
        }
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

        if (id == R.id.nav_home) {
            fragment = new Home();
            // Handle the camera action
        } else if (id == R.id.nav_timeline) {
            fragment = new TimeLine();
        } else if (id == R.id.nav_search) {
            fragment = new Search();
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

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

}
