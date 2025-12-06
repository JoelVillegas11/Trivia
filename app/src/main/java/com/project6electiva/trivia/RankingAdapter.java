// RankingAdapter.java
package com.project6electiva.trivia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<RankingItem> items;
    private static final int[] EMBLEMAS = {
            R.drawable.emblema1,
            R.drawable.emblema2,
            R.drawable.emblema3,
            R.drawable.emblema4,
            R.drawable.emblema5
    };

    public RankingAdapter(List<RankingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RankingItem item = items.get(position);
        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvName.setText(item.name);
        holder.tvPoints.setText(item.points + " pts");

        // Asignar emblema (usa el índice guardado o uno por defecto)
        int emblemaIndex = (int) (item.emblemaIndex != null ? item.emblemaIndex : 0L);
        if (emblemaIndex >= 0 && emblemaIndex < EMBLEMAS.length) {
            holder.ivEmblema.setImageResource(EMBLEMAS[emblemaIndex]);
        } else {
            holder.ivEmblema.setImageResource(EMBLEMAS[0]);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition, tvName, tvPoints;
        ImageView ivEmblema;

        ViewHolder(View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvName = itemView.findViewById(R.id.tvName);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            ivEmblema = itemView.findViewById(R.id.ivEmblema);
        }
    }

    public static class RankingItem {
        public String name;
        public Long points;
        public Long emblemaIndex;

        public RankingItem(String name, Long points, Long emblemaIndex) {
            this.name = name;
            this.points = points;
            this.emblemaIndex = emblemaIndex;
        }
    }
}