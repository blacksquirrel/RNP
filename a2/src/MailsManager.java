import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxim on 31.10.2014.
 */
public class MailsManager {
private File dir;
    private final String CRLF = "\r\n";

    public MailsManager() {
        dir = new File("mails");
        dir.mkdir();
        makeEmpty();
    }

    public String getStat(){

        int count = 0;
        long mesagesInBytes = 0;

        for(File file : dir.listFiles()){
            if(file.isFile() && file.getName().endsWith(".mail")){
                count++;
                mesagesInBytes += file.length();

            }
        }

        return "+OK " +count + " " + mesagesInBytes;
    }

    public String getList() {

        int count = 0;
        long mesagesInBytes = 0;
        long oneMesageInBytes = 0;
        String mesagesList = "";


        for(File file : dir.listFiles()){
            if(file.isFile() && file.getName().endsWith(".mail")){
                count++;
                oneMesageInBytes = file.length();
                mesagesInBytes += oneMesageInBytes;
                mesagesList = mesagesList + count +" " +oneMesageInBytes + CRLF;
            }
        }
        return "+OK " + count + " " + mesagesInBytes + CRLF + mesagesList + ".";
    }

    public String getList(String s) {
        return null;
    }

    public String setRSET() {
        return null;
    }

    public String update() {
        return null;
    }

    public String getMessage(String s) {
        return null;
    }

    public String deleteMessage(String s) {
        return null;
    }

    public String getUIDL() {
        return null;
    }

    public String getUIDL(String s) {
        return null;
    }

    public void makeEmpty(){
        for(File f : dir.listFiles()){
            f.delete();
        }
    }

    public void saveNeuMail(String user, String host, String message) {

        Mail email = new Mail (user, host, message);

        File file = new File(dir.getAbsolutePath() + "\\" +email.getUidl());

        if( !file.exists()) {
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
