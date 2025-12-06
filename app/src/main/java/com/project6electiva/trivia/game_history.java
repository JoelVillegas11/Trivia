// game_history.java
package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class game_history extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GameHistoryAdapter adapter;
    private List<GameRecord> recordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        recordList = new ArrayList<>();
        adapter = new GameHistoryAdapter(recordList, this::showGameDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadGameHistory();
    }

    private void loadGameHistory() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).collection("gameHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    recordList.clear();
                    for (var doc : querySnapshot) {
                        // Mapear campos simples
                        String id = doc.getId();
                        String difficulty = doc.getString("difficulty");
                        String category = doc.getString("category");
                        Long score = doc.getLong("score");
                        Long totalQuestions = doc.getLong("totalQuestions");
                        com.google.firebase.Timestamp timestamp = doc.getTimestamp("timestamp");

                        // Mapear el campo 'details' con validación de tipo
                        Object detailsObj = doc.get("details");
                        List<QuestionResult> details = new ArrayList<>();

                        if (detailsObj instanceof List) {
                            List<?> rawList = (List<?>) detailsObj;
                            for (Object obj : rawList) {
                                if (obj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> map = (Map<String, Object>) obj;
                                    String qText = (String) map.get("questionText");
                                    Boolean isCorr = (Boolean) map.get("isCorrect");
                                    String corrAns = (String) map.get("correctAnswer");

                                    if (qText != null && isCorr != null && corrAns != null) {
                                        details.add(new QuestionResult(qText, isCorr, corrAns));
                                    }
                                }
                            }
                        } else {
                            Log.e("GameHistory", "Campo 'details' no es una lista: " + (detailsObj != null ? detailsObj.getClass().getName() : "null"));
                        }

                        // Crear el objeto GameRecord
                        GameRecord record = new GameRecord();
                        record.id = id;
                        record.difficulty = difficulty;
                        record.category = category;
                        record.score = score != null ? score : 0L;
                        record.totalQuestions = totalQuestions != null ? totalQuestions : 0L;
                        record.timestamp = timestamp;
                        record.details = details;

                        recordList.add(record);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showGameDetails(GameRecord record) {
        Intent intent = new Intent(this, game_details.class);
        intent.putExtra("recordId", record.id); // Solo el ID
        startActivity(intent);
    }
}