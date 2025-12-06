// UserChallengesActivity.java
package com.project6electiva.trivia;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserChallengesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserChallengeAdapter adapter;
    private List<ChallengeItem> challengeList;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_challenges);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        recyclerView = findViewById(R.id.recyclerViewUserChallenges);
        challengeList = new ArrayList<>();
        adapter = new UserChallengeAdapter(challengeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUserChallenges();
    }

    private void loadUserChallenges() {
        if (currentUserId == null) return;

        // Cargar retos activos y el estado del usuario
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) return;

                    List<String> tempCompletedIds = (List<String>) userDoc.get("completedChallenges");

                    // 💡 PASO 1: Establecer la variable final inmediatamente
                    final List<String> completedIds;

                    if (tempCompletedIds == null) {
                        completedIds = new ArrayList<>();
                    } else {
                        // 💡 PASO 2: Si existe, usar el valor obtenido, pero como final.
                        completedIds = tempCompletedIds;
                    }
                    // A partir de aquí, 'completedIds' es efectivamente final.

                    db.collection("challenges").whereEqualTo("isActive", true).get()
                            .addOnSuccessListener(querySnapshot -> {
                                challengeList.clear();
                                for (QueryDocumentSnapshot doc : querySnapshot) {
                                    Challenge c = doc.toObject(Challenge.class);
                                    c.id = doc.getId();

                                    // Ahora 'completedIds' es final y se puede usar.
                                    boolean isCompleted = completedIds.contains(c.id);
                                    challengeList.add(new ChallengeItem(c, isCompleted));
                                }
                                adapter.notifyDataSetChanged();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar retos", Toast.LENGTH_SHORT).show();
                });
    }

    // Modelo para los retos
    public static class Challenge {
        public String id;
        public String title;
        public String description;
        public long rewardPoints;
        public String challengeType;
        public Challenge() {}
    }

    public static class ChallengeItem {
        public Challenge challenge;
        public boolean isCompleted;

        public ChallengeItem(Challenge challenge, boolean isCompleted) {
            this.challenge = challenge;
            this.isCompleted = isCompleted;
        }
    }
}