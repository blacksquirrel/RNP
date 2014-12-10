import java.io.*;

public class Starter {

    public static void main(String[] args) {

//        FileCopyServer myServer = new FileCopyServer();
//        try {
//            myServer.runFileCopyServer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //read ConfigFile
        String serverName = null;
        String sourcePath = null;
        String destPath = null;
        String windowSize = null;
        String errorRate = null;
        int configArgs = 0;

        File configFile = new File(new File(new File(new File(new File(new File(new File(
                File.listRoots()[0], "Users"), "maxim"), "IdeaProjects"), "RNP"), "a3"), "config"), "Config.txt");
        if (!configFile.exists()) System.exit(0);
        String[] config;

        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = br.readLine()) != null) {
                config = line.trim().split(":");

                if (config[1] == null || config[1].isEmpty()) continue;

                switch (config[0]) {
                    case ("ServerName"):
                        serverName = config[1];
                        configArgs++;
                        break;
                    case ("SourcePath"):
                        sourcePath = config[1];
                        System.out.println(sourcePath);
                        configArgs++;
                        break;
                    case ("DestPath"):
                        destPath = config[1];
                        configArgs++;
                        break;
                    case ("WindowSizeArg"):
                        windowSize = config[1];
                        configArgs++;
                        break;
                    case ("ErrorRate"):
                        errorRate = config[1];
                        configArgs++;
                }
            }
            br.close();

        } catch (FileNotFoundException e) {
            System.out.println("Config File not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Config File is empty");
            e.printStackTrace();
        }

        //start Client
        if (configArgs != 5) System.exit(0);

        FileCopyClient myClient = new FileCopyClient(serverName, sourcePath, destPath, windowSize, errorRate);
        myClient.runFileCopyClient();
    }
}
