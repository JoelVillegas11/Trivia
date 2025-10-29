package com.project6electiva.trivia;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class categories extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ❌ EdgeToEdge.enable(this); // Eliminado
        setContentView(R.layout.activity_categories);

        // ❌ Todo el bloque ViewCompat.setOnApplyWindowInsetsListener... // Eliminado
    }
}