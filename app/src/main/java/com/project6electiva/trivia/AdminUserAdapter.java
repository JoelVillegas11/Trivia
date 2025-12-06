// AdminUserAdapter.java
package com.project6electiva.trivia;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private static final String TAG = "AdminUserAdapter";
    private List<AdminUsersActivity.UserItem> users;
    private Context context;
    private FirebaseFirestore db;

    public AdminUserAdapter(List<AdminUsersActivity.UserItem> users, Context context) {
        this.users = users;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 🛑 CORRECCIÓN DE SINTAXIS: Usar R.layout.item_admin_user
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUsersActivity.UserItem u = users.get(position);
        holder.tvName.setText(u.name);
        holder.tvEmail.setText(u.email != null ? u.email : "");
        holder.tvRole.setText("Rol: " + u.role);

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminEditUserActivity.class);
            intent.putExtra("userId", u.id);
            intent.putExtra("name", u.name);
            intent.putExtra("email", u.email);
            intent.putExtra("role", u.role);
            intent.putExtra("points", u.points);
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar usuario")
                    .setMessage("¿Está seguro de eliminar a " + u.name + "?\nSe eliminarán todos sus datos.")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Iniciar la eliminación de datos de Firestore
                        deleteAllUserData(u.id, position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void deleteAllUserData(String userId, int position) {
        // 1. Eliminar subcolección gameHistory (asíncrono y recursivo)
        deleteCollection(db.collection("users").document(userId).collection("gameHistory"), 50)
                .addOnSuccessListener(aVoid -> {
                    // 2. Eliminar el documento del usuario (perfil)
                    db.collection("users").document(userId).delete()
                            .addOnSuccessListener(aVoid2 -> {
                                // ⚠️ NOTA: La eliminación de Firebase Auth debe hacerse desde Cloud Functions.
                                // Aquí solo actualizamos la vista local.
                                users.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al eliminar documento del perfil", e);
                                Toast.makeText(context, "Error al eliminar perfil", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Si falla el primer paso (historial), muestra error.
                    Log.e(TAG, "Error al eliminar historial", e);
                    Toast.makeText(context, "Error al borrar historial", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Elimina una colección completa en lotes, usando un patrón recursivo de Task.
     * @return Task<Void> que se completa cuando todos los documentos han sido eliminados.
     */
    private Task<Void> deleteCollection(CollectionReference collection, int batchSize) {
        // Llama a la función de lotes que manejará la recursividad.
        return deleteQueryBatch(collection.limit(batchSize));
    }

    /**
     * Procesa un lote de documentos y, si quedan más, llama recursivamente para el siguiente lote.
     */
    private Task<Void> deleteQueryBatch(Query query) {
        return query.get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        // Si la consulta inicial falla, lanzar la excepción
                        throw task.getException();
                    }

                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot.isEmpty()) {
                        // Caso Base: No hay más documentos. Devolver éxito.
                        return Tasks.forResult(null);
                    }

                    // 1. Crear el lote de eliminación
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }

                    // 2. Comprometer el lote actual
                    Task<Void> commitTask = batch.commit();

                    // 3. Encadenar: Después de comprometer, llamar a deleteQueryBatch de nuevo
                    // para el siguiente lote, reiniciando la consulta (RECURSIVIDAD).
                    return commitTask.continueWithTask(task2 -> deleteQueryBatch(query));
                });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}