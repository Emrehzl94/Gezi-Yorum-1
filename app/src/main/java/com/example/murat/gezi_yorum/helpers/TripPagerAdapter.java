package com.example.murat.gezi_yorum.helpers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.murat.gezi_yorum.fragments.Element;

/**
 * Created by murat on 24.10.2017.
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
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return Element.newInstance(0, "Page # 1");
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return Element.newInstance(1, "Page # 2");
            case 2: // Fragment # 1 - This will show SecondFragment
                return Element.newInstance(2, "Page # 3");
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }
}
