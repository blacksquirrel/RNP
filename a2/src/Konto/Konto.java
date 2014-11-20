package Konto;

public class Konto {

    private String typ;
    private String user;
    private String password;
    private String host;
    private int port;

    public Konto(String typ, String user, String password, String host, String port) {
        this.typ = typ;
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = Integer.parseInt(port);
    }

    public String getTyp() {
        return typ;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
