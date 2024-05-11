package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Player {
    private String nickname;
    private int score;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String answer;

    // Updated constructor to handle both socket and nickname
    public Player(Socket socket, String nickname) throws IOException {
        this.socket = socket;
        this.nickname = nickname; // Assign nickname directly from the parameter
        setupStreams();
        this.score = 0;
    }

    public void setupStreams() throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    public BufferedReader getInput() {
        return input;
    }

    public PrintWriter getOutput() {
        return output;
    }

    public void closeConnection() throws IOException {
        input.close();
        output.close();
        socket.close();
    }

    public String getNickname() {
        return nickname;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
}
