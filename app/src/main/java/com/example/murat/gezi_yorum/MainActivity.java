package com.example.murat.gezi_yorum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.Path;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Entity.User;
import com.example.murat.gezi_yorum.Fragments.Notifications;
import com.example.murat.gezi_yorum.Fragments.TripControllers.ContinuingTrip;
import com.example.murat.gezi_yorum.Fragments.TripControllers.StartTripFragment;
import com.example.murat.gezi_yorum.Fragments.TripControllers.TimeLine;
import com.example.murat.gezi_yorum.Fragments.TripControllers.Trips;
import com.example.murat.gezi_yorum.Fragments.WebViewFragment;
import com.example.murat.gezi_yorum.Fragments.WelcomeScreen;
import com.example.murat.gezi_yorum.Utils.LocationDbOpenHelper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences preferences;
    private Fragment currentFragment;
    private NavigationView navigationView;
    private User user;
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeFragment(new WelcomeScreen());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUser();
    }

    public void loadUser(){
        preferences = getSharedPreferences(Constants.PREFNAME ,Context.MODE_PRIVATE);
        user = new User(preferences);
        preferences = getSharedPreferences(Constants.PREFNAME + user.username ,Context.MODE_PRIVATE);
        View header = navigationView.getHeaderView(0);
        ImageView profilePhoto = header.findViewById(R.id.profilePhoto);
        profilePhoto.setImageBitmap(ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(user.profilePicturePath), 200, 200
        ));
        TextView name_surname = header.findViewById(R.id.name_surname);
        name_surname.setText(user.name + " " +user.surname);
        TextView username = header.findViewById(R.id.username);
        username.setText("@"+user.username);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(currentFragment.getClass().equals(WebViewFragment.class) && ((WebViewFragment)currentFragment).goBack()){
            return;
        } else {
            //if there is no fragment on backstack then go finish
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.quit_sure));
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                    }
                });
                builder.create();
                builder.show();
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return itemSelected(id);
    }

    public boolean itemSelected(int id){
        Fragment fragment = null;
        switch (id){
            case(R.id.nav_return_start): {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                changeFragment(new WelcomeScreen());
                return true;
            }
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
            } case (R.id.nav_downloads): {
                fragment = new TimeLine();
                Bundle extras = new Bundle();
                extras.putBoolean("isImported", true);
                fragment.setArguments(extras);
                break;
            }
            case (R.id.nav_search): {
                fragment = new WebViewFragment();
                Bundle extras = new Bundle();
                extras.putString(Constants.PAGE, Constants.SEARCH);
                fragment.setArguments(extras);
                break;
            }case (R.id.nav_trip):{
                if(Trip.STARTED.equals(preferences.getString(Trip.TRIPSTATE, Trip.ENDED))){
                    fragment = new ContinuingTrip();
                }else {
                    fragment = new StartTripFragment();
                }
                break;
            }case (R.id.nav_settings): {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }case (R.id.nav_log_out): {
                setResult(Activity.RESULT_CANCELED);
                Intent intent = new Intent(this,LocationSaveService.class);
                Long path_id = preferences.getLong(Path.PATH_ID, -1);
                stopService(intent);
                LocationDbOpenHelper helper = new LocationDbOpenHelper(this);
                helper.endPath(path_id);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(Path.PATH_ID,-1);
                editor.putString(Trip.RECORDSTATE,Trip.PASSIVE);
                editor.apply();
                finish();
                break;
            }case (R.id.nav_edit_profile): {
                Intent intent = new Intent(this, EditProfile.class);
                startActivity(intent);
                break;
            }
        }

        if(fragment != null){
            changeFragment(fragment);
        }
        return true;
    }

    public void changeFragment(Fragment fragment){
        currentFragment =fragment;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main,currentFragment)
                        .addToBackStack(currentFragment.getClass().toString())
                        .commit();
            }
        }).start();
    }
    public void showSnackbarMessage(String message, int length){
        Snackbar.make(getCurrentFocus(),message,length).show();
    }
}
