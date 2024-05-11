package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameSession implements Runnable {
    private List<Player> players = new ArrayList<>();
    private String[][] questionsAndAnswers = {
            {"What is 10 + 5?", "15"},
            {"What is 20 - 8?", "12"},
            {"What is 7 * 3?", "21"},
            {"What is 40 / 8?", "5"},
            {"What is 15 + 7?", "22"},
            {"What is 30 - 9?", "21"},
            {"What is 6 * 7?", "42"},
            {"What is 50 / 10?", "5"},
            {"What is 9 + 6?", "15"},
            {"What is 8 * 12?", "96"}
    };

    public void addPlayer(Player player) {
        players.add(player);
    }

    @Override
    public void run() {
        try {
            for (Player player : players) {
                player.setupStreams();
            }

            // Game logic with questions and answers
            for (String[] qa : questionsAndAnswers) {
                broadcast("QUESTION " + qa[0]);  // Broadcast question
                collectAnswers();
                for (Player player : players) {
                    if (player.getAnswer().equals(qa[1])) {  // Check if answer is correct
                        player.addScore(10);  // Award points
                    }
                }
            }

            announceWinner();

            // Cleanup
            for (Player player : players) {
                player.closeConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message) throws IOException {
        for (Player player : players) {
            player.getOutput().println(message);
        }
    }

    private void collectAnswers() throws IOException {
        for (Player player : players) {
            String answer = player.getInput().readLine();
            player.setAnswer(answer);
        }
    }

    private void announceWinner() throws IOException {
        Player winner = players.stream().max((p1, p2) -> Integer.compare(p1.getScore(), p2.getScore())).orElse(null);
        if (winner != null) {
            System.out.println("Winner is " + winner.getNickname() + " with a score of " + winner.getScore());
            for (Player player : players) {
                if (player == winner) {
                    player.getOutput().println("GAME OVER: WIN");
                } else {
                    player.getOutput().println("GAME OVER: LOSE");
                }
            }
        } else {
            // In case of a tie or no winner
            for (Player player : players) {
                player.getOutput().println("GAME OVER: DRAW");
            }
        }
    }
}
