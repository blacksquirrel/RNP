package Server;

import Mail.*;
import Konto.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class KeeperThread extends Thread {
    private Socket connectionSocket;
    private ProxyServer server;
    private int name;
    private Konto clientKonto;
    private MailsManager mailsdrop;

    private final String CRLF = "\r\n";
    private boolean belive    = false;

    KeeperThread(Socket connectionSocket, ProxyServer server, int counter, Konto clientKonto, MailsManager mailsdrop) {
        this.connectionSocket = connectionSocket;
        this.server = server;
        this.name = counter;
        this.clientKonto = clientKonto;
        this.mailsdrop = mailsdrop;
        System.out.println("connection with " + name + " is accept");
    }

    public void run() {

        BufferedReader inFromClient;
        DataOutputStream outToClient;

        try {

            inFromClient = new BufferedReader(new InputStreamReader(
                    connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(
                    connectionSocket.getOutputStream());

            writeReply(outToClient,"+OK POP3 server ready");

            //The AUTHORIZATION State
            while (!belive){
                if(isKnowUserName(inFromClient.readLine(), outToClient)) {
                    while (!belive) {
                        belive = isRightPass(inFromClient.readLine(), outToClient);
                    }
                }
            }

            String request;
            String response;
            //The TRANSACTION State
            while (belive) {
                request  = inFromClient.readLine();
                response = doit(request);
                writeReply(outToClient, response);
            }

            //The UPDATE State
            mailsdrop.setUpdate();
            connectionSocket.close();
            System.out.println("connection "+ name +" is broken, session is off");

        } catch (Exception e) {
            // System.out.println(e.getMessage());
        }
    }

    private void  writeReply(DataOutputStream outToClient,String reply){
        try {
            outToClient.write((reply+CRLF).getBytes("UTF-8"));
        } catch (IOException e) {
            System.out.println("Hm... kann nicht als UTF-8 Bytefolge senden");
        }
    }

    private boolean isKnowUserName(String inFromClient, DataOutputStream outToClient) throws IOException {

        String userName = clientKonto.getUser();
        String[] request = inFromClient.trim().split(" ");

        //if one word: expected NOOP or QUIT or something else
        if (request.length == 1) {
            if (request[0].startsWith("NOOP")) {
                writeReply(outToClient, " +OK");
            } else if (request[0].startsWith("QUIT")) {
                this.close();
            } else {
                writeReply(outToClient, "-ERR Unknown command - 1");
            }
        }

        //if two word: expected USER + username or something else
        if (request.length == 2) {
            if (!request[0].startsWith("USER")) {
                writeReply(outToClient, "-ERR Unknown command - 2");
            } else if (request[1].startsWith(userName)) {
                writeReply(outToClient, "+OK " + request[1] + " is a real user");
                return true;
            } else {
                writeReply(outToClient, "-ERR Unknow user");
            }
        }

        //if more as two word: not accept
        if (request.length > 2) writeReply(outToClient, "-ERR Unknown comman - 3");

        return false;
    }

    private boolean isRightPass(String inFromClient, DataOutputStream outToClient) {
        String password = clientKonto.getPassword();
        String[] request = inFromClient.trim().split(" ");

        //if one word: expected NOOP or QUIT or something else
        if (request.length == 1) {
            if (request[0].startsWith("NOOP")) {
                writeReply(outToClient, " +OK");
            } else if (request[0].startsWith("QUIT")) {
                this.close();
            } else {
                writeReply(outToClient, "-ERR Unknown command - 1");
            }
        }

        //if two word: expected USER + username or something else
        if (request.length == 2) {
            if (!request[0].startsWith("PASS")) {
                writeReply(outToClient, "-ERR Unknown command - 2");
            } else if (request[1].startsWith(password)) {
                writeReply(outToClient, "+OK maildrop locked and ready" );
                return true;
            } else {
                writeReply(outToClient, "-ERR unknow user");
            }
        }

        //if more as two word: not accept
        if (request.length > 2) writeReply(outToClient, "-ERR Unknown comman - 3");
        return false;
    }

    private void close() {
        belive = false;
        server.close();
    }

    private String doit(String request) {

        String[] command = request.trim().split(" ");
        String reply = "-ERR bad Command";

        if(command.length < 2){
            switch (command[0]){
                case ("NOOP"):
                    reply = "+OK";
                    break;
                case ("STAT"):
                    reply = mailsdrop.getStat();
                    break;
                case ("LIST"):
                    reply = mailsdrop.getList();
                    break;
                case ("RSET"):
                    reply = mailsdrop.setRSET();
                    break;
                case ("QUIT"):
                    reply = mailsdrop.setUpdate();
                    break;
                case ("UIDL"):
                    reply = mailsdrop.getUIDL();
            }
        }

        else if(command.length == 2){
            switch (command[0]) {
                case ("RETR"):
                    reply = mailsdrop.getMessage(command[1]);
                    break;
                case ("DELE"):
                    reply = mailsdrop.deleteMessage(command[1]);
                    break;
                case ("LIST"):
                    reply = mailsdrop.getList(command[1]);
                    break;
                case ("UIDL"):
                    reply = mailsdrop.getUIDL(command[1]);
                    break;
                case ("SHUTDOWN"):
                    if (clientKonto.getPassword().startsWith(command[1])) {
                        close();
                        reply = "+OK Bye";
                    }
            }
        }
        return reply;
    }
}
