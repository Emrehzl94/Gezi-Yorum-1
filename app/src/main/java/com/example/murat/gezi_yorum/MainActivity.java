package com.example.murat.gezi_yorum;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Fragments.Notifications;
import com.example.murat.gezi_yorum.Fragments.Search;
import com.example.murat.gezi_yorum.Fragments.TripControllers.ContinuingTrip;
import com.example.murat.gezi_yorum.Fragments.TripControllers.StartTripFragment;
import com.example.murat.gezi_yorum.Fragments.TripControllers.TimeLine;
import com.example.murat.gezi_yorum.Fragments.TripControllers.Trips;
import com.example.murat.gezi_yorum.Fragments.WebViewFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences preferences;
    private Fragment currentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton notification = findViewById(R.id.notifications);
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(new Notifications());
            }
        });

        preferences = getSharedPreferences(Constants.PREFNAME ,Context.MODE_PRIVATE);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        ImageView profilePhoto = header.findViewById(R.id.profilePhoto);
        String path = preferences.getString(Constants.PROFILEPHOTO,"");
        profilePhoto.setImageBitmap(BitmapFactory.decodeFile(preferences.getString(Constants.PROFILEPHOTO,"")));

        String isActive = preferences.getString(Constants.RECORDSTATE, Constants.PASSIVE);
        if(isActive.equals(Constants.ACTIVE)){
            changeFragment(new ContinuingTrip());
        }else {
            Fragment home = new WebViewFragment();
            Bundle extras = new Bundle();
            extras.putString(Constants.PAGE, Constants.HOME);
            home.setArguments(extras);
            changeFragment(home);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(currentFragment.getClass().equals(WebViewFragment.class) && ((WebViewFragment)currentFragment).goBack()){
                return;
            }
            finishAffinity();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        int id = item.getItemId();
        switch (id){
            case(R.id.nav_profile): {
                fragment = new WebViewFragment();
                Bundle extras = new Bundle();
                extras.putString(Constants.PAGE, Constants.PROFILE);
                fragment.setArguments(extras);
                break;
            }
            case(R.id.nav_home): {
                fragment = new WebViewFragment();
                Bundle extras = new Bundle();
                extras.putString(Constants.PAGE, Constants.HOME);
                fragment.setArguments(extras);
                break;
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
