package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerHandler implements Runnable {
    private Socket socket;
    private LobbyManager lobbyManager;
    private PrintWriter out;
    private BufferedReader in;
    private GameSession currentSession;
    private String playerName;
    private QuizGameServer server;

    public PlayerHandler(Socket socket, QuizGameServer server, LobbyManager lobbyManager) throws IOException {
        this.socket = socket;
        this.server = server;
        this.lobbyManager = lobbyManager;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public PrintWriter getOutput() {
        return out;
    }

    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                switch (line.split(" ")[0]) {
                    case "CREATE_LOBBY":
                        handleCreateLobby(line.substring("CREATE_LOBBY ".length()));
                        break;
                    case "JOIN_LOBBY":
                        handleJoinLobby(line.substring("JOIN_LOBBY ".length()));
                        break;
                    case "START_GAME":
                        if (currentSession != null && playerName.equals(currentSession.getPlayers().get(0).getNickname())) {
                            currentSession.startGame();
                        }
                        break;
                    case "NICKNAME":
                        playerName = line.substring("NICKNAME ".length());
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Player disconnected: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleCreateLobby(String lobbyName) throws IOException {
        lobbyManager.createLobby(lobbyName);
        currentSession = lobbyManager.joinLobby(lobbyName);
        currentSession.addPlayer(new Player(socket, playerName));
        server.broadcastLobbyList();
        server.broadcastPlayerList(lobbyName);
    }

    private void handleJoinLobby(String lobbyName) throws IOException {
        currentSession = lobbyManager.joinLobby(lobbyName);
        if (currentSession != null) {
            currentSession.addPlayer(new Player(socket, playerName));
            server.broadcastPlayerList(lobbyName);
            out.println("JOINED_LOBBY " + lobbyName);  // Confirm lobby joining
        } else {
            out.println("LOBBY_NOT_EXIST");
        }
    }
}
