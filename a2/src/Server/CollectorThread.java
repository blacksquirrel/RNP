package Server;

import Konto.Konto;
import Mail.MailsManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class CollectorThread extends Thread {

    private static Konto konto = null;
    private final String CRLF = "\n";
    private MailsManager mailManager;

    public CollectorThread(MailsManager mailManager, Konto konto) {
        this.mailManager = mailManager;
        this.konto = konto;

    }

    private static Socket initSocket(String host, int port) {

        Socket socket = null;
        try {
            socket = new Socket(host, port);
            System.out.println("collector für: " + host + ":" + port );
        } catch (Exception e) {
            System.out.println("collector " + host +" initerror "+ e.getMessage());
            System.exit(-1);
        }
        return socket;
    }

    public void run() {

        Socket clientSocket;
        DataOutputStream outToServer = null;
        BufferedReader inFromServer = null;
        String messageFromServer = null;

        clientSocket = initSocket(konto.getServer(), konto.getPort());

        try {
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));


           // messageFromServer = readInputStream(inFromServer);

            writeRequest(outToServer, "NOOP");
            //System.out.println(konto.getServer() +" -> NOOP");
            messageFromServer = readInputStream(inFromServer);

            //Connection accept
            if (messageFromServer.startsWith("+OK")) {

                //User accept
                writeRequest(outToServer, "USER " + konto.getUser());
                messageFromServer = readInputStream(inFromServer);

                if (messageFromServer.startsWith("+OK")) {

                    //Paswort accept
                    writeRequest(outToServer, "PASS " + konto.getPassword());
                    messageFromServer = readInputStream(inFromServer);
                    if (messageFromServer.startsWith("+OK")) {

                        //Mails collect
                        writeRequest(outToServer, "STAT ");
                        messageFromServer = readInputStream(inFromServer);
                        String[] request = messageFromServer.split(" ");
                        int count = 0;

                        if (request[0].startsWith("+OK")) {
                            count = Integer.getInteger(request[1]);
                        }

                        for (int i = 1; i <= count; i++) {
                            writeRequest(outToServer, "RETR " + count);
                            messageFromServer = readInputStream(inFromServer);

                            if (messageFromServer.trim().startsWith("+OK")) {

                                //RETR accept
                                String message = "";

                                while (!( messageFromServer = readInputStream(inFromServer)).equals(".")) {
                                    message = message + messageFromServer + CRLF;
                                }

                                message = message + "." + CRLF;

                                //Save mail
                                mailManager.saveNeuMail(konto.getUser(), konto.getServer(), message);

                                //Delete mail
                                writeRequest(outToServer, "DELE " + count);
                                messageFromServer = readInputStream(inFromServer);
                                if (!messageFromServer.startsWith("+OK")) {
                                    System.out.println("Message " + count + "couldn't be deleted");
                                }
                            }
                        }
                    }
                }
            }

            //Session closed
            writeRequest(outToServer, "QUIT");
//            messageFromServer = readInputStream(inFromServer);

        } catch (IOException e) {
            System.out.println("!!! "+konto.getUser() + " have a problem");
            e.printStackTrace();
        }

    }

    private String readInputStream(BufferedReader inFromServer) {
        String replay = "go along" ;
        try {
            System.out.println("   " + inFromServer.readLine());
            replay = (inFromServer != null)? inFromServer.readLine().trim(): "hm... null";

        } catch (IOException e) {
            System.out.println("UPS... have a problem with inputstream");
            //e.printStackTrace();
        }
        return replay;
    }

    private void writeRequest(DataOutputStream outToClient, String reply) {
        try {
            System.out.println("  " + reply);
            reply = reply + CRLF;
            outToClient.write(reply.getBytes("UTF-8"));
        } catch (IOException e) {
            System.out.println("Hm... kann <" + reply + "> nicht als UTF-8 Bytefolge senden");
        }
    }
}
