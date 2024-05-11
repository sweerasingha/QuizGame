package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.stream.Collectors;

public class PlayerHandler implements Runnable {
    private final Socket socket;
    private final LobbyManager lobbyManager;
    private final PrintWriter out;
    private final BufferedReader in;
    private GameSession currentSession;
    private String playerName;
    private final QuizGameServer server;

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
                    case "CREATE_LOBBY" -> handleCreateLobby(line.substring("CREATE_LOBBY ".length()));
                    case "JOIN_LOBBY" -> handleJoinLobby(line.substring("JOIN_LOBBY ".length()));
                    case "START_GAME" -> {
                        if (currentSession != null && playerName.equals(currentSession.getPlayers().get(0).getNickname())) {
                            currentSession.startGame();
                        }
                    }
                    case "NICKNAME" -> playerName = line.substring("NICKNAME ".length());
                    case "ANSWER" -> handleAnswer(line.substring("ANSWER ".length()));
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

    private void handleAnswer(String answer) {
        if (currentSession != null) {
            Player player = currentSession.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(playerName))
                    .findFirst()
                    .orElse(null);

            if (player != null) {
                try {
                    currentSession.processAnswer(player, answer);
                } catch (IOException e) {
                    System.out.println("Error processing answer: " + e.getMessage());
                }
            }
        }
    }

    private void handleCreateLobby(String lobbyName) throws IOException {
        lobbyManager.createLobby(lobbyName);
        currentSession = lobbyManager.joinLobby(lobbyName);
        Player newPlayer = new Player(socket, playerName);
        currentSession.addPlayer(newPlayer);
        currentSession.setCreator(newPlayer); // Assuming a setCreator method in GameSession
        server.broadcastLobbyList();
        server.broadcastPlayerList(lobbyName);
        out.println("JOINED_LOBBY " + lobbyName + " creator");  // Indicate that this player is the creator
    }

    private void handleJoinLobby(String lobbyName) throws IOException {
        currentSession = lobbyManager.joinLobby(lobbyName);
        if (currentSession != null) {
            currentSession.addPlayer(new Player(socket, playerName));
            server.broadcastPlayerList(lobbyName);
            boolean isCreator = playerName.equals(currentSession.getCreator().getNickname());
            out.println("JOINED_LOBBY " + lobbyName + (isCreator ? " creator" : ""));
            if (currentSession.getPlayers().size() == 10) {
                currentSession.startGame();
            }
        } else {
            out.println("LOBBY_NOT_EXIST");
        }
    }
}
