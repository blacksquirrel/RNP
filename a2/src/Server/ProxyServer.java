package Server;
import Konto.*;
import Mail.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer extends Thread {

    private static String HOST  = "localhost";
    private static int PORT     = 3128;
    private String clientAcc    = "user";
    private String clientPass   = "hello";
    int connectionCounter       = 1;
    boolean startChat           = true;
    private static Konto user;
    private MailsManager mailsdrop;

    public ProxyServer() {
        this.mailsdrop = new MailsManager();
        this.user  = new Konto(clientAcc, clientPass,HOST, PORT);
    }

    public static void main(String[] args) {
        new ProxyServer().start();
    }

    public void run() {
        ServerSocket welcomeSocket  = null;
        Socket connectionSocket     = null;

        try {
            System.out.println("server is started");

            CollectorStarter collectorStarter = new CollectorStarter(mailsdrop, null);
            collectorStarter.setDaemon(true);
            collectorStarter.start();

            welcomeSocket = new ServerSocket(PORT, 0, InetAddress.getByName(HOST));

            do {
                connectionSocket = welcomeSocket.accept();
                if (startChat) {
                    new KeeperThread(connectionSocket, this, connectionCounter, user, mailsdrop).start();
                    connectionCounter++;
                }
            } while (startChat);

            welcomeSocket.close();
            connectionSocket.close();
            System.out.println("server not accept connection");

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connectionSocket != null)
                try {
                    connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    void close() {
        startChat = false;
    }
}

