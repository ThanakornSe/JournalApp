package com.example.udm_19journalfirestoreapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.udm_19journalfirestoreapp.Model.Journal;
import com.example.udm_19journalfirestoreapp.R;
import com.example.udm_19journalfirestoreapp.Util.JournalApi;
import com.example.udm_19journalfirestoreapp.databinding.ActivityPostJournalBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private Uri imageUri;
    private ActivityPostJournalBinding binding;
    ActivityResultLauncher<String> stringActivityResultLauncher
            = registerForActivityResult(new ActivityResultContracts.GetContent()
            , new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    imageUri = result;
                    binding.imvPhoto.setImageURI(result);
                }
            });
    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //create FireStoreConnection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference journalCollection = db.collection("Journal");
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_journal);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        binding.btnAddPhoto.setOnClickListener(this);
        binding.btnPostSave.setOnClickListener(this);

        binding.progressBar.setVisibility(View.GONE);

        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUsername();

            binding.txtUsername.setText(currentUserName);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {

                } else {

                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_post_save:
                saveJournal();
                break;
            case R.id.btn_addPhoto:
                stringActivityResultLauncher.launch("image/*");
                break;
        }
    }

    private void saveJournal() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String title = binding.edtPostTitle.getText().toString();
        String thought = binding.edtPostThought.getText().toString();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri != null) {
            //create path in storage /journal_image/ourImage.jpg but we have to careful about name
            final StorageReference filepath = storageReference
                    .child("journal_images")
                    .child("my_image" + Timestamp.now().getSeconds());//So every we save photo the file's name is not the same

            filepath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {

                //get image Url to POJO and sent back to FireStore
                filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Journal journal = new Journal(title, thought, imageUrl, currentUserId, new Timestamp(new Date()), currentUserName);
                    //invoke our collectionReference
                    journalCollection.add(journal).addOnSuccessListener(documentReference -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(PostJournalActivity.this, JournalListActivity.class);
                        startActivity(intent);
                        finish();
                    }).addOnFailureListener(e -> {
                        Log.d("TAG", "onFailure: " + e.getMessage());
                    });
                }).addOnFailureListener(e -> {
                    Log.d("TAG", "onFailure: " + e.getMessage());
                });
                //TODO: save a Journal Instance
            }).addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
            });
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}