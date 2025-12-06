//game_mode.java
package com.project6electiva.trivia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

// Cambiamos AppCompatActivity por BaseActivity
public class game_mode extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llama al onCreate de BaseActivity para inicializar el NetworkReceiver
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);

        // 💡 VERIFICACIÓN INICIAL FORZADA:
        // Si la actividad se crea sin conexión, llamar inmediatamente a handleNoNetwork()
        // para mostrar el AlertDialog y evitar que el usuario intente jugar.
        if (!NetworkReceiver.isConnected) {
            handleNoNetwork();
            // Retornar aquí para evitar inicializar listeners que no se usarán
            return;
        }

        Button btnSolo = findViewById(R.id.btnSolo);

        // El botón Solo ahora verifica la conexión
        btnSolo.setOnClickListener(v -> {
            // Se realiza una verificación de seguridad justo antes de avanzar
            if (NetworkReceiver.isConnected) {
                startActivity(new Intent(game_mode.this, categories.class));
            } else {
                // Esta línea ahora es un fallback, pero handleNoNetwork() es la acción principal
                Toast.makeText(game_mode.this, "¡Error! Necesitas conexión para seleccionar categorías.", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón "En línea" se omite por ahora (no funcional)
    }

    /**
     * Sobrescribe el método de BaseActivity. Se llama cuando se pierde la conexión
     * o al iniciar la actividad sin red (gracias a la verificación en onCreate/onResume de BaseActivity).
     */
    @Override
    protected void handleNoNetwork() {
        // Detiene cualquier proceso de la actividad (aunque aquí no hay procesos largos)

        // Comprobación de seguridad para evitar que el diálogo se abra dos veces si se está cerrando
        if (isFinishing() || isDestroyed()) {
            return;
        }

        // Mostrar AlertDialog y redirigir al menú principal (MainActivity)
        new AlertDialog.Builder(this)
                .setTitle("Conexión Perdida")
                .setMessage("Se ha detectado la pérdida de conexión. Regresando al menú principal.")
                .setCancelable(false) // No permite cerrar sin acción
                .setPositiveButton("OK", (dialog, id) -> {
                    // Redirigir a MainActivity y finalizar esta actividad
                    Intent intent = new Intent(game_mode.this, MainActivity.class);
                    // Flags para limpiar la pila de actividades
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}