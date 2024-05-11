package server;

import java.util.HashMap;
import java.util.Map;

public class LobbyManager {
    private Map<String, GameSession> lobbies = new HashMap<>();

    public synchronized void createLobby(String lobbyName) {
        if (!lobbies.containsKey(lobbyName)) {
            lobbies.put(lobbyName, new GameSession());
            // Potentially broadcast updated lobby list here or after command confirmation
        }
    }

    public synchronized GameSession joinLobby(String lobbyName) {
        return lobbies.get(lobbyName);
    }

    public synchronized Map<String, GameSession> getAvailableLobbies() {
        return new HashMap<>(lobbies);
    }

    public synchronized GameSession getLobby(String lobbyName) {
        return lobbies.get(lobbyName);
    }



}
