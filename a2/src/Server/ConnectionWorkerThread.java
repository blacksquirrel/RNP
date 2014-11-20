package Server;

import Konto.Konto;
import Mail.MailsManager;
import Tools.LogFile;

import java.io.*;
import java.net.Socket;
import java.util.List;

class ConnectionWorkerThread extends Thread {

    private final String CRLF = "\r\n";
    private Konto smtpClient = null;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private LogFile logfile;

    //Def in constructor
    private Socket connectionSocket;
    private ProxyServer proxyServer;
    private Konto client;
    private MailsManager emailsbox;
    private List<Konto> kontosbox;
    private boolean belive;

    //Constructor
    public ConnectionWorkerThread(Socket connectionSocket, ProxyServer proxyServer, Konto client, MailsManager emailsbox, List<Konto> kontosbox) {
        this.connectionSocket = connectionSocket;
        this.proxyServer = proxyServer;
        this.client = client;
        this.emailsbox = emailsbox;
        this.kontosbox = kontosbox;
        this.belive = false;
        this.logfile = new LogFile("Client-" + client.getUser());
    }

    public void run() {
        try {

            //init IO-Stream from/to client and send Ready-Message
            inFromClient = new BufferedReader(new InputStreamReader(
                    connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(
                    connectionSocket.getOutputStream());

            send(outToClient, "+OK proxyServer ready");

            //take authorization of client
            while (!belive) {
                if (isRegisteredUser(inFromClient.readLine(), outToClient)) {
                    while (!belive) {
                        belive = isRightPass(inFromClient.readLine(), outToClient);
                    }
                }
            }

            //authorization successful, start transaction
            if (belive) {
                //client collects his email
                if (client.getTyp().startsWith("POP3Client")) {
                    pop3transaction();

                    //client sendet neu email
                } else if (client.getTyp().startsWith("SMTPClient")) {
                    smtpTransaction();

                    //something else
                } else {
                    send(outToClient, "QUIT");
                }
            }

            //close client-connection
            connectionSocket.close();
            System.out.println("connection is broken");


        } catch (Exception e) {
            // System.out.println(e.getMessage());
        }
    }

    private void send(DataOutputStream out, String reply) {

        if (connectionSocket.isClosed()) this.close();

        try {
            logfile.update("Keeper: " + reply);
            out.write((reply + CRLF).getBytes("UTF-8"));
        } catch (IOException e) {
            System.out.println("Hm... kann nicht als UTF-8 Bytefolge senden");
        }
    }

    private boolean isRegisteredUser(String candidName, DataOutputStream outToClient) throws IOException {

        String userName = client.getUser();
        logfile.update("Client: " + candidName);

        String[] request = candidName.trim().split(" ");

        //if one word: expected NOOP or QUIT or something else
        if (request.length == 1) {
            if (request[0].startsWith("NOOP")) {
                send(outToClient, " +OK");
            } else if (request[0].startsWith("QUIT")) {
                this.close();
            } else {
                send(outToClient, "-ERR Unknown command - 1");
            }
        }

        //if two word: expected USER + username or something else
        if (request.length == 2) {
            if (!request[0].startsWith("USER")) {
                send(outToClient, "-ERR Unknown command - 2");
            } else if (request[1].startsWith(userName)) {
                send(outToClient, "+OK " + request[1] + " is a real user");
                return true;
            } else {
                send(outToClient, "-ERR Unknow user");
            }
        }

        //if more as two word: not accept
        if (request.length > 2) send(outToClient, "-ERR Unknown comman - 3");

        return false;
    }

    private boolean isRightPass(String candidPass, DataOutputStream outToClient) {
        String password = client.getPassword();
        try {
            logfile.update("Client: " + candidPass);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String[] request = candidPass.trim().split(" ");

        //if one word: expected NOOP or QUIT or something else
        if (request.length == 1) {
            if (request[0].startsWith("NOOP")) {
                send(outToClient, " +OK");
            } else if (request[0].startsWith("QUIT")) {
                this.close();
            } else {
                send(outToClient, "-ERR Unknown command - 1");
            }
        }

        //if two word: expected USER + username or something else
        if (request.length == 2) {
            if (!request[0].startsWith("PASS")) {
                send(outToClient, "-ERR Unknown command - 2");
            } else if (request[1].startsWith(password)) {
                send(outToClient, "+OK maildrop locked and ready");
                return true;
            } else {
                send(outToClient, "-ERR unknow user");
            }
        }

        //if more as two word: not accept
        if (request.length > 2) send(outToClient, "-ERR Unknown comman - 3");
        return false;
    }

    private void close() {
        belive = false;
    }

    private void smtpTransaction() {
        Socket clientSocket = null;
        String[] command;
        String request = null;
        String reply = "504 Command parameter not implemented";

        try {
            //if the client is registered,
            //create a socket for connection to the SMTP-Server
            //else interrupt trans
            request = inFromClient.readLine();
            command = request.trim().split(" ");

            if (command[0].startsWith("HELO")) {

                for (Konto konto : kontosbox) {

                    if (konto.getUser().startsWith(command[1])) {

                        smtpClient = konto;
                        try {
                            clientSocket = new Socket(smtpClient.getHost(), smtpClient.getPort());
                        } catch (IOException e) {
                            System.out.println(smtpClient.getHost() + " init error " + e.getMessage());
                            System.exit(-1);
                        }
                        break;
                    }
                }
            }

            if (smtpClient == null) {
                send(outToClient, "bad user");
                return;
            }

        } catch (IOException e) {
            System.out.println("IN from client have problem");
            System.exit(-1);
        }


        try {
            DataOutputStream outToSMTPServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromSMTPServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));

            String messageFromClient;
            String messageFromServer;
            boolean makeChat = true;

            while (makeChat) {
                messageFromServer = readInputStream(inFromSMTPServer);
                if (Integer.parseInt(messageFromServer.trim().split(" ")[0]) > 500) makeChat = false;
                send(outToClient, messageFromServer);

                messageFromClient = readInputStream(inFromClient);
                if (messageFromClient.startsWith("QUIT")) makeChat = false;
                System.out.println("smtp quit");
                send(outToSMTPServer, messageFromClient);
            }

        } catch (IOException e) {
            System.out.println("!!! " + smtpClient.getUser() + " have a problem");

        }
    }

    private void pop3transaction() {

        String[] command;
        String request;
        String reply;

        while (belive) {
            reply = "-ERR bad Command";
            try {
                request = inFromClient.readLine();
                command = request.trim().split(" ");

                if (command.length < 2) {
                    logfile.update("Client: " + command[0]);
                    switch (command[0]) {
                        case ("NOOP"):
                            reply = "+OK";
                            break;
                        case ("STAT"):
                            reply = emailsbox.getStat();
                            break;
                        case ("LIST"):
                            reply = emailsbox.getList();
                            break;
                        case ("RSET"):
                            reply = emailsbox.setRSET();
                            break;
                        case ("QUIT"):
                            reply = emailsbox.setUpdate();
                            logfile.update("-----------------------------------------------");
                            return;
                        case ("UIDL"):
                            reply = emailsbox.getUIDL();
                    }
                } else if (command.length == 2) {
                    logfile.update("Client: " + command[0] + " " + command[1]);
                    switch (command[0]) {
                        case ("RETR"):
                            reply = emailsbox.getMessage(command[1]);
                            break;
                        case ("DELE"):
                            reply = emailsbox.deleteMessage(command[1]);
                            break;
                        case ("LIST"):
                            reply = emailsbox.getList(command[1]);
                            break;
                        case ("UIDL"):
                            reply = emailsbox.getUIDL(command[1]);
                            break;
                        case ("SHUTDOWN"):
                            if (client.getPassword().startsWith(command[1])) {
                                close();
                                reply = "+OK Bye";
                            }
                    }
                }
            } catch (IOException e) {
                System.out.println("IN from client have problem");
            }

            send(outToClient, reply);
        }
    }

    private String readInputStream(BufferedReader inFromServer) {

        if (connectionSocket.isClosed()) this.close();

        String reply = "go along";
        try {
            reply = (inFromServer != null) ? inFromServer.readLine().trim() : "hm... null";
            // System.out.println(" <<" + reply);

        } catch (IOException e) {
            System.out.println("UPS... have a problem with inputstream");
            //e.printStackTrace();
        }
        return reply;
    }
}