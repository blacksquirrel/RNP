import java.io.Serializable;

/**
 * Created by maxim on 31.10.2014.
 */

public class Mail implements Serializable {

    private String host;
    private String user;
    private String email;
    private String uidl;

    public Mail(String user,String host, String email) {
        this.user = user;
        this.host = host;
        this.email = email;
        this.uidl = generateUIDL();
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public String getUidl() {
        return uidl;
    }

    private String generateUIDL() {

        long time = System.currentTimeMillis();
        int randomNummber = (int) (Math.random() * (Integer.MAX_VALUE-100));

        return "<"+Long.toString(time, 36) + "." + Integer.toString(randomNummber, 36) + "@" + host+">";
    }

}
