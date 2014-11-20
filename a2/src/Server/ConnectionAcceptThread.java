package Server;

import Konto.Konto;
import Mail.MailsManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by maxim on 12.11.14.
 */
public class ConnectionAcceptThread extends Thread {
    private ServerSocket welcomeSocket;
    private ProxyServer proxyServer;
    private Konto client;
    private List<Konto> kontosbox;
    private MailsManager emailsbox;


    public ConnectionAcceptThread(ProxyServer proxyServer, ServerSocket welcomeSocket, Konto client, MailsManager emailsbox, List<Konto> kontosbox) {
        this.welcomeSocket = welcomeSocket;
        this.proxyServer = proxyServer;
        this.client = client;
        this.emailsbox = emailsbox;
        this.kontosbox = kontosbox;
    }

    public void run() {
        Socket connectionSocket = null;

        while (true) {
            try {
                connectionSocket = welcomeSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new ConnectionWorkerThread(connectionSocket, proxyServer, client, emailsbox,kontosbox).start();
        }
    }
}
