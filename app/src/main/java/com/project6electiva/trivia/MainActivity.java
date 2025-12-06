//MainActivity.java
package com.project6electiva.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;
import java.util.HashMap;


// 1. EXTENDER BASEACTIVITY
public class MainActivity extends BaseActivity {

    private TextView tvNombreUsuario;
    private TextView tvTotalScore;
    private ImageView ivEmblema;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button btnJugar; // Declaramos el botón como variable de clase

    private static final int[] EMBLEMAS = {
            R.drawable.emblema1,
            R.drawable.emblema2,
            R.drawable.emblema3,
            R.drawable.emblema4,
            R.drawable.emblema5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // IMPORTANTÍSIMO: Llama primero a super.onCreate() para inicializar BaseActivity y el NetworkReceiver.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(this::showPopupMenu);
        tvTotalScore = findViewById(R.id.tvTotalScore);
        ImageButton btnRetos = findViewById(R.id.btnRetos);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        ivEmblema = findViewById(R.id.ivEmblema);
        btnJugar = findViewById(R.id.btnJugar); // Inicializamos la variable de clase

        cargarPerfil();

        btnRetos.setOnClickListener(v -> {
            if (NetworkReceiver.isConnected) {
                startActivity(new Intent(MainActivity.this, UserChallengesActivity.class));
            } else {
                Toast.makeText(MainActivity.this, "Se requiere conexión para ver los retos.", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. LÓGICA DE VERIFICACIÓN DE RED EN EL BOTÓN "JUGAR"
        btnJugar.setOnClickListener(v -> {
            if (NetworkReceiver.isConnected) {
                // Si hay red, iniciar el juego
                startActivity(new Intent(MainActivity.this, game_mode.class));
            } else {
                // Si no hay red, mostrar el Toast (o llamar a handleNoNetwork())
                Toast.makeText(MainActivity.this, "¡Atención! Se requiere conexión para jugar en línea.", Toast.LENGTH_SHORT).show();
                // Opcional: Podrías habilitar aquí la opción de jugar en modo local, si la implementas.
            }
        });
    }

    // 3. IMPLEMENTAR MÉTODO REQUERIDO POR BASEACTIVITY
    @Override
    protected void handleNoNetwork() {
        // Aquí no hacemos nada (el MainActivity puede estar sin red)
        // La lógica de bloqueo/aviso ahora está en el OnClickListener del botón Jugar
        Toast.makeText(this, "Modo sin conexión. Inicie sesión para actualizar perfil.", Toast.LENGTH_SHORT).show();
    }

    // Método para mostrar el menú
    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        // 💡 AÑADIR LA NUEVA OPCIÓN DE MENÚ
        popup.getMenu().add("Historial de Juegos");
        popup.getMenu().add("Tabla de Posiciones");
        popup.getMenu().add("Cerrar sesión");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Cerrar sesión")) {
                // Lógica existente para cerrar sesión
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, login.class));
                finish();
                return true;
            } else if (item.getTitle().equals("Historial de Juegos")) {
                // 💡 VERIFICACIÓN AÑADIDA: Asegurar conexión ANTES de ir al historial
                if (mAuth.getCurrentUser() != null && NetworkReceiver.isConnected) {
                    startActivity(new Intent(MainActivity.this, game_history.class));
                } else { // Mostrar el Toast de error de conexión para evitar que se lance la actividad sin red.
                    Toast.makeText(MainActivity.this, "Se requiere conexión para ver el historial.", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (item.getTitle().equals("Tabla de Posiciones")) {
                if (NetworkReceiver.isConnected) {
                    startActivity(new Intent(MainActivity.this, RankingActivity.class));
                } else {
                    Toast.makeText(MainActivity.this, "Se requiere conexión para ver el ranking.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void cargarPerfil() {
        // ... (Tu código de cargarPerfil es funcional)
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

                        // 💡 Mostrar el puntaje total
                        Long totalScore = doc.getLong("points");
                        tvTotalScore.setText((totalScore != null ? totalScore : 0) + " pts");
                    }
                    if (doc.get("gamesPlayedByCategory") == null) {
                        db.collection("users").document(uid)
                                .update("gamesPlayedByCategory", new HashMap<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                });
    }
}