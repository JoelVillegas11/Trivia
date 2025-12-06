// AdminChallengeAdapter.java
package com.project6electiva.trivia;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AdminChallengeAdapter extends RecyclerView.Adapter<AdminChallengeAdapter.ViewHolder> {

    private List<AdminChallengesActivity.Challenge> challenges;
    private Context context;
    private FirebaseFirestore db;

    public AdminChallengeAdapter(List<AdminChallengesActivity.Challenge> challenges, Context context) {
        this.challenges = challenges;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_challenge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminChallengesActivity.Challenge c = challenges.get(position);
        holder.tvTitle.setText(c.title);
        holder.tvDescription.setText(c.description);
        holder.tvReward.setText("Recompensa: " + c.rewardPoints + " pts");
        holder.tvTarget.setText("Meta: " + c.targetValue);

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminEditChallengeActivity.class);
            intent.putExtra("challengeId", c.id);
            intent.putExtra("title", c.title);
            intent.putExtra("description", c.description);
            intent.putExtra("rewardPoints", c.rewardPoints);
            intent.putExtra("targetValue", c.targetValue);
            intent.putExtra("challengeType", c.challengeType);
            intent.putExtra("categoryId", c.categoryId != null ? c.categoryId : "Todas");
            intent.putExtra("difficulty", c.difficulty != null ? c.difficulty : "Todas");
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar reto")
                    .setMessage("¿Está seguro de eliminar este reto?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        db.collection("challenges").document(c.id)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    challenges.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Reto eliminado", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvReward, tvTarget;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvReward = itemView.findViewById(R.id.tvReward);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}