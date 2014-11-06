package Mail;

import Mail.Mail;

import java.io.*;

/**
 * Created by maxim on 31.10.2014.
 */
public class MailsManager {
    private final String CRLF = "\r\n";
    private File dir;
    private String DIRNAME = "maildrop";

    public MailsManager() {
        dir = new File(DIRNAME);
        dir.mkdir();
        makeEmpty();
    }

    private static Mail restoreMailFromFile(File f) throws IOException, ClassNotFoundException {
        String name = f.getName();
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream o = new ObjectInputStream(fis);
        Mail mail = (Mail) o.readObject();
        fis.close();
        return mail;
    }

    public String getStat() {

        int count = 0;
        long mesagesInBytes = 0;

        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".mail")) {
                count++;
                mesagesInBytes += file.length();

            }
        }
        return "+OK " + count + " " + mesagesInBytes + CRLF;
    }

    public String getList() {

        int count = 0;
        long mesagesInBytes = 0;
        long oneMesageInBytes = 0;
        String mesagesList = "";

        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".mail")) {
                count++;
                oneMesageInBytes = file.length();
                mesagesInBytes += oneMesageInBytes;
                mesagesList = mesagesList + count + " " + oneMesageInBytes + CRLF;
            }
        }
        return "+OK " + count + " " + mesagesInBytes + CRLF + mesagesList + "." + CRLF;
    }

    public String getList(String s) {

        if (s != null) {

            int count = 0;
            int suchMessageNummer = Integer.getInteger(s);

            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".mail") && suchMessageNummer == ++count) {
                    return "+OK " + s + " " + file.length() + CRLF;
                }
            }
        }
        return "-ERR no such message " + s + CRLF;
    }

    public String setRSET() {

        int count = 0;
        File rsetFile;
        String rsetFileName;

        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".mail")) {
                count++;
                rsetFileName = file.getName();
                if (rsetFileName.startsWith("TODEL")) {
                    rsetFileName = rsetFileName.trim().split("//s+")[1];
                    rsetFile = new File(rsetFileName);
                    file.renameTo(rsetFile);
                }
            }

        }
        return "+OK maildrop have " + count + " message" + CRLF;
    }

    public String setUpdate() {

        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".mail") && file.getName().startsWith("TODEL")) {
                file.delete();
            }

        }
        return "+OK" + CRLF;
    }

    public String getMessage(String s) {

        if (s != null) {

            int count = 0;
            int suchMessageNummer = Integer.getInteger(s);
            String suchFile = "";

            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".mail") && suchMessageNummer == ++count) {
                    try {
                        suchFile = restoreMailFromFile(file).getEmail();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return "+OK message " + s + file.length() + CRLF + suchFile + CRLF;
                }
            }

        }
        return "-ERR no such message " + s+CRLF;
    }

    public String deleteMessage(String s) {

        if (s != null) {

            int count = 0;
            int suchMessageNummer = Integer.getInteger(s);
            File suchFile;

            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".mail") && suchMessageNummer == ++count) {
                    suchFile = new File("TODEL " + file.getName());
                    if (file.renameTo(suchFile)) {
                        return "+OK message " + s + " deleted" + CRLF;
                    }
                }
            }
        }
        return "-ERR no such message " + s+ CRLF;
    }

    public String getUIDL() {

        int count = 0;
        String uidl = "";
        String mesagesList = "";


        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".mail")) {
                count++;
                try {
                    uidl = restoreMailFromFile(file).getUidl();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                mesagesList = mesagesList + count + " " + uidl + CRLF;
            }
        }
        return "+OK " + CRLF + mesagesList + "."+CRLF;
    }

    public String getUIDL(String s) {
        if (s != null) {

            int count = 0;
            int suchMessageNummer = Integer.getInteger(s);
            String uidl = "";
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".mail") && suchMessageNummer == ++count) {
                    try {
                        uidl = restoreMailFromFile(file).getUidl();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return "+OK " + s + " " + uidl + CRLF;
                }
            }
        }
        return "-ERR no such message " + s+CRLF;
    }

    private void makeEmpty() {
        for (File f : dir.listFiles()) {
            f.delete();
        }
    }

    public void saveNeuMail(String user, String host, String message) {

        Mail email = new Mail(user, host, message);

        File file = new File(dir.getAbsolutePath() + "\\" + email.getUidl());

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fis = null;
        try {
            fis = new FileOutputStream(file);
            ObjectOutputStream o = null;
            o = new ObjectOutputStream(fis);
            o.writeObject(email);
            o.flush();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
