/* FileCopyClient.java
 Version 0.1 - Muss erg�nzt werden!!
 Praktikum 3 Rechnernetze BAI4 HAW Hamburg
 Autoren:
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class FileCopyClient extends Thread {

    // -------- Constants
    public final static boolean TEST_OUTPUT_MODE = false;
    public final int SERVER_PORT = 23000;
    public final int UDP_PACKET_SIZE = 1008;

    // -------- Public parms
    public String servername;
    public String sourcePath;
    public String destPath;
    public int windowSize;
    public long serverErrorRate;

    // -------- Variables
    // current default timeout in nanoseconds
    private long timeoutValue = 100000000L;

    private DatagramSocket socket;
    private PacketAckChecker packetAckChecker;
    private PacketBuffer buffer;
    private int sendBase = 0;
    private int nextSeqN = 0;

    // Constructor
    public FileCopyClient(String serverArg, String sourcePathArg, String destPathArg, String windowSizeArg, String errorRateArg) {
        servername = serverArg;
        sourcePath = sourcePathArg;
        destPath = destPathArg;
        windowSize = Integer.parseInt(windowSizeArg);
        serverErrorRate = Long.parseLong(errorRateArg);

    }

    public void runFileCopyClient() {

        // ToDo!!
        long startTime = System.currentTimeMillis();

        //init Socket
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Have Problem with init Socket");
            e.printStackTrace();
        }

        //start AckCheckerThread
        packetAckChecker = new PacketAckChecker(this, socket);
        packetAckChecker.start();

        //init PacketBuffer
        buffer = new PacketBuffer(this, windowSize);

        //send ControlPacket SeqNumber 0
        sendPacket(makeControlPacket());

        //init file reader
        FileInputStream fileReader = null;
        try {
            fileReader = new FileInputStream(sourcePath);
        } catch (FileNotFoundException e) {
            System.out.println("Have Problem with SourcePath");
            e.printStackTrace();
        }

        FCpacket packetToSend;

        while ((packetToSend = getNextPacket(fileReader)) != null) {
            while (buffer.isFull()) {
                //do nothing
            }


            startTimer(packetToSend);
            buffer.ad(packetToSend);
            sendPacket(packetToSend);
        }

        //The End
        System.out.println("Total Transfer Time: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    /**
     * Getter and Setter
     */
    public int getNextSeqN() {
        return ++nextSeqN;
    }

    /**
     * Packet Operations
     */
    private FCpacket getNextPacket(FileInputStream inputStream) {
        byte[] data = new byte[UDP_PACKET_SIZE - 8];
        int dataSize = 0;

        try {
            dataSize = inputStream.read(data);
        } catch (IOException e) {
            System.out.println("Have Problem with read input stream");
            e.printStackTrace();
        }

        if (dataSize > 0) {
            return new FCpacket(getNextSeqN(), data, dataSize);
        }
        return null;
    }

    private void sendPacket(FCpacket packet) {

        DatagramPacket packetToSend = null;

        try {
            packetToSend = new DatagramPacket(new byte[UDP_PACKET_SIZE], UDP_PACKET_SIZE, InetAddress.getByName(servername), SERVER_PORT);
        } catch (UnknownHostException e) {
            System.out.println("Server ist not available");
            e.printStackTrace();
        }

        packetToSend.setData(packet.getSeqNumBytesAndData());

        try {
            socket.send(packetToSend);
        } catch (IOException e) {
            System.out.println("Have Problem with send Packet");
            e.printStackTrace();
        }

    }

    public void setAck(FCpacket packet) {
        buffer.setAck(packet);
    }

    /**
     * Timer Operations
     */
    public void startTimer(FCpacket packet) {
    /* Create, save and start timer for the given FCpacket */
        FC_Timer timer = new FC_Timer(timeoutValue, this, packet.getSeqNum());
        packet.setTimer(timer);
        timer.start();
    }

    public void cancelTimer(FCpacket packet) {
    /* Cancel timer for the given FCpacket */
        testOut("Cancel Timer for packet" + packet.getSeqNum());

        if (packet.getTimer() != null) {
            packet.getTimer().interrupt();
        }
    }

    /**
     * Implementation specific task performed at timeout
     */
    public void timeoutTask(long seqNum) {
        FCpacket packet = buffer.getPacket(seqNum);

        if (null != packet) {
            startTimer(packet);
            sendPacket(packet);
        }
    }

    /**
     * Computes the current timeout value (in nanoseconds)
     */
    public void computeTimeoutValue(long sampleRTT) {

        // ToDo
    }

    /**
     * Return value: FCPacket with (0 destPath;windowSize;errorRate)
     */
    public FCpacket makeControlPacket() {
   /* Create first packet with seq num 0. Return value: FCPacket with
     (0 destPath ; windowSize ; errorRate) */
        String sendString = destPath + ";" + windowSize + ";" + serverErrorRate;
        byte[] sendData = null;
        try {
            sendData = sendString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new FCpacket(0, sendData, sendData.length);
    }

    public void testOut(String out) {
        if (TEST_OUTPUT_MODE) {
            System.err.printf("%,d %s: %s\n", System.nanoTime(), Thread
                    .currentThread().getName(), out);
        }
    }

    public static void main(String argv[]) throws Exception {
        FileCopyClient myClient = new FileCopyClient(argv[0], argv[1], argv[2],
                argv[3], argv[4]);
        myClient.runFileCopyClient();
    }
}
