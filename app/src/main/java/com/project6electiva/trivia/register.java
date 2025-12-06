// register.java
package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

public class register extends AppCompatActivity {

    private EditText etName, etEmailRegister, etPasswordRegister;
    private TextInputLayout tilPassword, tilEmail;
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Lista de dominios permitidos
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
            "gmail.com",
            "outlook.com",
            "hotmail.com",
            "yahoo.com",
            "protonmail.com"
    );

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
        tilEmail = findViewById(R.id.tilEmail); // Asegúrate de tener este ID en XML
        Button btnRegister = findViewById(R.id.btnRegister);

        // Mostrar menú de dominios al escribir "@"
        etEmailRegister.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().endsWith("@")) {
                    showDomainMenu();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tilPassword.setEndIconOnClickListener(v -> togglePasswordVisibility());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void showDomainMenu() {
        PopupMenu popup = new PopupMenu(this, etEmailRegister);
        for (String domain : ALLOWED_DOMAINS) {
            popup.getMenu().add(domain);
        }
        popup.setOnMenuItemClickListener(item -> {
            String selectedDomain = item.getTitle().toString();
            String current = etEmailRegister.getText().toString();
            etEmailRegister.setText(current + selectedDomain);
            etEmailRegister.setSelection(etEmailRegister.getText().length());
            return true;
        });
        popup.show();
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

    private boolean isValidEmail(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String[] parts = email.split("@", -1);
        if (parts.length != 2) return false;

        String domain = parts[1].toLowerCase();
        return ALLOWED_DOMAINS.contains(domain);
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmailRegister.getText().toString().trim();
        String password = etPasswordRegister.getText().toString().trim();

        // Validar campos
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.length() > 13) {
            Toast.makeText(this, "El nombre no puede tener más de 13 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Correo no válido. Usa dominios permitidos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        // ❌ NO GUARDAR "email" en Firestore
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
                                    // 🔥 MUESTRA EL ERROR REAL PARA DEPURAR
                                    Toast.makeText(this, "Error al guardar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    e.printStackTrace(); // Imprime el stack trace en Logcat
                                });
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}