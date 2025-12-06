// results.java
package com.project6electiva.trivia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import java.util.ArrayList;

// 💡 IMPORTACIÓN GLOBAL NECESARIA
import com.project6electiva.trivia.QuestionResult;

public class results extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        TextView tvScore = findViewById(R.id.tvScore);
        TextView tvMessage = findViewById(R.id.tvMessage);
        Button btnBackToMenu = findViewById(R.id.btnBackToMenu);
        TableLayout tableResults = findViewById(R.id.tableResults);

        int correctCount = getIntent().getIntExtra("correctCount", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 10);

        // 1. CORRECCIÓN: Usar el tipo de clase global (QuestionResult)
        ArrayList<QuestionResult> resultsHistory = getIntent().getParcelableArrayListExtra("resultsHistory");

        // Mensaje personalizado
        String gradeMessage = getGradeMessage(correctCount);
        tvScore.setText(String.valueOf(correctCount));
        tvMessage.setText(gradeMessage + ". Respondiste " + correctCount + " de " + totalQuestions + " preguntas correctamente.");

        // Llenar la tabla si hay datos
        if (resultsHistory != null && !resultsHistory.isEmpty()) {

            for (int i = 0; i < resultsHistory.size(); i++) {
                // 2. CORRECCIÓN: Usar el tipo de clase global (QuestionResult)
                QuestionResult item = resultsHistory.get(i);

                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                ));
                row.setBackgroundColor(0xFF1C2E45);

                // Columna 1: Pregunta
                TextView tvQuestion = createTextView(item.questionText, false, false, 2f);

                // Columna 2: Estado
                TextView tvStatus = createTextView(item.isCorrect ? "✅" : "❌", false, true, 1f);

                // Columna 3: Respuesta correcta
                TextView tvCorrect = createTextView(item.correctAnswer, false, false, 1f);

                row.addView(tvQuestion);
                row.addView(tvStatus);
                row.addView(tvCorrect);
                tableResults.addView(row);
            }
        }

        btnBackToMenu.setOnClickListener(v -> {
            startActivity(new Intent(results.this, MainActivity.class));
            finish();
        });
    }

    // Método auxiliar para crear las celdas de la tabla (Optimización de tu código)
    private TextView createTextView(String text, boolean isHeader, boolean isCenter, float weight) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(8, 8, 8, 8);
        if (isHeader) {
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (isCenter) {
            tv.setGravity(Gravity.CENTER);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
        tv.setLayoutParams(params);
        return tv;
    }

    private void addHeaderCell(TableRow row, String text) {
        TextView tv = createTextView(text, true, true, 1f);
        row.addView(tv);
    }

    // ... (getGradeMessage y handleNoNetwork son correctos)
    private String getGradeMessage(int score) {
        if (score >= 9) {
            return "¡Felicidades! 🏆 Eres un experto en Trivia";
        } else if (score >= 7) {
            return "¡Muy Bien! Sigue practicando";
        } else if (score >= 5) {
            return "Aceptable. Estuviste cerca";
        } else if (score >= 3) {
            return "Puedes mejorar. Es hora de estudiar";
        } else {
            return "Lo sentimos. Vuelve a intentarlo";
        }
    }

    @Override
    protected void handleNoNetwork() {
        // ... (handleNoNetwork es correcto)
        new AlertDialog.Builder(this)
                .setTitle("Conexión Perdida")
                .setMessage("Se ha detectado la pérdida de conexión. Regresando al menú principal.")
                .setCancelable(false)
                .setPositiveButton("Regresar al Menú", (dialog, id) -> {
                    Intent intent = new Intent(results.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}