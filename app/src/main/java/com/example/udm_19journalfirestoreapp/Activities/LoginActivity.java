package com.example.udm_19journalfirestoreapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.udm_19journalfirestoreapp.R;
import com.example.udm_19journalfirestoreapp.Util.JournalApi;
import com.example.udm_19journalfirestoreapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //create FireStoreConnection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userCollection = db.collection("User");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.autoEdtEmail.getText().toString();
                String password = binding.edtPassword.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    loginEmailPasswordUser(email, password);
                } else {
                    new AlertDialog.Builder(LoginActivity.this).setMessage("Please fill up all information")
                            .setPositiveButton("Ok", null).show();
                }

            }
        });


    }

    private void loginEmailPasswordUser(String email, String password) {
        binding.loginProgessbar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    String currentUserId = currentUser.getUid();
                    userCollection.whereEqualTo("userId", currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if (error != null) {

                                    }
                                    if (value != null && !value.isEmpty()) {
                                        binding.loginProgessbar.setVisibility(View.GONE);
                                        for(QueryDocumentSnapshot snapshot:value){
                                            //Set All of username and password to global class and compare to what we fill up
                                            JournalApi.getInstance().setUsername(snapshot.getString("username"));
                                            JournalApi.getInstance().setUserId(snapshot.getString("userId"));

                                            //Go to list Activity
                                            startActivity(new Intent(LoginActivity.this,JournalListActivity.class));
                                        }
                                    }
                                }
                            });
                }else {
                    binding.loginProgessbar.setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(e -> {

        });
    }
}