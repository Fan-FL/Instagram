package com.group10.comp90018.instagramviewer.Main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.group10.comp90018.instagramviewer.R;

public class MessagesFragment extends Fragment {
    private static final String TAG = "MessagesFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment_messages,container,false);
        return view;
    }


}
