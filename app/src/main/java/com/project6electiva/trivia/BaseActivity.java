// BaseActivity.java
package com.project6electiva.trivia;

import android.app.AlertDialog;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkReceiver = new NetworkReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registrar el receiver
        registerReceiver(networkReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        // Verificar estado inicial
        if (!NetworkReceiver.isConnected) {
            handleNoNetwork();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desregistrar el receiver
        unregisterReceiver(networkReceiver);
    }

    /**
     * Método que se llama cuando se detecta que no hay red.
     * Se sobrescribe en cada actividad para comportamiento específico.
     */
    protected abstract void handleNoNetwork();
}