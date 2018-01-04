package com.example.murat.gezi_yorum.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murat.gezi_yorum.MainActivity;
import com.example.murat.gezi_yorum.R;

import java.util.ArrayList;

public class WelcomeScreen extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).itemSelected(view.getId());
            }
        };
        View view = inflater.inflate(R.layout.fragment_welcome_screen, container, false);
        ArrayList<View> buttons = view.getTouchables();
        for(View button : buttons){
            button.setOnClickListener(clickListener);
        }
        return view;

    }
}
