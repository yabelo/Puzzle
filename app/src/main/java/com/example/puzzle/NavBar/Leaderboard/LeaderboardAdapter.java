package com.example.puzzle.NavBar.Leaderboard;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puzzle.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<LeaderboardPlayer> players;
    public static LeaderboardMode mode = LeaderboardMode.TIME;

    public LeaderboardAdapter(List<LeaderboardPlayer> players) {
        this.players = players;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLeaderboardData(List<LeaderboardPlayer> players) {
        this.players = players;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardPlayer player = players.get(position);
        holder.bind(player);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameAndBestTextView;

        ViewHolder(View itemView) {
            super(itemView);
            usernameAndBestTextView = itemView.findViewById(R.id.usernameAndBestTextView);
        }

        void bind(LeaderboardPlayer player) {
            String username = player.getUsername();

            String best = "";
            if(mode == LeaderboardMode.TIME){
                best = (player.getBestTime() == -1) ? "N/A" : String.valueOf(player.getBestTime());
            }
            else{
                best = (player.getBestMoves() == -1) ? "N/A" : String.valueOf(player.getBestMoves());
            }

            String formattedText;
            formattedText = username + "     " + best;
            usernameAndBestTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

            usernameAndBestTextView.setText(formattedText);
        }


    }


}
