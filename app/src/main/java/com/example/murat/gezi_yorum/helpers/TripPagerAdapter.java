package com.example.murat.gezi_yorum.helpers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.murat.gezi_yorum.fragments.Element;

/**
 * Custom adapter for viewPager
 */

public class TripPagerAdapter extends FragmentPagerAdapter{

    private int NUM_ITEMS = 3;

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
        return Element.newInstance(position, "Page # "+position);
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }
}
