package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameSession implements Runnable {
    private int currentQuestionIndex = 0;
    private final List<Player> players = new ArrayList<>();
    private final ConcurrentHashMap<Player, String> answers = new ConcurrentHashMap<>();
    private final String[][] questionsAndAnswers = {
            {"What is 10 + 5?", "15"},
            {"What is 20 - 8?", "12"},
            // Additional questions can be uncommented or added here.
    };

    private Player creator;

    public synchronized void setCreator(Player creator) {
        this.creator = creator;
    }

    public Player getCreator() {
        return creator;
    }

    public synchronized void addPlayer(Player player) {
        players.add(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void startGame() throws IOException {
        broadcast("GAME_STARTED");
        new Thread(this).start(); // Use a separate thread to run the game logic.
    }

    @Override
    public void run() {
        try {
            for (currentQuestionIndex = 0; currentQuestionIndex < questionsAndAnswers.length; currentQuestionIndex++) {
                String question = questionsAndAnswers[currentQuestionIndex][0];
                broadcast("QUESTION " + question);
                Thread.sleep(20000); // Wait for answers for 20 seconds.

                // Process answers
                answers.forEach((player, answer) -> {
                    try {
                        processAnswer(player, answer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                answers.clear(); // Clear answers after each question
            }
            announceWinner();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void processAnswer(Player player, String answer) throws IOException {  // Changed from private to public
        player.setAnswer(answer);
        String correctAnswer = questionsAndAnswers[currentQuestionIndex][1];
        if (answer.equals(correctAnswer)) {
            player.addScore(10);
            broadcast(player.getNickname() + " has answered correctly!");
        }
    }

    private void broadcast(String message) throws IOException {
        for (Player player : players) {
            player.getOutput().println(message);
        }
    }

    private void announceWinner() throws IOException {
        if (players.isEmpty()) {
            broadcast("No players in the game.");
            return;
        }

        // Calculate the highest score to identify ties or a clear winner.
        int highestScore = players.stream()
                .mapToInt(Player::getScore)
                .max()
                .orElse(0);

        List<Player> winners = players.stream()
                .filter(p -> p.getScore() == highestScore)
                .collect(Collectors.toList());

        if (winners.size() > 1) {
            broadcast("It's a tie between: " + winners.stream().map(Player::getNickname).collect(Collectors.joining(", ")));
            for (Player player : players) {
                if (winners.contains(player)) {
                    player.getOutput().println("GAME OVER: TIE");
                } else {
                    player.getOutput().println("GAME OVER: LOSE");
                }
            }
        } else if (winners.size() == 1) {
            Player winner = winners.get(0);
            broadcast("Winner is " + winner.getNickname());
            for (Player player : players) {
                if (player.equals(winner)) {
                    player.getOutput().println("GAME OVER: WIN");
                } else {
                    player.getOutput().println("GAME OVER: LOSE");
                }
            }
        } else {
            broadcast("GAME OVER: DRAW");
        }
    }


    private void cleanup() {
        players.forEach(player -> {
            try {
                player.closeConnection();
            } catch (IOException e) {
                System.out.println("Error closing player connection: " + e.getMessage());
            }
        });
    }
}
