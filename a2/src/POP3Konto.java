public class POP3Konto {

    private String user;
    private String password;
    private String serverAdress;
    private int port;

    public POP3Konto(String user, String password, String serverAdress, int port) {
        this.user = user;
        this.password = password;
        this.serverAdress = serverAdress;
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getServer() {
        return serverAdress;
    }

    public int getPort() {
        return port;
    }
}
