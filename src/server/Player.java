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

    public Player(Socket socket) throws IOException {
        this.socket = socket;
        setupStreams();
        this.nickname = input.readLine();  // Assume first message is the nickname
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
        socket.close();
    }

    public String getNickname() {
        return nickname;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
}
