// ModeratorEditQuestionActivity.java
package com.project6electiva.trivia;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ModeratorEditQuestionActivity extends AppCompatActivity {

    private EditText etQuestion, etOptionA, etOptionB, etOptionC, etOptionD;
    private Spinner spCategory, spDifficulty;
    private Button btnSave;
    private FirebaseFirestore db;
    private String questionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_edit_question);

        db = FirebaseFirestore.getInstance();

        etQuestion = findViewById(R.id.etQuestion);
        etOptionA = findViewById(R.id.etOptionA);
        etOptionB = findViewById(R.id.etOptionB);
        etOptionC = findViewById(R.id.etOptionC);
        etOptionD = findViewById(R.id.etOptionD);
        spCategory = findViewById(R.id.spCategory);
        spDifficulty = findViewById(R.id.spDifficulty);
        btnSave = findViewById(R.id.btnSave);

        // Configurar spinners
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_array, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(difficultyAdapter);

        // Cargar datos si es edición
        questionId = getIntent().getStringExtra("questionId");
        if (questionId != null) {
            loadQuestionData();
            btnSave.setText("Actualizar");
        } else {
            btnSave.setText("Crear");
        }

        btnSave.setOnClickListener(v -> saveQuestion());
    }

    private void loadQuestionData() {
        etQuestion.setText(getIntent().getStringExtra("questionText"));

        String[] options = getIntent().getStringArrayExtra("options");
        if (options != null && options.length >= 4) {
            etOptionA.setText(options[0]);
            etOptionB.setText(options[1]);
            etOptionC.setText(options[2]);
            etOptionD.setText(options[3]);
        }

        String categoryId = getIntent().getStringExtra("categoryId");
        if (categoryId != null) {
            setSpinnerSelection(spCategory, categoryId);
        }

        String difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty != null) {
            setSpinnerSelection(spDifficulty, difficulty);
        }
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

    private void saveQuestion() {
        String question = etQuestion.getText().toString().trim();
        String optA = etOptionA.getText().toString().trim();
        String optB = etOptionB.getText().toString().trim();
        String optC = etOptionC.getText().toString().trim();
        String optD = etOptionD.getText().toString().trim();

        if (question.isEmpty() || optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryId = spCategory.getSelectedItem().toString();
        String difficulty = spDifficulty.getSelectedItem().toString();

        List<String> options = Arrays.asList(optA, optB, optC, optD);
        int correctIndex = 0; // Asumimos que la primera opción es la correcta

        Map<String, Object> questionData = new HashMap<>();
        questionData.put("text", question);
        questionData.put("options", options);
        questionData.put("correctIndex", correctIndex);
        questionData.put("categoryId", categoryId);
        questionData.put("difficulty", difficulty);
        questionData.put("randomOrder", new Random().nextDouble());
        questionData.put("createdAt", com.google.firebase.Timestamp.now());

        if (questionId != null) {
            // Actualizar
            db.collection("questions").document(questionId)
                    .update(questionData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Pregunta actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Crear nueva
            db.collection("questions").add(questionData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Pregunta creada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al crear", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}