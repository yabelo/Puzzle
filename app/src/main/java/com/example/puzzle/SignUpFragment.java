package com.example.puzzle;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.puzzle.Account.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SignUpFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signup, container, false);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        Button submitBtn = rootView.findViewById(R.id.sumbitBtn);
        EditText usernameField = rootView.findViewById(R.id.usernameField);

        usernameField.setTextDirection(View.TEXT_DIRECTION_LTR);

        submitBtn.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = generateRandomPassword(10); // Replace with your password generation logic
            if (!username.isEmpty()) {
                signUpWithUsernameAndPassword(username, password);
            }
        });

        return rootView;
    }

    private void signUpWithUsernameAndPassword(String username, String password) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Check if the username is too long
        if (username.length() > 16) {
            Toast.makeText(requireContext(), "Username cannot be more than 16 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        // First, check if the username already exists
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Username already exists
                            QueryDocumentSnapshot userDocument = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                            String existingUsername = userDocument.getString("username"); // Fetch the username
                            Toast.makeText(requireContext(), "Username " + existingUsername + " already exists.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Username is available, proceed with registration
                        mAuth.createUserWithEmailAndPassword(username + "@puzzle.game", password)
                                .addOnCompleteListener(requireActivity(), registrationTask -> {
                                    if (registrationTask.isSuccessful()) {
                                        // Registration successful
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        assert currentUser != null;
                                        String userId = currentUser.getUid(); // Get the current user's ID

                                        saveUserDataToDatabase(userId, username); // Initialize bestTime and bestMoves

                                        // Save username to SharedPreferences
                                        editor.putBoolean("loggedIn", true);
                                        editor.putString("username", username);
                                        editor.apply();

                                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                        transaction.replace(R.id.fragment_container, new StartFragment());
                                        transaction.addToBackStack(null);
                                        transaction.commit();
                                    } else {
                                        Toast.makeText(requireContext(), "Registration failed.", Toast.LENGTH_SHORT).show();
                                        if (registrationTask.getException() != null) {
                                            System.out.println(registrationTask.getException().getMessage());
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(requireContext(), "Error checking username availability.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveUserDataToDatabase(String userId, String username) {

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("bestTime", -1);
        userData.put("bestMoves", -1);

        System.out.println("New username registered: " + userData.get("username"));

        UserAccount.userId = userId;
        UserAccount.username = username;
        UserAccount.bestTime = -1;
        UserAccount.bestMoves = -1;

        mFirestore.collection("users").document(userId)
                .set(userData);
    }



    public static String generateRandomPassword(int len)
    {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        SecureRandom random = new SecureRandom();

        return IntStream.range(0, len)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(randomIndex -> String.valueOf(chars.charAt(randomIndex)))
                .collect(Collectors.joining());
    }

}
