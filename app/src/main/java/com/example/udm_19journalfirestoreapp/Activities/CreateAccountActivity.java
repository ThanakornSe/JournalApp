package com.example.udm_19journalfirestoreapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.udm_19journalfirestoreapp.R;
import com.example.udm_19journalfirestoreapp.Util.JournalApi;
import com.example.udm_19journalfirestoreapp.databinding.ActivityCreateAccountBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    private ActivityCreateAccountBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //create FireStoreConnection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userCollection = db.collection("User");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_account);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    //this mean user is already logged-in

                } else {
                    //this mean no account yet
                }

            }
        };

        binding.btnCreateAccount.setOnClickListener(v -> {
            String email = binding.accountEmail.getText().toString().trim();
            String password = binding.accountPassword.getText().toString().trim();
            String username = binding.usernameAccount.getText().toString().trim();
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {
                createUserEmailAccount(email, password, username);
            } else {
                new AlertDialog.Builder(this).setMessage("Please fill up all information")
                        .setPositiveButton("Ok", null);
            }
        });
    }

    private void createUserEmailAccount(String email, String password, String username) {
        binding.accountProgessbar.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //this mean create user is completed then we take user to AddJournal Activity
                    currentUser = firebaseAuth.getCurrentUser();
                    if (currentUser != null) {
                        final String currentUserId = currentUser.getUid();
                        //Create User and Map<UserID,UserName> so we can Map userName and UserId in the User Collection
                        //To track user activity on fireStore
                        Map<String, String> userObj = new HashMap<>();
                        userObj.put("userId", currentUserId);
                        userObj.put("username", username);
                        //save to our FireStore database
                        userCollection.add(userObj).addOnSuccessListener(documentReference -> {
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (Objects.requireNonNull(task.getResult()).exists()) {
                                        binding.accountProgessbar.setVisibility(View.GONE);
                                        String name = task.getResult().getString("username");

                                        //Save currentUserId and Username in Global Variable
                                        JournalApi journalApi = JournalApi.getInstance();
                                        journalApi.setUserId(currentUserId);
                                        journalApi.setUsername(username);

                                        Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                        intent.putExtra("username", name);
                                        intent.putExtra("userId", currentUserId);
                                        startActivity(intent);
                                    } else {
                                        binding.accountProgessbar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }).addOnFailureListener(e -> {

                        });
                    }
                } else {
                    //Something went wrong
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}