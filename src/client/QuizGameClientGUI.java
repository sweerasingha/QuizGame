package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class QuizGameClientGUI {
    private JFrame frame;
    private JTextField textFieldAnswer;
    private JButton buttonSend;
    private JTextArea textAreaDisplay;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public QuizGameClientGUI(String serverAddress, int serverPort) throws IOException {
        // Establish connection
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Create GUI
        initializeUI();
        new Thread(this::listenForMessages).start();
    }

    private void initializeUI() {
        frame = new JFrame("Quiz Game Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);

        textFieldAnswer = new JTextField();
        buttonSend = new JButton("Send");
        textAreaDisplay = new JTextArea();
        textAreaDisplay.setEditable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(textFieldAnswer, BorderLayout.CENTER);
        panel.add(buttonSend, BorderLayout.EAST);
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(textAreaDisplay), BorderLayout.CENTER);

        buttonSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendAnswer();
            }
        });

        frame.setVisible(true);
    }

    private void sendAnswer() {
        String answer = textFieldAnswer.getText().trim();
        if (!answer.isEmpty()) {
            out.println(answer);
            textFieldAnswer.setText("");
        }
    }

    private void listenForMessages() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                final String finalResponse = response; // Declare a final variable
                if (finalResponse.startsWith("QUESTION")) {
                    String question = "QUESTION: " + finalResponse.substring(9);
                    SwingUtilities.invokeLater(() -> {
                        textAreaDisplay.append(question + "\nYour answer:\n");
                    });
                } else if (finalResponse.startsWith("SCORE")) {
                    SwingUtilities.invokeLater(() -> {
                        textAreaDisplay.append(finalResponse + "\n");
                    });
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
