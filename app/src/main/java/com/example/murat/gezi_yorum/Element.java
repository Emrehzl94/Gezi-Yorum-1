package com.example.murat.gezi_yorum;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by murat on 24.10.2017.
 */

public class Element extends Fragment {
    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static Element newInstance(int page, String title) {
        Element fragmentFirst = new Element();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.element_fragment, container, false);
        TextView tvLabel = (TextView) view.findViewById(R.id.trytext);
        tvLabel.setText(page + " -- " + title);

        return view;
    }
}
