import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class POP3Server extends Thread {

    static String PASSWORD = "p";
    private static String HOST = "localhost";
    private static int PORT = 110;
    ServerSocket welcomeSocket = null;
    Socket connectionSocket = null;
    boolean startChat = true;
    int connectionCounter = 1;
    private String AbholAccount = "Iam";
    private String AbholPASSWORD = "me";
    private MailsManager mails;

    public static void main(String[] args) {
        new POP3Server().start();
    }

    public void run() {



        try {

            POP3Konto abholKonto = new POP3Konto(AbholAccount, AbholPASSWORD, HOST, PORT);
            welcomeSocket = new ServerSocket(PORT, 0,
                    InetAddress.getByName(HOST));
            System.out.println("server is started");

            new MailsCollectorThread(mails,null).run();

            do {
                connectionSocket = welcomeSocket.accept();
                if (startChat) {
                    new MailsKeeperThread (connectionSocket, this, connectionCounter, abholKonto, mails).start();
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

class MailsKeeperThread  extends Thread {
    private static final String CRLF = "\r\n";
    private Socket connectionSocket;
    private int name;
    private POP3Konto abholKonto;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private String messageFromClient;
    private String messageToClient;
    private POP3Server server;
    private MailsManager mailsManager;

    MailsKeeperThread (Socket connectionSocket, POP3Server server, int counter, POP3Konto abholKonto, MailsManager mails) {
        this.connectionSocket = connectionSocket;
        this.server = server;
        this.name = counter;
        this.abholKonto = abholKonto;
        this.mailsManager = mails;
        System.out.println("connection with " + name + " is accept");
    }

    public void run() {
        try {
            inFromClient = new BufferedReader(new InputStreamReader(
                    connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(
                    connectionSocket.getOutputStream());

            outToClient.writeBytes("+OK POP3 server ready" + CRLF);

            // The AUTHORIZATION State
            boolean guess = authorization();

            //The TRANSACTION State
            while (guess) {
                messageFromClient = inFromClient.readLine();

                if(check("QUIT")){
                    outToClient.writeBytes("+OK" + CRLF);
                    break;
                }
                outToClient.writeBytes(checkRequest()+CRLF);
            }

            //The Update State
            mailsManager.update();
            connectionSocket.close();
            System.out.println("connection with POP3Server " + name + " is broken, Server is off");

        } catch (Exception e) {
            // System.out.println(e.getMessage());
        }
    }

    private boolean authorization() throws IOException {

        boolean guess = false;

        while (!guess) {
            messageFromClient = inFromClient.readLine();
            if(check("QUIT")){
                outToClient.writeBytes("+OK" + CRLF);
                return guess;
            }
            guess = check("USER");
        }
        outToClient.writeBytes("+OK welcome, write your password" + CRLF);

        guess = false;
        while (!guess) {
            messageFromClient = inFromClient.readLine();
            if (check("QUIT")) {
                outToClient.writeBytes("+OK" + CRLF);
                return guess;
            }
            guess = check("PASS");
        }
        outToClient.writeBytes("+OK  maildrop locked and ready" + CRLF);

        return guess;
    }

    private boolean check(String command ) throws IOException {

        String[] request = messageFromClient.trim().split("\\s+");

        if(request[0].startsWith(command) && request.length < 3){
            switch (command) {
                case ("USER"):
                    if (request.length==2 && abholKonto.getUser().startsWith(request[1])){
                        return true;
                    }else {
                        outToClient.writeBytes("-ERR invalid mailbox name"+ CRLF);
                        return false;
                    }
                case ("PASS"):
                    if(request.length==2 && abholKonto.getPassword().startsWith(request[1])){
                        return true;
                    }else {
                        outToClient.writeBytes("-ERR invalid password "+ CRLF);
                        return false;
                    }
                case ("QUIT"):
                    if (request.length < 2 ){
                        return true;
                    }
            }
        }
        outToClient.writeBytes("-ERR invalid Argument"+ CRLF);
        return false;
    }

    private String checkRequest() {

        String[] request = messageFromClient.trim().split("\\s+");
        String reply = "-ERR bad Command";

        if(request.length < 2){
            switch (request[0]){
                case ("STAT"):
                    reply =mailsManager.getStat();
                    break;
                case ("NOOP"):
                    reply = "+OK";
                    break;
                case ("LIST"):
                    reply = mailsManager.getList();
                    break;
                case ("RSET"):
                    reply = mailsManager.setRSET();
                    break;
                case ("QUIT"):
                    reply = mailsManager.update();
                    break;
                case ("UIDL"):
                    reply = mailsManager.getUIDL();
            }
        }

        else if(request.length == 2){
            switch (request[0]) {
                case ("RETR"):
                    reply = mailsManager.getMessage(request[1]);
                    break;
                case ("DELE"):
                    reply = mailsManager.deleteMessage(request[1]);
                    break;
                case ("LIST"):
                    reply = mailsManager.getList(request[1]);
                    break;
                case ("UIDL"):
                    reply = mailsManager.getUIDL(request[1]);
            }
        }

        return reply;
    }
}