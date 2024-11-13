package org.example;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to the chat server");
            String userInput;

            do {
                System.out.print("Enter message: ");
                userInput = reader.readLine();
                writer.println(userInput);

                String response = serverReader.readLine();
                System.out.println("Server response: " + response);
            } while (!userInput.equalsIgnoreCase("bye"));

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}

