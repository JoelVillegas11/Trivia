// question.java (completo con recompensas)
package com.project6electiva.trivia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.project6electiva.trivia.QuestionResult;

public class question extends BaseActivity {

    private TextView tvQuestion, tvTimer, tvProgress;
    private RadioGroup rgOptions;
    private ProgressBar progressTimer;
    private FirebaseFirestore db;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private CountDownTimer timer;
    private List<QuestionResult> resultsHistory = new ArrayList<>();
    private String categoryId;
    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        db = FirebaseFirestore.getInstance();
        categoryId = getIntent().getStringExtra("category");
        difficulty = getIntent().getStringExtra("difficulty");

        tvQuestion = findViewById(R.id.tvQuestion);
        tvTimer = findViewById(R.id.tvTimer);
        tvProgress = findViewById(R.id.tvProgress);
        rgOptions = findViewById(R.id.rgOptions);
        progressTimer = findViewById(R.id.progressTimer);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(question.this, "Debes terminar el juego.", Toast.LENGTH_SHORT).show();
            }
        });

        setFinishOnTouchOutside(false);

        loadQuestions(categoryId, difficulty);
    }

    @Override
    protected void handleNoNetwork() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (isFinishing() || isDestroyed()) return;

        new AlertDialog.Builder(this)
                .setTitle("Conexión Perdida")
                .setMessage("Se ha detectado la pérdida de conexión. El juego ha terminado.")
                .setCancelable(false)
                .setPositiveButton("Regresar al Menú", (dialog, id) -> {
                    Intent intent = new Intent(question.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void loadQuestions(String category, String difficulty) {
        double randomSeed = new Random().nextDouble();

        db.collection("questions")
                .whereEqualTo("categoryId", category)
                .whereEqualTo("difficulty", difficulty)
                .orderBy("randomOrder")
                .startAt(randomSeed)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    questions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Question q = document.toObject(Question.class);
                        questions.add(q);
                    }

                    Collections.shuffle(questions);

                    if (!questions.isEmpty()) {
                        showQuestion(questions.get(currentQuestionIndex));
                    } else {
                        Toast.makeText(this, "No hay preguntas disponibles", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar preguntas", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void showQuestion(Question q) {
        if (isFinishing()) return;

        tvQuestion.setText(q.text);
        tvProgress.setText("Pregunta " + (currentQuestionIndex + 1) + "/" + questions.size());

        List<String> options = new ArrayList<>(q.options);
        int originalCorrectIndex = q.correctIndex;
        String correctAnswer = options.get(originalCorrectIndex);

        Collections.shuffle(options);
        int newCorrectIndex = options.indexOf(correctAnswer);
        q.correctIndex = newCorrectIndex;

        RadioButton rbA = findViewById(R.id.rbA);
        RadioButton rbB = findViewById(R.id.rbB);
        RadioButton rbC = findViewById(R.id.rbC);
        RadioButton rbD = findViewById(R.id.rbD);

        rbA.setText("A) " + options.get(0));
        rbB.setText("B) " + options.get(1));
        rbC.setText("C) " + options.get(2));
        rbD.setText("D) " + options.get(3));

        rgOptions.clearCheck();

        rgOptions.setOnCheckedChangeListener(null);
        rgOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                RadioButton selected = findViewById(checkedId);
                int selectedIndex = -1;
                if (selected == rbA) selectedIndex = 0;
                else if (selected == rbB) selectedIndex = 1;
                else if (selected == rbC) selectedIndex = 2;
                else if (selected == rbD) selectedIndex = 3;

                boolean isCorrect = (selectedIndex == q.correctIndex);
                if (isCorrect) correctCount++;

                resultsHistory.add(new QuestionResult(q.text, isCorrect, correctAnswer));

                if (timer != null) timer.cancel();

                currentQuestionIndex++;
                if (currentQuestionIndex < questions.size()) {
                    showQuestion(questions.get(currentQuestionIndex));
                } else {
                    finishGame();
                }
            }
        });

        startTimer();
    }

    private void startTimer() {
        int totalSeconds = 13;
        if ("normal".equals(difficulty)) {
            totalSeconds = 18;
        } else if ("dificil".equals(difficulty)) {
            totalSeconds = 23;
        }

        final int finalTotalSeconds = totalSeconds;

        tvTimer.setText(finalTotalSeconds + "s");
        progressTimer.setMax(finalTotalSeconds);
        progressTimer.setProgress(finalTotalSeconds);

        timer = new CountDownTimer(finalTotalSeconds * 1000, 1000) {
            int secondsLeft = finalTotalSeconds;

            @Override
            public void onTick(long millisUntilFinished) {
                secondsLeft--;
                tvTimer.setText(secondsLeft + "s");
                progressTimer.setProgress(secondsLeft);
            }

            @Override
            public void onFinish() {
                if (isFinishing()) return;

                Question currentQ = questions.get(currentQuestionIndex);
                String correctAnswer = currentQ.options.get(currentQ.correctIndex);

                resultsHistory.add(new QuestionResult(currentQ.text, false, correctAnswer));

                currentQuestionIndex++;
                if (currentQuestionIndex < questions.size()) {
                    showQuestion(questions.get(currentQuestionIndex));
                } else {
                    finishGame();
                }
            }
        }.start();
    }

    private void finishGame() {
        saveGameHistory();
        updateTotalScore(correctCount);
        checkChallenges(); // 👈 NUEVO: Verificar retos al finalizar

        Intent intent = new Intent(question.this, results.class);
        intent.putExtra("correctCount", correctCount);
        intent.putExtra("totalQuestions", questions.size());
        intent.putParcelableArrayListExtra("resultsHistory", new ArrayList<>(resultsHistory));
        startActivity(intent);
        finish();
    }

    private void updateTotalScore(int scoreToAdd) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .update("points", FieldValue.increment(scoreToAdd))
                .addOnFailureListener(e -> {
                    Log.e("QuestionActivity", "Error al actualizar puntaje total", e);
                });
    }

    // === NUEVO: LÓGICA DE RETOS ===
    // === NUEVO: LÓGICA DE RETOS CORREGIDA ===
    private void checkChallenges() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Primero, incrementar el contador de partidas jugadas en la categoría
        Map<String, Object> categoryUpdate = new HashMap<>();
        String categoryPath = "gamesPlayedByCategory." + categoryId;
        categoryUpdate.put(categoryPath, FieldValue.increment(1));

        db.collection("users").document(uid)
                .update(categoryUpdate)
                .addOnSuccessListener(aVoid -> {
                    // Ahora, cargar los retos y verificar
                    db.collection("challenges").whereEqualTo("isActive", true).get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (var doc : querySnapshot) {
                                    Challenge c = doc.toObject(Challenge.class);
                                    c.id = doc.getId();

                                    // Verificar si ya fue completado
                                    db.collection("users").document(uid)
                                            .get()
                                            .addOnSuccessListener(userDoc -> {
                                                if (!userDoc.exists()) return;

                                                List<String> completed = (List<String>) userDoc.get("completedChallenges");
                                                if (completed != null && completed.contains(c.id)) return;

                                                boolean isMet = false;

                                                switch (c.challengeType) {
                                                    case "consecutivePerfectScores":
                                                        if (c.categoryId != null && !c.categoryId.equals(categoryId)) break;
                                                        if (c.difficulty != null && !c.difficulty.equals(difficulty)) break;
                                                        if (correctCount == 10) {
                                                            isMet = true;
                                                        }
                                                        break;
                                                    case "totalGamesPlayed":
                                                        if (c.categoryId != null && !c.categoryId.equals(categoryId)) break;
                                                        // Obtener el contador actual
                                                        Map<String, Object> gamesMap = (Map<String, Object>) userDoc.get("gamesPlayedByCategory");
                                                        long currentCount = 0;
                                                        if (gamesMap != null && gamesMap.containsKey(categoryId)) {
                                                            // Firestore devuelve Long o Integer, manejamos ambos
                                                            Object countObj = gamesMap.get(categoryId);
                                                            if (countObj instanceof Long) {
                                                                currentCount = (Long) countObj;
                                                            } else if (countObj instanceof Integer) {
                                                                currentCount = ((Integer) countObj).longValue();
                                                            }
                                                        }
                                                        // Verificar si se alcanzó la meta
                                                        if (currentCount >= c.targetValue) {
                                                            isMet = true;
                                                        }
                                                        break;
                                                    case "firstPerfectHard":
                                                        if ("dificil".equals(difficulty) && correctCount == 10) {
                                                            isMet = true;
                                                        }
                                                        break;
                                                }

                                                if (isMet) {
                                                    // Sumar puntos y marcar como completado
                                                    db.collection("users").document(uid)
                                                            .update(
                                                                    "points", FieldValue.increment(c.rewardPoints),
                                                                    "completedChallenges", FieldValue.arrayUnion(c.id)
                                                            )
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                Toast.makeText(this, "¡Has completado un reto! + " + c.rewardPoints + " puntos", Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            });
                                }
                            });
                });
    }

    // === MODELOS ===
    public static class Question {
        public String text;
        public List<String> options;
        public int correctIndex;
        public String categoryId;
        public String difficulty;
        public double randomOrder;
        public Question() {}
    }

    // Clase para los retos
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

    private void saveGameHistory() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> gameRecord = new HashMap<>();
        gameRecord.put("difficulty", difficulty);
        gameRecord.put("category", categoryId);
        gameRecord.put("score", correctCount);
        gameRecord.put("totalQuestions", questions.size());
        gameRecord.put("timestamp", com.google.firebase.Timestamp.now());

        List<Map<String, Object>> detailsList = new ArrayList<>();
        for (QuestionResult item : resultsHistory) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("questionText", item.questionText);
            detail.put("isCorrect", item.isCorrect);
            detail.put("correctAnswer", item.correctAnswer);
            detailsList.add(detail);
        }
        gameRecord.put("details", detailsList);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).collection("gameHistory")
                .add(gameRecord)
                .addOnSuccessListener(documentReference -> {
                    enforceMaxHistorySize(uid, db);
                })
                .addOnFailureListener(e -> {
                    // Error al guardar, pero seguimos
                });
    }

    private void enforceMaxHistorySize(String uid, FirebaseFirestore db) {
        db.collection("users").document(uid).collection("gameHistory")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                    if (docs.size() > 10) {
                        db.collection("users").document(uid).collection("gameHistory")
                                .document(docs.get(0).getId())
                                .delete();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}