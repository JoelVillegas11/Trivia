// MainActivity.java
package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem; // Añadir para el listener del menú
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.PopupMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView tvNombreUsuario;
    private ImageView ivEmblema;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final int[] EMBLEMAS = {
            R.drawable.emblema1,
            R.drawable.emblema2,
            R.drawable.emblema3,
            R.drawable.emblema4,
            R.drawable.emblema5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> showPopupMenu(v));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        ivEmblema = findViewById(R.id.ivEmblema);
        Button btnJugar = findViewById(R.id.btnJugar);

        cargarPerfil();
        btnJugar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, game_mode.class));
        });
    }

    // Método para mostrar el menú
    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Cerrar sesión"); // Solo una opción por ahora

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Cerrar sesión")) {
                // Cerrar sesión en Firebase
                FirebaseAuth.getInstance().signOut();
                // Volver al login
                startActivity(new Intent(MainActivity.this, login.class));
                finish();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void cargarPerfil() {
        String uid = mAuth.getUid();
        if (uid == null) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        tvNombreUsuario.setText(name != null ? name : "Usuario");

                        // Asignar emblema aleatorio (solo una vez al iniciar sesión)
                        // Si ya se guardó un emblema en Firestore, úsalo; si no, genera uno
                        Long emblemaIndex = doc.getLong("emblemaIndex");
                        if (emblemaIndex == null) {
                            int index = new Random().nextInt(EMBLEMAS.length);
                            ivEmblema.setImageResource(EMBLEMAS[index]);
                            // Opcional: guardar el emblema asignado para futuras sesiones
                            db.collection("users").document(uid)
                                    .update("emblemaIndex", (long) index);
                        } else {
                            int index = emblemaIndex.intValue();
                            if (index >= 0 && index < EMBLEMAS.length) {
                                ivEmblema.setImageResource(EMBLEMAS[index]);
                            } else {
                                ivEmblema.setImageResource(EMBLEMAS[0]);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                });
    }
}