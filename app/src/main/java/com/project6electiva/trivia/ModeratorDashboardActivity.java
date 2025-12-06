// ModeratorDashboardActivity.java
package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ModeratorDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_dashboard);

        Button btnManageQuestions = findViewById(R.id.btnManageQuestions);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnManageQuestions.setOnClickListener(v ->
                startActivity(new Intent(this, ModeratorQuestionsActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, login.class));
            finish();
        });
    }
}