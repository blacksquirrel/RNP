package Test;

import Mail.Mail;

import java.io.*;

/**
 * Created by abb087 on 03.11.2014.
 */
public class TestFiles {

    public static void main(String[] a) throws IOException, ClassNotFoundException {


//        String [] dir = new File("Z:").list();
//
//        for(String d: dir) System.out.println(d);
//
//        File dirDa = new File("Z:\\Da");



//        File dir = new File();
//        System.out.println(dir.getAbsolutePath());
//
//        if(!dir.exists()) {
//            //Создаем его.
//            try {
//                dir.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }


        File dir = new File("mails");

//        if(!dir.exists()) {
//            //Создаем его.
//            try {
//                dir.createNewFile();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }


        dir.mkdir();
        delete(dir);

        String[] ss = dir.list();
        int count = 0;
        long bytes = 0;

        for(File f : dir.listFiles()){
            if(f.isFile() && f.getName().endsWith(".mail")){
                count++;
                bytes = bytes+f.length();

            }
        }

        System.out.println("empty dir size "+ count +" / "+ bytes);

        File file = new File(dir.getAbsolutePath() + "\\test2.mail");



        if( !file.exists()) {
            //Создаем его.
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("File test.mail exist: " + file.exists() + " und ist ein File: " + file.isFile() + file.getAbsolutePath());


        ss = dir.list();
        count = 0;

        bytes = 0;

        for(File f : dir.listFiles()){
            if(f.isFile() && f.getName().endsWith(".mail")){
                count++;
                bytes = bytes + f.length();
                System.out.println(f.getName());

            }
        }
        System.out.println("dir with " + count +"emptyfile size "+ bytes);

       Mail email = new Mail ("lokal", "me", ".");

       generateFile(file, email);


        System.out.println("Listing von Z:\\IdeaProjects\\RNP\\a2\\mails:");
        String[] entries = dir.list();
        for(String s: entries) System.out.println(s);

        System.out.println();

        for(File f: dir.listFiles()) {
            Mail mail = restoreMail(f);
            System.out.println(mail + ": " + mail.getUser() + " / " + mail.getHost() + " / " + mail.getEmail() + " / " + mail.getUidl());

            System.out.println(f.length() );

        }

        ss = dir.list();
        count = 0;

         bytes = 0;

        for(File f : dir.listFiles()){
            if(f.isFile() && f.getName().endsWith(".mail")){
                count++;
                bytes = bytes + f.length();
                System.out.println(f.getName());

            }
        }

        System.out.println("Size with " + count +" mail: " + bytes);
    }

    private static void delete(File dir) {
        for(File f : dir.listFiles()){
           f.delete();
        }

    }

    private static Mail restoreMail(File f) throws IOException, ClassNotFoundException {
        String name = f.getName();
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream o = new ObjectInputStream(fis);
        Mail mail = (Mail) o.readObject();
        fis.close();
        return  mail;
    }

    private static void generateFile(File file, Mail email) {
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
