package Server;

import Konto.Konto;
import Mail.MailsManager;
import Tools.LogFile;

import java.io.*;
import java.net.Socket;


public class MailCollectorThread extends Thread {

    private static Konto konto;
    private LogFile logfile;
    private final String CRLF = "\n";
    private final long SLEEPTIME;
    private MailsManager mailManager;
    private DataOutputStream outToMailbox;
    private BufferedReader inFromMailbox;
    private Socket clientSocket;

    public MailCollectorThread(MailsManager mailManager, Konto konto, long sleeptime) {
        this.mailManager = mailManager;
        this.konto = konto;
        SLEEPTIME = sleeptime;

        logfile = new LogFile(konto.getUser());


    }

    private void initSocket() {

        try {
            clientSocket = new Socket(konto.getHost(), konto.getPort());
            logfile.update("Collector für: " + konto.getHost() + ":" + konto.getPort()+" is started");
        } catch (Exception e) {
            System.out.println("collector " + konto.getHost() + " init error " + e.getMessage());
            System.exit(-1);
        }
    }

    public void run() {

        while (true) {

            initSocket();

            try {
                outToMailbox = new DataOutputStream(clientSocket.getOutputStream());
                inFromMailbox = new BufferedReader(new InputStreamReader(
                        clientSocket.getInputStream()));
            } catch (IOException e) {
                System.out.println(konto.getUser() + " have a problem with Stream-IO");
            }

            //If connection is accept and authenticating is successfully - collect Mails
            if (readFromMailbox().startsWith("+OK")) {
                if (authenticatingToMailbox()) {
                    collectMails();
                }
            }

            //close Session
            try {
                sendToMailbox("QUIT");
                logfile.update("Connection is broken" + "\n"+ "--------------------------------------------");
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Have a Problem with socket close");
                e.printStackTrace();
            }

            try {
                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException e) {
                System.out.println("Have a Problem with Timer");
                e.printStackTrace();
            }

        }
    }

    private void collectMails()  {
        int count;
        String line;

        sendToMailbox("STAT ");
        String[] request = readFromMailbox().split(" ");


        if (request[0].startsWith("+OK") && request[1] != null) {
            count = Integer.parseInt(request[1]);
            line = "";
        } else {
            return;
        }

        String messageFromServer;
        for (int i = 1; i <= count; i++) {
            sendToMailbox("RETR " + i);
            messageFromServer = readFromMailbox();

            //if RETR accept - Save Mail to HDD and delete from Mailbox
            if (messageFromServer.trim().startsWith("+OK")) {

                while (!(messageFromServer = readFromMailbox()).equals(".")) {
                    if(messageFromServer.equals("..")) messageFromServer = ".";
                    line = line + messageFromServer + CRLF;
                }
                line = line + "." + CRLF;

                //Save mail to HDD
                mailManager.saveNeuMail(konto.getUser(), konto.getHost(), line);

                //Delete mail from Mailbox
                sendToMailbox("DELE " + i);
                messageFromServer = readFromMailbox();
                if (!messageFromServer.startsWith("+OK")) {
                    try {
                        logfile.update(" !!! Message " + count + "couldn't be deleted");
                    } catch (FileNotFoundException e) {
                        System.out.println("Logfile not found");
                    }
                }
            }
        }
    }

    private boolean authenticatingToMailbox() {
        sendToMailbox("USER " + konto.getUser());
        if (readFromMailbox().startsWith("+OK ")) {
            sendToMailbox("PASS " + konto.getPassword());
            if (readFromMailbox().startsWith("+OK")) {
                try {
                    logfile.update(" + Authenticating is ok");
                } catch (FileNotFoundException e) {
                    System.out.println("Logfile not found");
                }
                return true;
            }else {
                try {
                    logfile.update(" - Authenticating is not ok");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private String readFromMailbox() {
        String reply = "go along";
        try {
            reply = inFromMailbox.readLine().trim();
            logfile.update(" < Mailbox:    " + reply);
        } catch (IOException e) {
            System.out.println("UPS... have a problem with inputstream");
        }
        return reply;
    }

    private void sendToMailbox(String reply) {
        try {
            reply = reply + "\n";
            logfile.update(" > Collector: " + reply);
            outToMailbox.write(reply.getBytes("UTF-8"));
        } catch (IOException e) {
            System.out.println("Hm... kann <" + reply + "> nicht als UTF-8 Bytefolge senden");
        }
    }
}
