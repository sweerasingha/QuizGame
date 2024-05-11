package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class QuizGameServer {
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private LobbyManager lobbyManager;
    private List<PlayerHandler> playerHandlers = new ArrayList<>();



    public QuizGameServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        pool = Executors.newCachedThreadPool();
        lobbyManager = new LobbyManager();
    }

    public void startServer() {
        System.out.println("Server is listening on port " + port);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                PlayerHandler handler = new PlayerHandler(socket, this, lobbyManager);
                playerHandlers.add(handler);
                pool.execute(handler);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void broadcastLobbyList() throws IOException {
        String lobbies = String.join(",", lobbyManager.getAvailableLobbies().keySet());
        for (PlayerHandler handler : playerHandlers) {
            handler.getOutput().println("LOBBY_LIST " + lobbies);
        }
    }

    public void broadcastPlayerList(String lobbyName) throws IOException {
        GameSession session = lobbyManager.getLobby(lobbyName);
        if (session != null) {
            String playerList = session.getPlayers().stream()
                    .map(Player::getNickname)
                    .collect(Collectors.joining(","));
            for (Player player : session.getPlayers()) {
                player.getOutput().println("PLAYER_LIST " + lobbyName + ":" + playerList);
            }
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