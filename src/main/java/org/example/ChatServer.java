package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    static Set<ClientHandler> clientHandlers = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Client connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException ex) {
            logger.error("Server error {}", ex.getMessage());
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

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

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
            logger.info("Client connected with username {}", username);

            String text;
            do {
                text = reader.readLine();
                if (text.startsWith("@")) {
                    int spaceIndex = text.indexOf(' ');
                    String recipient = text.substring(1, spaceIndex);
                    String message = text.substring(spaceIndex + 1);

                    if (message.equals("null"))
                        logger.warn("Null message received");

                    logger.info("Message from {} to {}", username, recipient);

                    ChatServer.sendMessageToUser(message, recipient);
                } else {
                    logger.info("Broadcast message {} from {}", text, username);

                    if (text.equals("null"))
                        logger.warn("Null message received");

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
