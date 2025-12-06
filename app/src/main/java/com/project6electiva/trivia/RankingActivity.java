// RankingActivity.java
package com.project6electiva.trivia;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class RankingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RankingAdapter adapter;
    private List<RankingAdapter.RankingItem> rankingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        recyclerView = findViewById(R.id.recyclerViewRanking);
        rankingList = new ArrayList<>();
        adapter = new RankingAdapter(rankingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadRanking();
    }

    private void loadRanking() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    rankingList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        // Manejar campos nulos o de tipo incorrecto
                        Object pointsObj = doc.get("points");
                        Long points = null;

                        if (pointsObj instanceof Long) {
                            points = (Long) pointsObj;
                        } else if (pointsObj instanceof Integer) {
                            points = ((Integer) pointsObj).longValue();
                        } else if (pointsObj instanceof Double) {
                            points = ((Double) pointsObj).longValue();
                        }

                        // Filtrar usuarios sin puntos válidos
                        if (points == null || points <= 0) continue;

                        String name = doc.getString("name");
                        Long emblemaIndex = doc.getLong("emblemaIndex");

                        if (name != null) {
                            rankingList.add(new RankingAdapter.RankingItem(name, points, emblemaIndex));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar ranking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("RankingActivity", "Error en consulta", e);
                });
    }
}