package com.example.puzzle.NavBar.Leaderboard;

public class LeaderboardPlayer {
    private String userId;
    private String username;
    private long bestTime;
    private long bestMoves;

    public LeaderboardPlayer(String userId, String username, long bestTime, long bestMoves) {
        this.userId = userId;
        this.username = username;
        this.bestTime = bestTime;
        this.bestMoves = bestMoves;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getBestTime() {
        return bestTime;
    }

    public void setBestTime(long bestTime) {
        this.bestTime = bestTime;
    }

    public long getBestMoves() {
        return bestMoves;
    }

    public void setBestMoves(long bestMoves) {
        this.bestMoves = bestMoves;
    }
}
