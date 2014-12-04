import java.util.HashMap;
import java.util.LinkedList;

public class PacketBuffer {
    private FileCopyClient fileCopyClient;
    private LinkedList<FCpacket> packetSend;
    private HashMap<Long, FCpacket> packetAcked;
    private int windowSize;
    private long sendBase = 0;

    public PacketBuffer(FileCopyClient fileCopyClient, int windowSize) {
        this.fileCopyClient = fileCopyClient;
        this.windowSize = windowSize;
        this.packetSend = new LinkedList();
        this.packetAcked = new HashMap<Long, FCpacket>();
    }

    public boolean ad(FCpacket p) {
        if (!isFull()) {
            packetSend.add(p);
            return true;
        }
        return false;
    }

    // ACK(n) empfangen und Paket n ist im Sendepuffer
    // Markiere Paket n als quittiert
    // Timer für Paket n stoppen
    public boolean setAck(FCpacket p) {
        for (FCpacket packet : packetSend) {
            if (p.getSeqNum() == packet.getSeqNum()) {
                fileCopyClient.cancelTimer(packet);
                packetAcked.put(packet.getSeqNum(), packet);
                packetSend.remove(packet);
                chekSendBase(packet.getSeqNum());
                return true;
            }
        }
        return false;
    }

    // Wenn n = sendbase, dann lösche ab n alle Pakete,
    // bis ein noch nicht quittiertes Paket im Sendepuffer erreicht ist,
    // und setze sendbase auf dessen Sequenznummer
    private void chekSendBase(long packetSeqNum) {

        if (sendBase == packetSeqNum) {
            packetAcked.remove(packetSeqNum);
            //trimToSize
            packetAcked = new HashMap<Long, FCpacket>(packetAcked);

            sendBase++;
            chekSendBase(sendBase);
        }
    }

    public boolean isFull() {
        return packetSend.size() + packetAcked.size() == windowSize;
    }

    public FCpacket getPacket(long seqNum) {
        for (FCpacket p : packetSend) {
            if (p.getSeqNum() == seqNum) {
                return p;
            }
        }
        return null;
    }
}
