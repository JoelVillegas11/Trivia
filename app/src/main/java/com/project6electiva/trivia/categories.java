// categories.java
package com.project6electiva.trivia;

import android.app.AlertDialog; // Importación necesaria para AlertDialog
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// 1. CAMBIAMOS AppCompatActivity por BaseActivity
public class categories extends BaseActivity {

    private RadioGroup rgDifficulty;
    private Button btnPeliculas;
    private Button btnMusica;
    private Button btnCiencia;
    private Button btnDeportes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llama al onCreate de BaseActivity para inicializar el NetworkReceiver
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        // Inicialización de vistas
        rgDifficulty = findViewById(R.id.rgDifficulty);
        btnPeliculas = findViewById(R.id.btnPeliculas);
        btnMusica = findViewById(R.id.btnMusica);
        btnCiencia = findViewById(R.id.btnCiencia);
        btnDeportes = findViewById(R.id.btnDeportes);

        // Asignación de Listeners
        btnPeliculas.setOnClickListener(v -> startQuestionActivity("movies"));
        btnMusica.setOnClickListener(v -> startQuestionActivity("music"));
        btnCiencia.setOnClickListener(v -> startQuestionActivity("science"));
        btnDeportes.setOnClickListener(v -> startQuestionActivity("sports"));
    }

    /**
     * Método auxiliar para iniciar QuestionActivity con la categoría y dificultad seleccionadas.
     */
    private void startQuestionActivity(String category) {
        // Verificación de red adicional antes de avanzar (Buena Práctica)
        if (!NetworkReceiver.isConnected) {
            Toast.makeText(this, "Conexión perdida. Por favor, verifica tu red.", Toast.LENGTH_SHORT).show();
            // Llama a la lógica de desconexión si la conexión se ha perdido
            handleNoNetwork();
            return;
        }

        int selectedId = rgDifficulty.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Por favor selecciona una dificultad", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rb = findViewById(selectedId);
        String difficultyText = rb.getText().toString();
        String difficulty;

        // Estandarizar la dificultad
        if (difficultyText.equals("Fácil")) {
            difficulty = "facil";
        } else if (difficultyText.equals("Difícil")) {
            difficulty = "dificil";
        } else {
            difficulty = "normal";
        }

        Intent intent = new Intent(categories.this, question.class);
        intent.putExtra("category", category);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }

    /**
     * 2. IMPLEMENTACIÓN DEL MÉTODO ABSTRACTO handleNoNetwork()
     * Se llama cuando el BroadcastReceiver detecta que la red ha caído.
     */
    @Override
    protected void handleNoNetwork() {
        // Detiene cualquier proceso de la actividad (aquí solo mostramos el diálogo)

        // Mostrar AlertDialog y redirigir al menú principal (MainActivity)
        new AlertDialog.Builder(this)
                .setTitle("Conexión Perdida")
                .setMessage("Se ha detectado la pérdida de conexión. Regresando al menú principal.")
                .setCancelable(false) // No permite cerrar sin acción
                .setPositiveButton("OK", (dialog, id) -> {
                    // Redirigir a MainActivity y finalizar esta actividad
                    Intent intent = new Intent(categories.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}