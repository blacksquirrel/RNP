package Tools;

import java.io.*;

/**
 * Created by maxim on 19.11.14.
 */
public class LogFile {

    private  File logFile;
    private int counter =1;

    public LogFile(String fileName) {

        File logDir = getFile("Logs");
        logDir.mkdirs();

        logFile = new File(logDir, fileName + ".txt");

        while (logFile.exists()) {
            logFile = new File(logDir, fileName + " " + ++counter + ".txt");
        }

        try {
            logFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Can't log file created");
        }
    }

    public void update(String newText) throws FileNotFoundException {

        StringBuilder sb = new StringBuilder();
        String oldFile = readFile(logFile.getAbsolutePath());
        sb.append(oldFile);
        sb.append(newText);
        writeFile(sb.toString());
        System.out.println(newText);
    }

    public  String readFile(String fileName) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        try {

            BufferedReader in = new BufferedReader(new FileReader(new File(fileName).getAbsoluteFile()));
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    public  void writeFile(String text) {

        PrintWriter out = null;
        try {
            out = new PrintWriter(logFile.getAbsoluteFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            out.print(text);
        } finally {
            out.close();
        }
    }

    //create File platform independent
    private File getFile(String name) {
        return new File(new File(new File(new File(new File(File.listRoots()[0], "Users"), "maxim"), "IdeaProjects"), "RNP"), name);
    }
}