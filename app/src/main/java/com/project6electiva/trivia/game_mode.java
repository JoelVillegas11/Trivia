// GameModeActivity.java
package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class game_mode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);

        Button btnSolo = findViewById(R.id.btnSolo);
        btnSolo.setOnClickListener(v -> {
            startActivity(new Intent(game_mode.this, categories.class));
        });

        // Botón "En línea" se omite por ahora (no funcional)
    }
}