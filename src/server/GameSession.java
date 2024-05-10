package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameSession implements Runnable {
    private List<Player> players = new ArrayList<>();

    public void addPlayer(Player player) {
        players.add(player);
    }

    @Override
    public void run() {
        try {
            for (Player player : players) {
                player.setupStreams();
            }

            // Simple game logic example
            for (int i = 0; i < 5; i++) { // 5 rounds
                broadcast("QUESTION What is 2 + 2?");
                collectAnswers();
                for (Player player : players) {
                    if (player.getAnswer().equals("4")) {
                        player.addScore(10);
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

    private void announceWinner() {
        Player winner = players.stream().max((p1, p2) -> Integer.compare(p1.getScore(), p2.getScore())).orElse(null);
        if (winner != null) {
            System.out.println("Winner is " + winner.getNickname() + " with a score of " + winner.getScore());
        }
    }
}
