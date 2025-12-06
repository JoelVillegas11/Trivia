// AdminChallengesActivity.java
package com.project6electiva.trivia;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class AdminChallengesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminChallengeAdapter adapter;
    private List<Challenge> challengeList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_challenges);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewChallenges);
        Button btnAddChallenge = findViewById(R.id.btnAddChallenge);

        challengeList = new ArrayList<>();
        adapter = new AdminChallengeAdapter(challengeList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadChallenges();

        btnAddChallenge.setOnClickListener(v ->
                startActivity(new Intent(this, AdminEditChallengeActivity.class)));
    }

    private void loadChallenges() {
        db.collection("challenges")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    challengeList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Challenge c = doc.toObject(Challenge.class);
                        c.id = doc.getId();
                        challengeList.add(c);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar retos", Toast.LENGTH_SHORT).show();
                });
    }

    public static class Challenge {
        public String id;
        public String title;
        public String description;
        public long rewardPoints;
        public long targetValue;
        public String challengeType;
        public String categoryId;
        public String difficulty;
        public boolean isActive;

        public Challenge() {}
    }
}