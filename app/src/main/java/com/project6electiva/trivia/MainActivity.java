// com.project6electiva.trivia.MainActivity.java
package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnJugar = findViewById(R.id.btnJugar);
        Button btnCompetir = findViewById(R.id.btnCompetir);

        btnJugar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, categories.class));
        });

        btnCompetir.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, game_mode.class));
        });
    }
}