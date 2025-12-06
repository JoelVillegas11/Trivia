// GameHistoryAdapter.java
package com.project6electiva.trivia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class GameHistoryAdapter extends RecyclerView.Adapter<GameHistoryAdapter.ViewHolder> {

    private List<GameRecord> records;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GameRecord record);
    }

    public GameHistoryAdapter(List<GameRecord> records, OnItemClickListener listener) {
        this.records = records;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRecord record = records.get(position);

        // Categoría
        String category = getCategoryDisplayName(record.category);
        holder.tvCategory.setText(category);

        // Dificultad
        String difficulty = getDifficultyDisplayName(record.difficulty);
        holder.tvDifficulty.setText(difficulty);

        // Puntaje
        holder.tvScore.setText(record.score + "/" + record.totalQuestions);

        // Fecha y hora
        if (record.timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            String dateStr = sdf.format(record.timestamp.toDate());
            holder.tvDateTime.setText(dateStr);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDifficulty, tvScore, tvDateTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
        }
    }

    private String getCategoryDisplayName(String categoryId) {
        switch (categoryId) {
            case "movies": return "Películas";
            case "music": return "Música";
            case "science": return "Ciencia";
            case "sports": return "Deportes";
            default: return categoryId;
        }
    }

    private String getDifficultyDisplayName(String difficulty) {
        switch (difficulty) {
            case "facil": return "Fácil";
            case "normal": return "Normal";
            case "dificil": return "Difícil";
            default: return difficulty;
        }
    }
}