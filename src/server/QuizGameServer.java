package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizGameServer {
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private List<GameSession> sessions = new ArrayList<>();
    private int playersPerSession = 2; // Number of players per game session

    public QuizGameServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        pool = Executors.newCachedThreadPool();
    }

    public void startServer() {
        System.out.println("Server is listening on port " + port);
        try {
            while (true) {
                GameSession session = new GameSession();
                sessions.add(session);
                for (int i = 0; i < playersPerSession; i++) {
                    Socket socket = serverSocket.accept();
                    session.addPlayer(new Player(socket));
                    System.out.println("Player connected and added to session.");
                }
                pool.execute(session);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            stopServer();
        }
    }

    private void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            QuizGameServer server = new QuizGameServer(1234);
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
