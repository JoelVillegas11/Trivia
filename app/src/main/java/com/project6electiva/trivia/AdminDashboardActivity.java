// AdminDashboardActivity.java
package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnManageChallenges = findViewById(R.id.btnManageChallenges);
        Button btnManageUsers = findViewById(R.id.btnManageUsers);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnManageChallenges.setOnClickListener(v ->
                startActivity(new Intent(this, AdminChallengesActivity.class)));

        btnManageUsers.setOnClickListener(v ->
                startActivity(new Intent(this, AdminUsersActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, login.class));
            finish();
        });
    }
}