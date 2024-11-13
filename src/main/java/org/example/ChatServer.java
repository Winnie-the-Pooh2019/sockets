package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    static void broadcast(String message, ClientHandler excludeUser) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != excludeUser) {
                clientHandler.sendMessage(message);
            }
        }
    }

    static void sendMessageToUser(String message, String username) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUsername().equals(username)) {
                clientHandler.sendMessage(message);
                break;
            }
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            this.writer = writer;
            writer.println("Enter your username:");
            this.username = reader.readLine();
            System.out.println("Username: " + username);

            String text;
            do {
                text = reader.readLine();
                if (text.startsWith("@")) {
                    int spaceIndex = text.indexOf(' ');
                    String recipient = text.substring(1, spaceIndex);
                    String message = text.substring(spaceIndex + 1);
                    ChatServer.sendMessageToUser(message, recipient);
                } else {
                    ChatServer.broadcast(username + ": " + text, this);
                }
            } while (!text.equalsIgnoreCase("bye"));

            socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            ChatServer.clientHandlers.remove(this);
        }
    }

    void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    String getUsername() {
        return username;
    }
}
