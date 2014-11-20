package Server;

import Konto.Konto;
import Mail.MailsManager;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ProxyServer extends Thread {

    private MailsManager emailsbox;
    private List<Konto> kontosbox;
    private Konto POP3Client;
    private Konto SMTPClient;

    private boolean acceptConnect = true;
    private long sleeptime = 30000;

    public ProxyServer() {
        this.emailsbox = new MailsManager();
        this.kontosbox = new ArrayList<>();
        initClients("ProxyConfig.txt");
    }

    public static void main(String[] args) {
        new ProxyServer().start();
    }

    public void run() {

        //start collectors
        for (Konto collector : kontosbox) {
            if (collector.getTyp().startsWith("SMTPOutgoingMailServer")) continue;
            new MailCollectorThread(emailsbox, collector, sleeptime).start();
        }

        //start Keeperthread
        //accept POP3 incoming
        ServerSocket welcomePOP3Socket = null;

        try {
            welcomePOP3Socket = new ServerSocket(POP3Client.getPort(), 0, InetAddress.getByName(POP3Client.getHost()));
        } catch (IOException e) {
            System.out.println("Host is invalid");
        }

        ConnectionAcceptThread POP3Proxy = new ConnectionAcceptThread(this, welcomePOP3Socket, POP3Client, emailsbox, kontosbox);
        POP3Proxy.setDaemon(true);
        POP3Proxy.start();

        //accept SMTP incomming
        ServerSocket welcomeSMTPSocket = null;

        try {
            welcomeSMTPSocket = new ServerSocket(SMTPClient.getPort(), 0, InetAddress.getByName(SMTPClient.getHost()));
        } catch (IOException e) {
            System.out.println("Host is invalid");
        }

        ConnectionAcceptThread SMTPProxy = new ConnectionAcceptThread(this, welcomeSMTPSocket, SMTPClient, emailsbox, kontosbox);
        SMTPProxy.setDaemon(true);
        SMTPProxy.start();

        System.out.println("server is started");

        //if thread is death started him
        while (acceptConnect) {
            if (!POP3Proxy.isAlive()) POP3Proxy.start();
            if (!SMTPProxy.isAlive()) SMTPProxy.start();
        }

        //close all und exit
        try {
            welcomePOP3Socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("server not accept connection");
    }

    //Hilfsmethoden
    //read config file and init clients
    private void initClients(String fileName) {

        File configFile = new File(fileName);
        if (!configFile.exists()) System.exit(0);
        String[] config;

        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = br.readLine()) != null) {
                config = line.trim().split(":");

                if (config[1] == null || config[1].isEmpty()) continue;

                switch (config[0]) {
                    case ("POP3Client"):
                        this.POP3Client = createNewKonto(config[0], config[1]);
                        break;
                    case ("SMTPClient"):
                        this.SMTPClient = createNewKonto(config[0], config[1]);
                        break;
                    case ("POP3IncomingMailServer"):
                        kontosbox.add(createNewKonto(config[0], config[1]));
                        break;
                    case ("SMTPOutgoingMailServer"):
                        kontosbox.add(createNewKonto(config[0], config[1]));
                }
            }
            br.close();

        } catch (FileNotFoundException e) {
            System.out.println("Config File not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Config File is empty");
            e.printStackTrace();
        }
    }

    private Konto createNewKonto(String typ, String daten) {
        String[] config = daten.trim().split(",");

        String host = config[0];
        String port = config[1];
        String name = config[2];
        String pass = config[3];

        return new Konto(typ, name, pass, host, port);
    }

    void close() {
        acceptConnect = false;
    }
}