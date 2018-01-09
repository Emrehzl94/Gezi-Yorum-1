package com.example.murat.gezi_yorum.Utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.example.murat.gezi_yorum.Entity.Constants;
import com.example.murat.gezi_yorum.Entity.Trip;
import com.example.murat.gezi_yorum.Fragments.TripControllers.TripInfo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom adapter for viewPager
 */

public class TripPagerAdapter extends FragmentStatePagerAdapter{
    private HashMap<Integer,TripInfo> fragments;
    private ArrayList<Long> trip_ids;
    private int limit = -1;

    public TripPagerAdapter(FragmentManager fragmentManager, ArrayList<Long> trip_ids) {
        super(fragmentManager);
        this.trip_ids = trip_ids;
        fragments = new HashMap<>();
    }

    public void setDataset(ArrayList<Long> trip_ids){
        this.trip_ids = trip_ids;
        notifyDataSetChanged();
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
        info.putLong(Trip.TRIPID, trip_ids.get(position));
        info.putInt("position",position);
        info.putBoolean("islast", (getCount()-1) == position);
        info.putInt("limit", limit);
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

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }
}
