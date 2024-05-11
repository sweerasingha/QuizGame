package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class QuizGameClientGUI {
    private JFrame frame;
    private JTextField textFieldAnswer, textFieldLobbyName;
    private JButton buttonSend, buttonCreateLobby, buttonJoinLobby, buttonStartGame;
    private JTextArea textAreaDisplay;
    private JComboBox<String> lobbyList;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isInLobby = false;

    public QuizGameClientGUI(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        initializeUI();
        promptForNickname();
        new Thread(this::listenToServer).start();
    }

    private void initializeUI() {
        frame = new JFrame("Quiz Game Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);

        JPanel topPanel = new JPanel(new FlowLayout());
        JPanel bottomPanel = new JPanel(new FlowLayout());

        textFieldAnswer = new JTextField(20);
        textFieldAnswer.setEnabled(false); // Initially disabled
        buttonSend = new JButton("Send");
        buttonSend.setEnabled(false); // Initially disabled

        textFieldLobbyName = new JTextField(10);
        buttonCreateLobby = new JButton("Create Lobby");
        buttonJoinLobby = new JButton("Join Lobby");
        buttonStartGame = new JButton("Start Game");
        buttonStartGame.setEnabled(false); // Initially disabled

        lobbyList = new JComboBox<>();
        lobbyList.setPreferredSize(new Dimension(200, 25));
        topPanel.add(new JLabel("Lobby:"));
        topPanel.add(lobbyList);
        topPanel.add(textFieldLobbyName);
        topPanel.add(buttonCreateLobby);
        topPanel.add(buttonJoinLobby);
        topPanel.add(buttonStartGame);

        textAreaDisplay = new JTextArea(10, 70);
        textAreaDisplay.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textAreaDisplay);

        bottomPanel.add(textFieldAnswer);
        bottomPanel.add(buttonSend);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        buttonSend.addActionListener(this::sendAnswer);
        buttonCreateLobby.addActionListener(this::createLobby);
        buttonJoinLobby.addActionListener(this::joinLobby);
        buttonStartGame.addActionListener(this::startGame);

        frame.setVisible(true);
    }

    private void promptForNickname() {
        String nickname = JOptionPane.showInputDialog(frame, "Enter your nickname:");
        if (nickname != null && !nickname.isEmpty()) {
            frame.setTitle("Quiz Game Client - " + nickname);
            out.println("NICKNAME " + nickname);
        } else {
            JOptionPane.showMessageDialog(frame, "You must enter a nickname to continue.", "Error", JOptionPane.ERROR_MESSAGE);
            promptForNickname();
        }
    }

    private void sendAnswer(ActionEvent e) {
        if (isInLobby) {
            String answer = textFieldAnswer.getText().trim();
            if (!answer.isEmpty()) {
                out.println("ANSWER " + answer);
                textFieldAnswer.setText(""); // Clear the input field after sending
                System.out.println("Answer sent: " + answer); // Debugging output
            }
        }
    }



    private void createLobby(ActionEvent e) {
        String lobbyName = textFieldLobbyName.getText().trim();
        if (!lobbyName.isEmpty()) {
            out.println("CREATE_LOBBY " + lobbyName);
            textFieldLobbyName.setText("");
        }
    }

    private void joinLobby(ActionEvent e) {
        String lobbyName = (String) lobbyList.getSelectedItem();
        if (lobbyName != null && !lobbyName.isEmpty()) {
            out.println("JOIN_LOBBY " + lobbyName);
        }
    }

    private void startGame(ActionEvent e) {
        if (isInLobby) {

            out.println("START_GAME");  // This will send the command to the server to start the game
            textAreaDisplay.append("Trying to start the game...\n");
        }
    }

    // Add this method to the QuizGameClientGUI class
    private void updatePlayerList(String playerListInfo) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = playerListInfo.split(":");
            if (parts.length > 1) {
                String[] players = parts[1].split(",");
                textAreaDisplay.append("Players in " + parts[0] + ": " + Arrays.toString(players) + "\n");
                if (players.length > 1) {
                    buttonStartGame.setEnabled(true);
                }
                if (players.length == 10) {
                    out.println("START_GAME"); // Auto-start the game if 10 players are connected
                }
            }
        });
    }


    private void listenToServer() {
        try {
            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer); // Debugging
                if (fromServer.startsWith("JOINED_LOBBY")) {
                    String[] parts = fromServer.split(" ");
                    isInLobby = true;
                    textFieldAnswer.setEnabled(true);
                    buttonSend.setEnabled(true);
                    textAreaDisplay.append("Joined lobby: " + parts[1] + "\n");
                    if (parts.length > 2 && "creator".equals(parts[2])) {
                        buttonStartGame.setEnabled(false); // Disable initially, enable when players > 1
                    }
                } else if (fromServer.startsWith("LOBBY_LIST")) {
                    updateLobbyList(fromServer.substring(11));
                } else if (fromServer.startsWith("PLAYER_LIST")) {
                    updatePlayerList(fromServer.substring(12));
                } else if (fromServer.startsWith("GAME_STARTED")) {
                    textAreaDisplay.append("Game has started!\n");
                    buttonStartGame.setEnabled(false);
                }
                else if (fromServer.startsWith("Your answer is")) {
                    textAreaDisplay.append(fromServer + "\n");
                }
                else {
                    textAreaDisplay.append(fromServer + "\n");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void updateLobbyList(String lobbies) {
        SwingUtilities.invokeLater(() -> {
            lobbyList.removeAllItems();
            for (String lobby : lobbies.split(",")) {
                lobbyList.addItem(lobby);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new QuizGameClientGUI("localhost", 1234);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}
