package com.example.udm_19journalfirestoreapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.udm_19journalfirestoreapp.Adapter.JournalRecViewAdapter;
import com.example.udm_19journalfirestoreapp.Model.Journal;
import com.example.udm_19journalfirestoreapp.R;
import com.example.udm_19journalfirestoreapp.Util.JournalApi;
import com.example.udm_19journalfirestoreapp.databinding.ActivityJournalListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JournalListActivity extends AppCompatActivity {
    private ActivityJournalListBinding binding;
    private List<Journal> journalList;
    private JournalRecViewAdapter adapter;

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_journal_list);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        journalList = new ArrayList<>();

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        binding.RecycleView.setLayoutManager(new LinearLayoutManager(this));
        binding.RecycleView.setHasFixedSize(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Here we will query all of our journal
        journalCollection.whereEqualTo("userId", JournalApi.getInstance().getUserId())
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot journals : queryDocumentSnapshots) {
                    journalList.add(journals.toObject(Journal.class));
                }
                adapter = new JournalRecViewAdapter(journalList,JournalListActivity.this);
                binding.RecycleView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }else{
                binding.listNoThoughts.setVisibility(View.VISIBLE);
            }

        }).addOnFailureListener(e -> {

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                //take user to add journal
                if (currentUser != null && firebaseAuth != null) {
                    Intent intent = new Intent(JournalListActivity.this, PostJournalActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.action_signOut:
                //Sign user out
                if (currentUser != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    Intent intent = new Intent(JournalListActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}