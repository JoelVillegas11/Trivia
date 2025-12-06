// game_details.java
package com.project6electiva.trivia;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class game_details extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);

        String recordId = getIntent().getStringExtra("recordId");
        if (recordId == null) {
            Toast.makeText(this, "Error: ID de partida no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).collection("gameHistory").document(recordId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Mapear campos simples
                        String difficulty = documentSnapshot.getString("difficulty");
                        String category = documentSnapshot.getString("category");
                        Long score = documentSnapshot.getLong("score");
                        Long totalQuestions = documentSnapshot.getLong("totalQuestions");
                        Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");

                        // Mapear 'details' manualmente
                        List<QuestionResult> details = new ArrayList<>();
                        List<Map<String, Object>> detailsMap = (List<Map<String, Object>>) documentSnapshot.get("details");
                        if (detailsMap != null) {
                            for (Map<String, Object> map : detailsMap) {
                                String qText = (String) map.get("questionText");
                                Boolean isCorr = (Boolean) map.get("isCorrect");
                                String corrAns = (String) map.get("correctAnswer");

                                if (qText != null && isCorr != null && corrAns != null) {
                                    details.add(new QuestionResult(qText, isCorr, corrAns));
                                }
                            }
                        }

                        // Mostrar en la UI
                        setupUI(difficulty, category, score, totalQuestions, timestamp, details);
                    } else {
                        Toast.makeText(this, "Partida no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar partida: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupUI(String difficulty, String category, Long score, Long totalQuestions, Timestamp timestamp, List<QuestionResult> details) {
        TextView tvHeader = findViewById(R.id.tvHeader);
        String dateStr = "Fecha desconocida";
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            dateStr = sdf.format(timestamp.toDate());
        }
        tvHeader.setText("Partida del " + dateStr);

        TableLayout table = findViewById(R.id.tableResults);

        TableRow header = new TableRow(this);
        header.setBackgroundColor(0xFF152A40);
        addHeaderCell(header, "Pregunta");
        addHeaderCell(header, "Estado");
        addHeaderCell(header, "Respuesta Correcta");
        table.addView(header);

        if (details != null) {
            for (QuestionResult item : details) {
                TableRow row = new TableRow(this);
                row.setBackgroundColor(0xFF1C2E45);
                addDataCell(row, item.questionText);
                addDataCell(row, item.isCorrect ? "✅" : "❌");
                addDataCell(row, item.correctAnswer);
                table.addView(row);
            }
        }
    }

    private void addHeaderCell(TableRow row, String text) {
        TextView tv = createTextView(text, true, true);
        row.addView(tv);
    }

    private void addDataCell(TableRow row, String text) {
        TextView tv = createTextView(text, false, false);
        row.addView(tv);
    }

    private TextView createTextView(String text, boolean isHeader, boolean isCenter) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFFFFFFF);
        tv.setPadding(8, 8, 8, 8);
        if (isHeader) {
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (isCenter) {
            tv.setGravity(Gravity.CENTER);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(params);
        return tv;
    }
}