import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    private static int PORT = 3128;
    private static boolean makeChat = true;

    public static void main(String[] args) {
        Socket clientSocket;
        Scanner inFromUser;
        DataOutputStream outToServer;
        BufferedReader inFromServer;
        String writeUser;
        String messageToServer;

        try {
            clientSocket = initSocket(HOST, PORT);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));

            inFromUser = new Scanner(System.in);

            while (makeChat) {
                writeUser = inFromUser.nextLine() + '\n';
                System.out.println("FROM USER: " + writeUser);

                outToServer.write(writeUser.getBytes("UTF-8"));

                messageToServer = inFromServer.readLine();
                System.out.println("FROM SERVER: " + messageToServer);

                if (messageToServer.startsWith("BYE") || messageToServer.startsWith("OK_BYE")) makeChat = false;

            }
            clientSocket.close();
        } catch (IOException e) {
            System.exit(1);
        }
        System.out.println("client is stopped!");
    }

    private static Socket initSocket(String host, int port) {

        Socket socket = null;
        try {
            socket = new Socket(host, port);
            System.out.println("client is started. init port: " + port);
        } catch (Exception e) {
            System.out.println("init error: " + port);
            System.exit(-1);
        }
        return socket;
    }
}