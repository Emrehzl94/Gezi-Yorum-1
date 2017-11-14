package com.example.murat.gezi_yorum.helpers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.murat.gezi_yorum.fragments.TripInfo;

/**
 * Custom adapter for viewPager
 */

public class TripPagerAdapter extends FragmentPagerAdapter{

    private int NUM_ITEMS;
    private TripInfo currentFragment;

    public void setCount(int NUM_ITEMS) {
        this.NUM_ITEMS = NUM_ITEMS;
    }

    public TripPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        currentFragment = new TripInfo();
        Bundle info = new Bundle();
        info.putInt("position", position);
        currentFragment.setArguments(info);
        return currentFragment;
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }
    public TripInfo getCurrentFragment(){return currentFragment;}
}
