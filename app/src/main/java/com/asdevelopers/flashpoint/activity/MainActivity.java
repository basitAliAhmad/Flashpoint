package com.asdevelopers.flashpoint.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asdevelopers.flashpoint.R;
import com.asdevelopers.flashpoint.adapter.HomeRecyclerViewAdapter;
import com.asdevelopers.flashpoint.model.Database;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    HomeRecyclerViewAdapter homeRecyclerViewAdapter;
    SearchView searchView;

    String userID;

    Database database;
    DatabaseReference databaseReference;
    DataSnapshot data;
    HashMap<String, Object> map;

    public void resetDisplayedData() {
        homeRecyclerViewAdapter = new HomeRecyclerViewAdapter(this, database);
        recyclerView.setAdapter(homeRecyclerViewAdapter);

        homeRecyclerViewAdapter.getFilter().filter("");
        searchView.setQuery("", false);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                homeRecyclerViewAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        resetDisplayedData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userID = getIntent().getStringExtra("userID");

        if (userID == null)
            startActivity(new Intent(MainActivity.this, LoginActivity.class));

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                map = snapshot.getValue(new GenericTypeIndicator<HashMap<String, Object>>() {});
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database = new Database(this);

        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint("Search for a deck");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        findViewById(R.id.add_button).setOnClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);


            alert.setTitle("Add new Deck");
            alert.setMessage("Enter name of Deck and press add");

            final EditText input = new EditText(MainActivity.this);

            input.setSingleLine();
            FrameLayout container = new FrameLayout(MainActivity.this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(30, 0, 30, 0);
            input.setLayoutParams(params);

            input.setHint("Enter name of deck");

            container.addView(input);
            alert.setView(container);
            alert.setPositiveButton("Add", (dialog, whichButton) -> {
                String deckTitle = input.getText().toString();



                database.addDeck(input.getText().toString());
                resetDisplayedData();
                Toast.makeText(MainActivity.this, "Deck created Successfully", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Cancel", (dialog, whichButton) -> {
                // Canceled.
            });

            alert.show();
        });
    }
}