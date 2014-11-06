package Server;

import Konto.Konto;
import Mail.MailsManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Created by maxim on 06.11.2014.
 */
public class CollectorStarter extends Thread {

    private static final long SLEEPTIME = 30000;
    private List<Konto> konten;
    private MailsManager mailsdrop;

    public CollectorStarter(MailsManager mailsdrop, ArrayList<Konto> konten) {
        this.mailsdrop = mailsdrop;

        if (konten != null) {
            this.konten = konten;
        } else {
            this.konten = new ArrayList<Konto>();

        }
    }

    public void run() {

        File file = new File("");
        List lines = null;
        try {
            lines = FileUtils.readLines(file, "UTF-8");
        } catch (IOException e) {
            System.out.println("lllllllllll");
        }

        int index = lines.size();
        System.out.println(index);
        String line;
        String [] param;
        while (true) {


            for (int i = 0; i <= index; i++) {
               line = (String) lines.get(i);
               param = line.trim().split(":");

                new CollectorThread(mailsdrop, new Konto(param[0],param[1],param[2],param[3])).start();
            }

            try {
                Thread.sleep(SLEEPTIME);
                System.out.println("small pause");
            } catch (InterruptedException e) {
                //e.printStackTrace();
                System.out.println("CollectorStarter have a problem with sleep");
            }
        }
    }
}
