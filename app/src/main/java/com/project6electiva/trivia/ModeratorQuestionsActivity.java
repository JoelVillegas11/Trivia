// ModeratorQuestionsActivity.java
package com.project6electiva.trivia;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class ModeratorQuestionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ModeratorQuestionAdapter adapter;
    private List<Question> fullQuestionList; // Lista completa sin filtrar
    private List<Question> filteredQuestionList; // Lista filtrada
    private FirebaseFirestore db;

    private EditText etSearch;
    private Spinner spFilterCategory, spFilterDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_questions);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewQuestions);
        etSearch = findViewById(R.id.etSearch);
        spFilterCategory = findViewById(R.id.spFilterCategory);
        spFilterDifficulty = findViewById(R.id.spFilterDifficulty);
        Button btnAddQuestion = findViewById(R.id.btnAddQuestion);

        // Inicializar listas
        fullQuestionList = new ArrayList<>();
        filteredQuestionList = new ArrayList<>();
        adapter = new ModeratorQuestionAdapter(filteredQuestionList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configurar spinners
        setupSpinners();

        // Cargar preguntas (ordenadas por fecha de creación, más recientes primero)
        loadQuestions();

        // Listener de búsqueda
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listeners de spinners
        spFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spFilterDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAddQuestion.setOnClickListener(v ->
                startActivity(new Intent(this, ModeratorEditQuestionActivity.class)));
    }

    private void setupSpinners() {
        // Categorías
        String[] categories = {"Todas", "movies", "music", "science", "sports"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterCategory.setAdapter(categoryAdapter);

        // Dificultad
        String[] difficulties = {"Todas", "facil", "normal", "dificil"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterDifficulty.setAdapter(difficultyAdapter);
    }

    private void loadQuestions() {
        // Ordenar por createdAt DESC (más reciente primero)
        db.collection("questions")
                .orderBy("text", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullQuestionList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Question q = doc.toObject(Question.class);
                        q.id = doc.getId();
                        fullQuestionList.add(q);
                    }
                    filteredQuestionList.clear();
                    filteredQuestionList.addAll(fullQuestionList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar preguntas", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilters() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        String selectedCategory = spFilterCategory.getSelectedItem().toString();
        String selectedDifficulty = spFilterDifficulty.getSelectedItem().toString();

        filteredQuestionList.clear();

        for (Question q : fullQuestionList) {
            boolean matchesText = query.isEmpty() || q.text.toLowerCase().contains(query);
            boolean matchesCategory = selectedCategory.equals("Todas") || q.categoryId.equals(selectedCategory);
            boolean matchesDifficulty = selectedDifficulty.equals("Todas") || q.difficulty.equals(selectedDifficulty);

            if (matchesText && matchesCategory && matchesDifficulty) {
                filteredQuestionList.add(q);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // Clase modelo (debe incluir 'createdAt' y 'id')
    public static class Question {
        public String text;
        public List<String> options;
        public int correctIndex;
        public String categoryId;
        public String difficulty;
        public double randomOrder;
        public com.google.firebase.Timestamp createdAt; // Para ordenar
        public String id; // Para editar/eliminar

        public Question() {}
    }
}