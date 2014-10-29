import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

    private static String HOST = "localhost";
    private static int PORT = 3128;
    static String PASSWORD = "p";

    ServerSocket welcomeSocket = null;
    Socket connectionSocket = null;
    boolean startChat = true;

    int connectionCounter = 1;

    public static void main(String[] args) {
        new Server().start();
    }

    public void run() {
        try {
            welcomeSocket = new ServerSocket(PORT, 0,
                    InetAddress.getByName(HOST));
            System.out.println("server is started");

            do {
                connectionSocket = welcomeSocket.accept();
                if (startChat) {
                    new ConnectionThread(connectionSocket, this, connectionCounter).start();
                    connectionCounter++;
                }
            } while (startChat);

            welcomeSocket.close();
            connectionSocket.close();
            System.out.println("server not accept connection");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void close() {
        startChat = false;
    }
}

class ConnectionThread extends Thread {
    private final Socket connectionSocket;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private String messageFromClient;
    private String messageToClient;
    private Server server;
    boolean makeChat = true;
    int name;

    ConnectionThread(Socket connectionSocket, Server server, int counter) {
        this.connectionSocket = connectionSocket;
        this.server = server;
        this.name = counter;
        System.out.println("connection with " + name + " is accept");
    }

    public void run() {
        try {
            inFromClient = new BufferedReader(new InputStreamReader(
                    connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(
                    connectionSocket.getOutputStream());

            while (makeChat) {
                messageFromClient = inFromClient.readLine();
                messageToClient = generateReply(messageFromClient) + '\n';
                outToClient.write(messageToClient.getBytes("UTF-8"));
            }

            connectionSocket.close();
            System.out.println("connection with " + name + " is broken");
        } catch (Exception e) {
            // System.out.println(e.getMessage());
        }
    }

    private String generateReply(String message) {

        String[] s = message.split("\\s+");
        String reply = message.replaceFirst(s[0], " ").trim();

        switch (s[0]) {
            case ("LOWERCASE"):
                reply = reply.toLowerCase();
                break;
            case ("UPPERCASE"):
                reply = reply.toUpperCase();
                break;
            case ("REVERSE"):
                reply = new StringBuffer(reply).reverse().toString();
                break;
            case ("BYE"):
                reply = "BYE";
                makeChat = false;
                break;
            case ("SHUTDOWN"):
                if (s.length > 1 && Server.PASSWORD.startsWith(s[1])) {
                    reply = "OK_BYE";
                    makeChat = false;
                    server.close();
                    break;
                }
            default:
                reply = "ERROR " + message;
                break;
        }
        return reply;
    }
}