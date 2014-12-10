import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class PacketAckChecker extends Thread {

    private FileCopyClient fileCopyClient;
    private DatagramSocket socket;


    public PacketAckChecker(FileCopyClient fileCopyClient, DatagramSocket socket) {
        this.fileCopyClient = fileCopyClient;
        this.socket = socket;
    }

    public void run() {

        while (!interrupted()) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[8], 8);
                socket.receive(datagramPacket);

                FCpacket packet = new FCpacket(datagramPacket.getData(), datagramPacket.getLength());

                //  System.out.println("Ack für packet " + packet.getSeqNum() + " empfangen.");

                fileCopyClient.setAck(packet);
                fileCopyClient.cancelTimer(packet);

            } catch (IOException ex) {
                socket.close();
            }
        }
    }
}
