import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class POP3Client extends Thread {
    private static final String CRLF = "\r\n";
    private static long SLEEPTIME = 30000;
    private static List<POP3Konto> konten;

    public POP3Client(List<POP3Konto> konten) {
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
        String serverWrite = null;

        while (true) {

            for (POP3Konto konto : konten) {

                //System.out.println("Konto: " + konto.getUser() + "@" + konto.getServer());
                clientSocket = initSocket(konto.getServer(), konto.getPort());

                try {
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    inFromServer = new BufferedReader(new InputStreamReader(
                            clientSocket.getInputStream()));
                    
                    serverWrite = inFromServer.readLine();

                    //Connection accept
                    if (!serverWrite.trim().startsWith("+OK")) continue;

                    //User accept
                    outToServer.writeBytes("USER " + (konto.getUser()) + CRLF);
                    serverWrite = inFromServer.readLine();
                    if (!serverWrite.trim().startsWith("+OK")) continue;

                    //Paswort accept
                    outToServer.writeBytes(("PASS " + konto.getPassword() + CRLF));
                    serverWrite = inFromServer.readLine();
                    if (!serverWrite.trim().startsWith("+OK")) continue;

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
