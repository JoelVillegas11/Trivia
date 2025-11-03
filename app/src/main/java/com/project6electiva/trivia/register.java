package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {

    private EditText etName, etEmailRegister, etPasswordRegister;
    private TextInputLayout tilPassword;
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmailRegister = findViewById(R.id.etEmailRegister);
        etPasswordRegister = findViewById(R.id.etPasswordRegister);
        tilPassword = findViewById(R.id.tilPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        tilPassword.setEndIconOnClickListener(v -> togglePasswordVisibility());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPasswordRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            tilPassword.setEndIconDrawable(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            etPasswordRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            tilPassword.setEndIconDrawable(R.drawable.ic_visibility);
            isPasswordVisible = true;
        }
        etPasswordRegister.setSelection(etPasswordRegister.getText().length());
    }

    // ... resto del código igual
    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmailRegister.getText().toString().trim();
        String password = etPasswordRegister.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("role", "user");
                        user.put("points", 0);
                        user.put("createdAt", Timestamp.now());

                        db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, login.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al guardar perfil", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}