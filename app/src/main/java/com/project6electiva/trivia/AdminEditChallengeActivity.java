// AdminEditChallengeActivity.java
package com.project6electiva.trivia;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminEditChallengeActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etReward, etTarget;
    private Spinner spType, spCategory, spDifficulty;
    private Button btnSave;
    private FirebaseFirestore db;
    private String challengeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_challenge);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etReward = findViewById(R.id.etReward);
        etTarget = findViewById(R.id.etTarget);
        spType = findViewById(R.id.spType);
        spCategory = findViewById(R.id.spCategory);
        spDifficulty = findViewById(R.id.spDifficulty);
        btnSave = findViewById(R.id.btnSave);

        setupSpinners();

        // Cargar datos si es edición
        challengeId = getIntent().getStringExtra("challengeId");
        if (challengeId != null) {
            loadChallengeData();
            btnSave.setText("Actualizar");
        } else {
            btnSave.setText("Crear");
        }

        btnSave.setOnClickListener(v -> saveChallenge());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.challenge_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_array, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(difficultyAdapter);
    }

    private void loadChallengeData() {
        etTitle.setText(getIntent().getStringExtra("title"));
        etDescription.setText(getIntent().getStringExtra("description"));
        etReward.setText(String.valueOf(getIntent().getLongExtra("rewardPoints", 0)));
        etTarget.setText(String.valueOf(getIntent().getLongExtra("targetValue", 0)));

        setSpinnerSelection(spType, getIntent().getStringExtra("challengeType"));
        setSpinnerSelection(spCategory, getIntent().getStringExtra("categoryId") != null ?
                getIntent().getStringExtra("categoryId") : "Todas");
        setSpinnerSelection(spDifficulty, getIntent().getStringExtra("difficulty") != null ?
                getIntent().getStringExtra("difficulty") : "Todas");
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveChallenge() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        long reward = Long.parseLong(etReward.getText().toString().trim());
        long target = Long.parseLong(etTarget.getText().toString().trim());
        String type = spType.getSelectedItem().toString();
        String category = spCategory.getSelectedItem().toString();
        String difficulty = spDifficulty.getSelectedItem().toString();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> challenge = new HashMap<>();
        challenge.put("title", title);
        challenge.put("description", desc);
        challenge.put("rewardPoints", reward);
        challenge.put("targetValue", target);
        challenge.put("challengeType", type);
        challenge.put("categoryId", category.equals("Todas") ? null : category);
        challenge.put("difficulty", difficulty.equals("Todas") ? null : difficulty);
        challenge.put("isActive", true);
        challenge.put("createdAt", com.google.firebase.Timestamp.now()); // 👈 NUEVO

        if (challengeId != null) {
            db.collection("challenges").document(challengeId).update(challenge)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Reto actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("challenges").add(challenge)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Reto creado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al crear: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}