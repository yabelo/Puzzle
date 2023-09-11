package com.example.puzzle;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.puzzle.Account.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameFragment extends Fragment {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private PuzzleGame puzzleGame;
    private long startTimeMillis;
    private long numOfMoves;
    private String userId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        surfaceView = rootView.findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        puzzleGame = new PuzzleGame();
        surfaceView.setOnTouchListener(puzzleGame);

        surfaceView.post(new Runnable() {
            @Override
            public void run() {
                puzzleGame.init(surfaceView.getWidth(), surfaceView.getHeight());
                puzzleGame.draw();
            }
        });

        return rootView;
    }


    private class PuzzleGame implements View.OnTouchListener {

        private int width, height;
        private Paint paint;
        private int[][] puzzleGrid; // Store the puzzle arrangement
        private int emptyRow, emptyColumn;

        private final Handler timerHandler = new Handler();
        private final Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (startTimeMillis > 0 && !checkIfGameCompleted()) {

                    draw();

                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        public void init(int width, int height) {
            this.width = width;
            this.height = height;

            numOfMoves = 0; // Initialize the move count

            // Initialize the puzzleGrid and other game variables here
            puzzleGrid = new int[3][3];
            shuffleGrid();

            paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(60);
        }


        // Inside PuzzleGame class
        public void draw() {
            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.parseColor("#808080"));

                int cellWidth = width / 3;
                int cellHeight = height / 3;

                for (int i = 0; i < puzzleGrid.length; i++) {
                    for (int j = 0; j < puzzleGrid[i].length; j++) {
                        int value = puzzleGrid[i][j];
                        float left = j * cellWidth;
                        float top = i * cellHeight;
                        float right = left + cellWidth;
                        float bottom = top + cellHeight;

                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(Color.TRANSPARENT);
                        canvas.drawRect(left, top, right, bottom, paint);

                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(8);
                        paint.setColor(Color.BLACK);
                        canvas.drawRect(left, top, right, bottom, paint);
                        paint.setStrokeWidth(0);

                        if (value != 0) {
                            paint.setStyle(Paint.Style.FILL);
                            paint.setColor(Color.BLACK);
                            paint.setTextSize(80);
                            String text = String.valueOf(value);
                            float x = j * cellWidth + cellWidth / 2 - paint.measureText(text) / 2;
                            float y = i * cellHeight + cellHeight / 2;
                            canvas.drawText(text, x, y, paint);
                        }

                    }
                }

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }


        private void handleCellClick(int row, int col) {
            if (checkIfCanMove(row, col)) {
                swapCells(row, col);
                emptyRow = row;
                emptyColumn = col;
                numOfMoves++; // Increment the move count
                // Redraw the puzzle grid
                draw();

                // Check for game completion
                if (checkIfGameCompleted()) {
                    long endTimeMillis = SystemClock.elapsedRealtime();
                    long elapsedTime = endTimeMillis - startTimeMillis;
                    long seconds = (elapsedTime / 1000) + 1;

                    showCompletionDialog(seconds, numOfMoves); // Show the completion dialog
                }
            }
        }



        private boolean checkIfGameCompleted() {
            for (int i = 0; i < puzzleGrid.length; i++) {
                for (int j = 0; j < puzzleGrid[i].length; j++) {
                    if (i == puzzleGrid.length - 1 && j == puzzleGrid[i].length - 1) {
                        return puzzleGrid[i][j] == 0;
                    } else if (puzzleGrid[i][j] != i * puzzleGrid[i].length + j + 1) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean checkIfCanMove(int row, int col) {
            if (checkIfGameCompleted()) return false;
            return ((Math.abs(row - emptyRow) == 1 && col == emptyColumn) ||
                    (row == emptyRow && Math.abs(col - emptyColumn) == 1));
        }

        private void swapCells(int row, int col) {
            int temp = puzzleGrid[row][col];
            puzzleGrid[row][col] = puzzleGrid[emptyRow][emptyColumn];
            puzzleGrid[emptyRow][emptyColumn] = temp;
        }

        private void shuffleGrid() {
            ArrayList<Integer> values = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                values.add(i);
            }
            Collections.shuffle(values);

            int index = 0;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    puzzleGrid[i][j] = values.get(index++);
                }
            }
            final byte[] state = new byte[9];

            for (int i = 0; i < 9; i++) {
                state[i] = Byte.valueOf(String.valueOf(values.get(i)));
            }

            if (!isSolvable(state)) {
                shuffleGrid();
            }

            findEmptyCell();
        }

        public boolean isSolvable(final byte[] state) {
            final int numOfTiles = state.length,
                    dim = (int) Math.sqrt(numOfTiles);
            int inversions = 0;

            for (int i = 0; i < numOfTiles; ++i) {
                final byte iTile = state[i];
                if (iTile != 0) {
                    for (int j = i + 1; j < numOfTiles; ++j) {
                        final byte jTile = state[j];
                        if (jTile != 0 && jTile < iTile) {
                            ++inversions;
                        }
                    }
                } else {
                    if ((dim & 0x1) == 0) {
                        inversions += (1 + i / dim);
                    }
                }
            }
            if ((inversions & 0x1) == 1) return false;
            return true;
        }


        private void findEmptyCell() {
            for (int i = 0; i < puzzleGrid.length; i++) {
                for (int j = 0; j < puzzleGrid[i].length; j++) {
                    if (puzzleGrid[i][j] == 0) {
                        emptyRow = i;
                        emptyColumn = j;
                        return;
                    }
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int touchX = (int) motionEvent.getX();
            int touchY = (int) motionEvent.getY();

            int cellWidth = width / 3;
            int cellHeight = height / 3;

            int row = touchY / cellHeight;
            int col = touchX / cellWidth;

            if (puzzleGrid[emptyRow][emptyColumn] == 0 && startTimeMillis == 0) {
                startTimeMillis = SystemClock.elapsedRealtime();
                // Start the timer thread
                timerHandler.postDelayed(timerRunnable, 0);
            }

            handleCellClick(row, col);

            return false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Reset the timer values
        startTimeMillis = 0;
    }

    private void showCompletionDialog(long seconds, long numOfMoves) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Congratulations!");

        String completionMessage = "You've completed this puzzle in " + seconds + " seconds and " + numOfMoves + " moves.";

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(); // Get the current user's ID

        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> userData = task.getResult().getData();

                        if (userData != null) {
                            long bestTime = UserAccount.bestTime;
                            long bestMoves = UserAccount.bestMoves;

                            String modifiedCompletionMessage = completionMessage; // Create a final copy

                            if (bestTime < 0 && bestMoves < 0) {
                                modifiedCompletionMessage += "\n\nNew best time!";
                                modifiedCompletionMessage += "\nNew best moves!";

                                String bestTimeMessage = (bestTime == -1) ? "N/A" : seconds + " seconds";
                                String bestMovesMessage = (bestMoves == -1) ? "N/A" : numOfMoves + " moves";

                                modifiedCompletionMessage += "\n\nBest time: " + bestTimeMessage + "\nBest moves: " + bestMovesMessage;

                                updateUserStats(userId, seconds, numOfMoves);
                            } else {
                                if (seconds < bestTime) {
                                    modifiedCompletionMessage += "\n\nNew best time!";
                                    updateUserBestTime(userId, seconds);
                                }
                                if (numOfMoves < bestMoves) {
                                    modifiedCompletionMessage += "\n\nNew best moves!";
                                    updateUserBestMoves(userId, numOfMoves);
                                } else {
                                    String bestTimeMessage = (bestTime == -1) ? "N/A" : bestTime + " seconds";
                                    String bestMovesMessage = (bestMoves == -1) ? "N/A" : bestMoves + " moves";

                                    modifiedCompletionMessage += "\n\nBest time: " + bestTimeMessage + "\nBest moves: " + bestMovesMessage;
                                }
                            }

                            final String finalCompletionMessage = modifiedCompletionMessage; // Final copy for lambda usage

                            builder.setMessage(finalCompletionMessage);

                            builder.setNeutralButton("Back to start!", (dialog, which) -> {
                                dialog.dismiss();
                                // Replace the GameFragment with the StartFragment
                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, new StartFragment());
                                transaction.addToBackStack(null);
                                transaction.commit();
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
    }



    private void updateUserStats(String userId, long newBestTime, long newBestMoves) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("bestTime", newBestTime);
        userData.put("bestMoves", newBestMoves);

        db.collection("users")
                .document(userId)
                .update(userData);

        UserAccount.bestTime = newBestTime;
        UserAccount.bestMoves = newBestMoves;
    }

    private void updateUserBestTime(String userId, long newBestTime) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("bestTime", newBestTime);

        db.collection("users")
                .document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                })
                .addOnFailureListener(e -> {
                    // Update failed
                });

        UserAccount.bestTime = newBestTime;
    }

    private void updateUserBestMoves(String userId, long newBestMoves) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("bestMoves", newBestMoves);

        db.collection("users")
                .document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                })
                .addOnFailureListener(e -> {
                    // Update failed
                });
        UserAccount.bestMoves = newBestMoves;
    }


}
