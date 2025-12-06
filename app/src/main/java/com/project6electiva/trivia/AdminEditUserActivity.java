// AdminEditUserActivity.java
package com.project6electiva.trivia;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminEditUserActivity extends AppCompatActivity {

    private EditText etName, etPoints;
    private Spinner spRole;
    private Button btnSave;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_user);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etPoints = findViewById(R.id.etPoints);
        spRole = findViewById(R.id.spRole);
        btnSave = findViewById(R.id.btnSave);

        // Configurar spinner de roles
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(roleAdapter);

        userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            loadUserData();
        }

        btnSave.setOnClickListener(v -> saveUser());
    }

    private void loadUserData() {
        etName.setText(getIntent().getStringExtra("name"));
        etPoints.setText(String.valueOf(getIntent().getLongExtra("points", 0)));

        String role = getIntent().getStringExtra("role");
        setSpinnerSelection(spRole, role);
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

    private void saveUser() {
        String name = etName.getText().toString().trim();
        long points = Long.parseLong(etPoints.getText().toString().trim());
        String role = spRole.getSelectedItem().toString();

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("role", role);
        user.put("points", points);

        db.collection("users").document(userId)
                .update(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}