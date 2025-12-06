// UserChallengeAdapter.java
package com.project6electiva.trivia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserChallengeAdapter extends RecyclerView.Adapter<UserChallengeAdapter.ViewHolder> {

    private List<UserChallengesActivity.ChallengeItem> challenges;

    public UserChallengeAdapter(List<UserChallengesActivity.ChallengeItem> challenges) {
        this.challenges = challenges;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_challenge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserChallengesActivity.ChallengeItem item = challenges.get(position);
        holder.tvTitle.setText(item.challenge.title);
        holder.tvDescription.setText(item.challenge.description);
        holder.tvReward.setText("+" + item.challenge.rewardPoints + " pts");

        if (item.isCompleted) {
            holder.tvStatus.setText("Completado");
            holder.tvStatus.setTextColor(0xFF4CAF50); // Verde
        } else {
            holder.tvStatus.setText("Disponible");
            holder.tvStatus.setTextColor(0xFFFF9800); // Naranja
        }
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvReward;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvChallengeTitle);
            tvDescription = itemView.findViewById(R.id.tvChallengeDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReward = itemView.findViewById(R.id.tvReward);
        }
    }
}