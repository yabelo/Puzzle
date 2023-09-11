package com.example.puzzle.NavBar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.puzzle.Account.UserAccount;
import com.example.puzzle.NavBar.Leaderboard.LeaderboardFragment;
import com.example.puzzle.R;
import com.example.puzzle.StartFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            UserAccount.userId = currentUser.getUid();

            TextView bestMessage = rootView.findViewById(R.id.bestMessage);

            // Set the initial data if available
            if (UserAccount.bestTime != -1 && UserAccount.bestMoves != -1) {
                String bestTimeMessage = (UserAccount.bestTime == -1) ? "N/A" : UserAccount.bestTime + " seconds";
                String bestMovesMessage = (UserAccount.bestMoves == -1) ? "N/A" : UserAccount.bestMoves + " moves";
                String fullMessage = "Best Records:\n" + bestTimeMessage + "\n" + bestMovesMessage;
                bestMessage.setText(fullMessage);
            }

            // Fetch updated data from Firebase and update UserAccount
            firestore.collection("users").document(UserAccount.userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserAccount.bestTime = documentSnapshot.getLong("bestTime");
                            UserAccount.bestMoves = documentSnapshot.getLong("bestMoves");

                            String bestTimeMessage = (UserAccount.bestTime == -1) ? "N/A" : UserAccount.bestTime + " seconds";
                            String bestMovesMessage = (UserAccount.bestMoves == -1) ? "N/A" : UserAccount.bestMoves + " moves";
                            String fullMessage = "Best Records:\n" + bestTimeMessage + "\n" + bestMovesMessage;
                            bestMessage.setText(fullMessage);
                        }
                    });
        }

        ImageButton homeButton = rootView.findViewById(R.id.homeBtn);
        homeButton.setOnClickListener(v -> {
            // Replace the StartFragment with the ProfileFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new StartFragment());
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