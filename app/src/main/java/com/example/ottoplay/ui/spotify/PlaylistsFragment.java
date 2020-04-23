package com.example.ottoplay.ui.spotify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ottoplay.JavaFiles.SpotifyAuthenticateActivity;
import com.example.ottoplay.WaypointMap.WaypointMapActivity;
import com.example.ottoplay.ui.home.HomeViewModel;

public class PlaylistsFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        //View root = inflater.inflate(R.layout.activity_waypoint_map, container, false);

        Intent intent = new Intent(getActivity(), SpotifyAuthenticateActivity.class);
        startActivity(intent);
        //View root = inflater.inflate(R.layout.fragment_home, container, false);*/

        //return root;
        getActivity().onBackPressed();
        return null;
    }
}
