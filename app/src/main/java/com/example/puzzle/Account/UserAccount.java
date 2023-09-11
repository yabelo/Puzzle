package com.example.puzzle.Account;

public class UserAccount {
    public static String userId;
    public static String username;
    public static long bestTime;
    public static long bestMoves;

    public UserAccount(String userId, String username, long bestTime, long bestMoves) {
        UserAccount.userId = userId;
        UserAccount.username = username;
        UserAccount.bestTime = bestTime;
        UserAccount.bestMoves = bestMoves;
    }
}
