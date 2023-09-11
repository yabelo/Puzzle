package com.example.puzzle;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.puzzle.Account.UserAccount;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("Account", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("loggedIn", false);

        if (savedInstanceState == null) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null && isLoggedIn) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = currentUser.getUid();
                String username = sharedPreferences.getString("username", null);

                // Use the checkIfUserExists method here
                checkIfUserExists(userId, username, new UserExistsCallback() {
                    @Override
                    public void onUserExists(boolean userExists) {
                        if (userExists) {
                            // User exists in Firestore
                            // Fetch user's data from Firestore and update UserAccount
                            DocumentReference userRef = db.collection("users").document(userId);

                            userRef.get().addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Long bestTime = documentSnapshot.getLong("bestTime");
                                    Long bestMoves = documentSnapshot.getLong("bestMoves");

                                    UserAccount.userId = userId;
                                    UserAccount.username = username;
                                    UserAccount.bestTime = (bestTime != null) ? bestTime : -1;
                                    UserAccount.bestMoves = (bestMoves != null) ? bestMoves : -1;

                                    // Replace the fragment
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, new StartFragment())
                                            .commit();
                                }
                            });
                        } else {
                            // User does not exist in Firestore
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new SignUpFragment())
                                    .commit();
                        }
                    }
                });
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SignUpFragment())
                        .commit();
            }
        }
    }


    private void checkIfUserExists(String userId, String username, UserExistsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean userExists = task.getResult().exists();
                callback.onUserExists(userExists);
            } else {
                // Handle the error case
                callback.onUserExists(false); // Return false in case of an error
            }
        });
    }

    // Define a callback interface
    interface UserExistsCallback {
        void onUserExists(boolean userExists);
    }



    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof GameFragment) {
            super.onBackPressed();
        }
    }
}

