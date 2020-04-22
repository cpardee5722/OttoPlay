package com.example.ottoplay.ui.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.ottoplay.R;
import com.example.ottoplay.WaypointMap.WaypointMapActivity;
import com.example.ottoplay.ui.home.HomeViewModel;

public class MapFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        //View root = inflater.inflate(R.layout.activity_waypoint_map, container, false);

        Intent intent = new Intent(getActivity(), WaypointMapActivity.class);
        startActivity(intent);
        //View root = inflater.inflate(R.layout.fragment_home, container, false);*/

        //return root;
        getActivity().onBackPressed();
        return null;
    }
}
