package com.example.puzzle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.puzzle.NavBar.Leaderboard.LeaderboardFragment;
import com.example.puzzle.NavBar.ProfileFragment;

public class StartFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_start, container, false);

        Button startButton = rootView.findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            // Replace the StartFragment with the GameFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new GameFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        ImageButton profileButton = rootView.findViewById(R.id.profileBtn);
        profileButton.setOnClickListener(v -> {
            // Replace the StartFragment with the ProfileFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ProfileFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        ImageButton leaderboardButton = rootView.findViewById(R.id.leaderboardBtn);
        leaderboardButton.setOnClickListener(v -> {
            // Replace the StartFragment with the ProfileFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new LeaderboardFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        return rootView;
    }
}
