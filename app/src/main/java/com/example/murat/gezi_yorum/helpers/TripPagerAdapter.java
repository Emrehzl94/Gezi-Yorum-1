package com.example.murat.gezi_yorum.helpers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.murat.gezi_yorum.fragments.TripInfo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom adapter for viewPager
 */

public class TripPagerAdapter extends FragmentPagerAdapter{
    private HashMap<Integer,TripInfo> fragments;
    private ArrayList<Integer> trip_ids;

    public TripPagerAdapter(FragmentManager fragmentManager, ArrayList<Integer> trip_ids) {
        super(fragmentManager);
        this.trip_ids = trip_ids;
        fragments = new HashMap<>();
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return trip_ids.size();
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        TripInfo fragment = new TripInfo();
        Bundle info = new Bundle();
        info.putInt("trip_id", trip_ids.get(position));
        info.putInt("position",position);
        fragment.setArguments(info);
        fragments.put(position,fragment);
        return fragment;
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }
    public TripInfo getFragment(int position){return fragments.get(position);}
}
