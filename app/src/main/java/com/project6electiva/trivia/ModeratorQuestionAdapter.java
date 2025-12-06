// ModeratorQuestionAdapter.java
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

public class ModeratorQuestionAdapter extends RecyclerView.Adapter<ModeratorQuestionAdapter.ViewHolder> {

    private List<ModeratorQuestionsActivity.Question> questions;
    private Context context;
    private FirebaseFirestore db;

    public ModeratorQuestionAdapter(List<ModeratorQuestionsActivity.Question> questions, Context context) {
        this.questions = questions;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_moderator_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModeratorQuestionsActivity.Question q = questions.get(position);
        holder.tvQuestion.setText(q.text);
        holder.tvCategory.setText(getCategoryName(q.categoryId));
        holder.tvDifficulty.setText(getDifficultyName(q.difficulty));

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, ModeratorEditQuestionActivity.class);
            intent.putExtra("questionId", q.id);
            intent.putExtra("questionText", q.text);
            intent.putExtra("categoryId", q.categoryId);
            intent.putExtra("difficulty", q.difficulty);
            intent.putExtra("options", q.options.toArray(new String[0]));
            intent.putExtra("correctIndex", q.correctIndex);
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar pregunta")
                    .setMessage("¿Está seguro de eliminar esta pregunta?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        db.collection("questions").document(q.id)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    questions.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Pregunta eliminada", Toast.LENGTH_SHORT).show();
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
        return questions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvCategory, tvDifficulty;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private String getCategoryName(String categoryId) {
        switch (categoryId) {
            case "movies": return "Películas";
            case "music": return "Música";
            case "science": return "Ciencia";
            case "sports": return "Deportes";
            default: return categoryId;
        }
    }

    private String getDifficultyName(String difficulty) {
        switch (difficulty) {
            case "facil": return "Fácil";
            case "normal": return "Normal";
            case "dificil": return "Difícil";
            default: return difficulty;
        }
    }
}