package Mail;

import java.io.*;

/**
 * Created by maxim on 31.10.2014.
 */
public class MailsManager {
    private final String CRLF = "\r\n";
    private File dir;

    public MailsManager() {

        dir = getFile("Maildrop");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //makeEmpty();
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
        long allMesagesInBytes = 0;

        for (File box : dir.listFiles()) {
            for (File file : box.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".mail")) {
                    count++;
                    allMesagesInBytes += file.length();
                }
            }
        }
        return "+OK " + count + " " + allMesagesInBytes + CRLF;
    }

    public String getList() {

        int count = 0;
        long allMesagesInBytes = 0;
        long oneMesageInBytes = 0;
        String mesagesList = "";

        for (File box : dir.listFiles()) {
            for (File file : box.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".mail")) {
                    count++;
                    oneMesageInBytes = file.length();
                    allMesagesInBytes += oneMesageInBytes;
                    mesagesList = mesagesList + count + " " + oneMesageInBytes + CRLF;
                }
            }
        }
        return "+OK " + count + " " + allMesagesInBytes + CRLF + mesagesList + "." + CRLF;
    }

    public String getList(String s) {

        if (s != null) {

            int count = 0;
            int suchMessageNummer = Integer.parseInt(s);

            for (File box : dir.listFiles()) {
                for (File file : box.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".mail") && suchMessageNummer == ++count) {
                        return "+OK " + s + " " + file.length() + CRLF;
                    }
                }
            }
        }
        return "-ERR no such message " + s + CRLF;
    }

    public String setRSET() {

        int count = 0;
        File rsetFile;
        String rsetFileName;

        for (File box : dir.listFiles()) {
            for (File file : box.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".mail")) {
                    count++;
                    rsetFileName = file.getName();
                    if (rsetFileName.startsWith("TODEL")) {
                        rsetFileName = rsetFileName.trim().split(" ")[1];
                        rsetFile = getFile(rsetFileName);
                        file.renameTo(rsetFile);
                    }
                }
            }
        }
        return "+OK maildrop have " + count + " message" + CRLF;
    }

    public String setUpdate() {

        for (File box : dir.listFiles()) {
            for (File file : box.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".mail") && file.getName().startsWith("TODEL")) {
                    file.delete();
                }
            }

        }
        return "+OK" + CRLF;
    }

    public String getMessage(String s) {

        if (s != null) {

            int suchMessageNummer = Integer.parseInt(s);
            String suchFile = "";
            int count = 0;

            for (File box : dir.listFiles()) {
                for (File file : box.listFiles()) {
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

        }
        return "-ERR no such message " + s + CRLF;
    }

    public String deleteMessage(String s) {

        if (s != null) {

            int count = 0;
            int suchMessageNummer = Integer.parseInt(s);
            File suchFile;
            for (File box : dir.listFiles()) {
                for (File file : box.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".mail") && suchMessageNummer == ++count) {
                        suchFile = new File(new File(getFile(dir.getName()), box.getName()), "/TODEL " + file.getName());
                        if (file.renameTo(suchFile)) {
                            return "+OK message " + s + " mark to delete" + CRLF;
                        }
                    }
                }
            }
        }
        return "-ERR no such message " + s + CRLF;
    }

    public String getUIDL() {

        int count = 0;
        String uidl = "";
        String mesagesList = "";


        for (File box : dir.listFiles()) {
            for (File file : box.listFiles()) {
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
        }
        return "+OK " + CRLF + mesagesList + "." + CRLF;
    }

    public String getUIDL(String s) {
        if (s != null) {

            int count = 0;
            int suchMessageNummer = Integer.parseInt(s);
            String uidl = "";
            for (File box : dir.listFiles()) {
                for (File file : box.listFiles()) {
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
        }
        return "-ERR no such message " + s + CRLF;
    }

    public void saveNeuMail(String user, String host, String message) {

        Mail email = new Mail(user, host, message);

        File userMailBox = new File(new File(dir.getAbsolutePath()), user);
        if (!userMailBox.exists()) {
            userMailBox.mkdirs();
        }

        File file = new File(userMailBox, email.getUidl());

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

    //--------------- Hilfsmethods -------------------------

    //if Mailboxdirectory not empty delete all files and childdirs
    private void makeEmpty() {
        for (File box : dir.listFiles()) {
            for (File file : box.listFiles()) {
                file.delete();
            }
        }
    }

    //create File platform independent
    private File getFile(String name) {
        return new File(new File(new File(new File(new File(File.listRoots()[0], "Users"), "maxim"), "IdeaProjects"), "RNP"), name);
    }
}
