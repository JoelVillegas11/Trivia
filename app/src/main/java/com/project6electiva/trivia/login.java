// login.java (completo y actualizado)
package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private EditText etEmail, etPassword;
    private TextInputLayout tilPassword;
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // ¡Importante!
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etEmail = findViewById(R.id.etEmailRegister);
        etPassword = findViewById(R.id.etPasswordRegister);
        tilPassword = findViewById(R.id.tilPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnGoogleLogin = findViewById(R.id.btnGoogleLogin); // Nuevo botón
        TextView tvRegister = findViewById(R.id.tvRegister);

        tilPassword.setEndIconOnClickListener(v -> togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> loginUser());
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(login.this, register.class)));
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            tilPassword.setEndIconDrawable(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            tilPassword.setEndIconDrawable(R.drawable.ic_visibility);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkOrCreateUserProfile(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(login.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // === GOOGLE SIGN-IN ===
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("GoogleLogin", "Error en Google Sign-In", e);
                Toast.makeText(this, "Error al iniciar con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkOrCreateUserProfile(user);
                    } else {
                        Log.w("GoogleAuth", "signInWithCredential:failure", task.getException());
                        Toast.makeText(login.this, "Autenticación con Google fallida", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // === CREAR PERFIL EN FIRESTORE SI NO EXISTE ===
    private void checkOrCreateUserProfile(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();
        String name = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();

        // ✅ Crear una variable final para usar en la lambda
        String finalName;
        if (name == null || name.isEmpty()) {
            finalName = (email != null && email.contains("@")) ? email.split("@")[0] : "Usuario";
        } else {
            finalName = name;
        }

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        startActivity(new Intent(login.this, MainActivity.class));
                        finish();
                    } else {
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", finalName); // ✅ Ahora es final/effectively final
                        user.put("email", email);
                        user.put("role", "user");
                        user.put("points", 0);
                        user.put("createdAt", com.google.firebase.Timestamp.now());

                        db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    startActivity(new Intent(login.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(login.this, "Error al crear perfil", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(login.this, "Error al verificar perfil", Toast.LENGTH_SHORT).show();
                });
    }
}