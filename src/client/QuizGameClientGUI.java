package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QuizGameClientGUI {
    private JFrame frame;
    private JTextField textFieldAnswer;
    private JButton buttonSend;
    private JTextArea textAreaDisplay;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public QuizGameClientGUI(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        initializeUI();
        promptForNickname();
        new Thread(this::listenForMessages).start();
    }

    private void initializeUI() {
        frame = new JFrame("Quiz Game Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);

        textFieldAnswer = new JTextField();
        textFieldAnswer.setFont(new Font("SansSerif", Font.PLAIN, 16));
        buttonSend = new JButton("Send");
        buttonSend.setFont(new Font("SansSerif", Font.BOLD, 16));
        textAreaDisplay = new JTextArea();
        textAreaDisplay.setEditable(false);
        textAreaDisplay.setFont(new Font("SansSerif", Font.PLAIN, 16));
        textAreaDisplay.setForeground(new Color(28, 150, 202));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(textFieldAnswer, BorderLayout.CENTER);
        panel.add(buttonSend, BorderLayout.EAST);

        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(textAreaDisplay), BorderLayout.CENTER);

        buttonSend.addActionListener(this::sendAnswer);

        frame.setVisible(true);
    }

    private void promptForNickname() {
        String nickname = JOptionPane.showInputDialog(frame, "Enter your nickname:", "Nickname", JOptionPane.PLAIN_MESSAGE);
        if (nickname != null && !nickname.isEmpty()) {
            out.println(nickname);
        } else {
            JOptionPane.showMessageDialog(frame, "You must enter a nickname to continue.", "Error", JOptionPane.ERROR_MESSAGE);
            promptForNickname();
        }
    }

    private void sendAnswer(ActionEvent e) {
        String answer = textFieldAnswer.getText().trim();
        if (!answer.isEmpty()) {
            out.println(answer);
            textFieldAnswer.setText("");
            textAreaDisplay.append("[" + sdf.format(new Date()) + "] Your answer: " + answer + "\n");
        }
    }

    private void listenForMessages() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                final String finalResponse = response;
                if (finalResponse.startsWith("QUESTION")) {
                    String question = "[" + sdf.format(new Date()) + "] QUESTION: " + finalResponse.substring(9);
                    SwingUtilities.invokeLater(() -> textAreaDisplay.append(question + "\n"));
                } else if (finalResponse.startsWith("SCORE")) {
                    SwingUtilities.invokeLater(() -> textAreaDisplay.append(finalResponse + "\n"));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
