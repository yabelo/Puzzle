package com.example.puzzle.NavBar.Leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puzzle.NavBar.ProfileFragment;
import com.example.puzzle.R;
import com.example.puzzle.StartFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private RecyclerView leaderboardRecyclerView;
    private LeaderboardAdapter leaderboardAdapter;
    private List<LeaderboardPlayer> leaderboardDataList;
    private String modeText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        // Initialize the RecyclerView
        leaderboardRecyclerView = rootView.findViewById(R.id.leaderboardRecyclerView);

        // Set the layout manager and adapter for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        leaderboardRecyclerView.setLayoutManager(layoutManager);

        // Initialize the leaderboard data list

        leaderboardDataList = new ArrayList<>();
        fetchUserDataFromFirestore();

        // Create an instance of LeaderboardAdapter and set it to the RecyclerView
        leaderboardAdapter = new LeaderboardAdapter(leaderboardDataList);
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

        TextView leaderboardMoveTextView = rootView.findViewById(R.id.leaderboardMoveTextView);
        modeText = (LeaderboardAdapter.mode == LeaderboardMode.TIME) ? "Best times" : "Best moves";
        leaderboardMoveTextView.setText(leaderboardMoveTextView.getText().toString().replace("<mode>", modeText));

        Button bestMovesButton = rootView.findViewById(R.id.bestMovesButton);
        bestMovesButton.setOnClickListener(v -> {
            LeaderboardAdapter.mode = (LeaderboardAdapter.mode == LeaderboardMode.TIME) ? LeaderboardMode.MOVES : LeaderboardMode.TIME;

            modeText = (LeaderboardAdapter.mode == LeaderboardMode.TIME) ? "Best times" : "Best moves";
            String newText = getString(R.string.leaderboard_n_mode).replace("<mode>", modeText);

            leaderboardMoveTextView.setText(newText);
            fetchUserDataFromFirestore();
        });





        //Nav
        ImageButton homeButton = rootView.findViewById(R.id.homeBtn);
        homeButton.setOnClickListener(v -> {
            // Replace the StartFragment with the ProfileFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new StartFragment());
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

        return rootView;
    }

    private void fetchUserDataFromFirestore() {
        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("users");

        usersCollection
                .orderBy((LeaderboardAdapter.mode == LeaderboardMode.TIME) ? "bestTime" : "bestMoves")
                .limit(9) // Retrieve only the top 10 players
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<LeaderboardPlayer> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            String username = document.getString("username");
                            long bestTime = document.getLong("bestTime");
                            long bestMoves = document.getLong("bestMoves");

                            LeaderboardPlayer user = new LeaderboardPlayer(userId, username, bestTime, bestMoves);
                            userList.add(user);
                        }

                        // Sort the user list based on best time (put -1 values at the end)
                        userList.sort((user1, user2) -> {
                            long bestValue1 = (LeaderboardAdapter.mode == LeaderboardMode.TIME) ? user1.getBestTime() : user1.getBestMoves();
                            long bestValue2 = (LeaderboardAdapter.mode == LeaderboardMode.TIME) ? user2.getBestTime() : user2.getBestMoves();

                            if (bestValue1 == -1 && bestValue2 != -1) {
                                return 1;
                            } else if (bestValue1 != -1 && bestValue2 == -1) {
                                return -1;
                            } else {
                                return Long.compare(bestValue1, bestValue2);
                            }
                        });

                        leaderboardDataList = userList;
                        leaderboardAdapter.setLeaderboardData(leaderboardDataList);
                    }
                });
    }



}
