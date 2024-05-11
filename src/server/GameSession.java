package server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameSession implements Runnable {
    private List<Player> players = new ArrayList<>();
    private String[][] questionsAndAnswers = {
            {"What is 10 + 5?", "15"},
            {"What is 20 - 8?", "12"},
//            {"What is 7 * 3?", "21"},
//            {"What is 40 / 8?", "5"},
//            {"What is 15 + 7?", "22"},
//            {"What is 30 - 9?", "21"},
//            {"What is 6 * 7?", "42"},
//            {"What is 50 / 10?", "5"},
//            {"What is 9 + 6?", "15"},
//            {"What is 8 * 12?", "96"}
    };

    public synchronized void addPlayer(Player player) {
        players.add(player);
    }

    public synchronized List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public synchronized void startGame() throws IOException {
        broadcast("GAME_STARTED");
        run();
    }



    @Override
    public void run() {
        try {
            // Start the game
            for (String[] qa : questionsAndAnswers) {
                broadcast("QUESTION " + qa[0]);
                collectAnswers();
                for (Player player : players) {
                    if (player.getAnswer().equals(qa[1])) {
                        player.addScore(10);
                    }
                }
            }
            announceWinner();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanup();
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
            for (Player player : players) {
                player.getOutput().println(player == winner ? "GAME OVER: WIN" : "GAME OVER: LOSE");
            }
        } else {
            broadcast("GAME OVER: DRAW");
        }
    }

    private void cleanup() {
        for (Player player : players) {
            try {
                player.closeConnection();
            } catch (IOException e) {
                System.out.println("Error closing player connection: " + e.getMessage());
            }
        }
    }
}
