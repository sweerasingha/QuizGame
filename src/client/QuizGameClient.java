package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class QuizGameClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public QuizGameClient(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter your nickname:");
            String nickname = scanner.nextLine();
            out.println(nickname);

            String response;
            while ((response = in.readLine()) != null) {
                if (response.startsWith("QUESTION")) {
                    System.out.println("QUESTION: " + response.substring(9));
                    System.out.println("Your answer:");
                    String answer = scanner.nextLine();
                    out.println(answer);
                } else if (response.startsWith("SCORE")) {
                    System.out.println(response);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            QuizGameClient client = new QuizGameClient("localhost", 1234);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
