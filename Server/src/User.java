
import java.io.PrintWriter;
import java.net.Socket;

public class User {
    private int id;
    private String ip;
    private String name;
    private Socket socket;
    private PrintWriter serverPost;
    private int score;

    public User(int id, Socket socket, PrintWriter printWriter) {
        this.id = id;
        this.socket = socket;
        this.serverPost = printWriter;
        score = 0;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void sendToUser(String msg) throws Exception{
        serverPost.println(msg);
    }

    public void addPoint(){
        score++;
    }

    public int getScore(){
        return score;
    }

    public void clearScore(){
        score=0;
    }
}
