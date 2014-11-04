import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MailsCollectorThread extends Thread {
    private final String CRLF = "\r\n";
    private long SLEEPTIME = 30000;
    private static List<POP3Konto> konten;

    private MailsManager mailManager ;

    public MailsCollectorThread(MailsManager mailManager, List<POP3Konto> konten) {
        this.mailManager = mailManager;

        if (konten != null){
            this.konten = konten;
        }else {
            this.konten = new ArrayList<POP3Konto>();
        }
    }

    public void run() {

        konten.add(new POP3Konto("scotty-haw1", "Snoopy1", "pop.yandex.ru", 110));
        konten.add(new POP3Konto("scottyhaw", "Snoopy1test", "pop.mail.yahoo.com", 995));
        konten.add(new POP3Konto("scotty.haw", "Snoopy1test", "pop3.web.de", 110));

        Socket clientSocket;
        DataOutputStream outToServer = null;
        BufferedReader inFromServer = null;
        String messageFromServer = null;

        while (true) {

            for (POP3Konto konto : konten) {

                //System.out.println("Konto: " + konto.getUser() + "@" + konto.getServer());
                clientSocket = initSocket(konto.getServer(), konto.getPort());

                try {
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    inFromServer = new BufferedReader(new InputStreamReader(
                            clientSocket.getInputStream()));

                    messageFromServer = inFromServer.readLine();

                    //Connection accept
                    if (!messageFromServer.trim().startsWith("+OK")) continue;

                    //User accept
                    outToServer.writeBytes("USER " + (konto.getUser()) + CRLF);
                    messageFromServer = inFromServer.readLine();
                    if (!messageFromServer.trim().startsWith("+OK")) continue;

                    //Paswort accept
                    outToServer.writeBytes("PASS " + konto.getPassword() + CRLF);
                    messageFromServer = inFromServer.readLine();
                    if (!messageFromServer.trim().startsWith("+OK")) continue;

                    //Mails abholen
                    outToServer.writeBytes("STAT"+CRLF);
                    messageFromServer = inFromServer.readLine();
                    String[] request = messageFromServer.trim().split("\\s+");
                    int count=0;

                    if(request[0].startsWith("+OK")){
                        count = Integer.getInteger(request[1]);
                    }

                    for (int i = 1; i <= count; i++) {
                       outToServer.writeBytes("RETR " + count );
                        messageFromServer = inFromServer.readLine();
                        if(!messageFromServer.trim().startsWith("+OK")) continue;

                        String message = "";
                        while (!(messageFromServer = inFromServer.readLine()).trim().equals(".")){
                            message = message + messageFromServer + CRLF;
                        }

                        message = message + "." + CRLF;
                        mailManager.saveNeuMail(konto.getUser(), konto.getServer(), message);

                        outToServer.writeBytes("DELE " + count);
                        if(messageFromServer.trim().startsWith("+OK")) continue;
                        System.out.println("Message " + count + "couldn't be deleted");

                     }
                    
                    //Session closed
                    outToServer.writeBytes("QUIT" + CRLF);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                sleep(SLEEPTIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Socket initSocket(String host, int port) {

        Socket socket = null;
        try {
            socket = new Socket(host, port);
            System.out.println("client is started. init host: " + host + "init port: " + port);
        } catch (Exception e) {
            System.out.println("init error: " + port + e.getMessage());
            System.exit(-1);
        }
        return socket;
    }
}
