import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    private int PORT = 80;
    public static int CONNECTED_USERS;
    public static int NEW_USER_ID;
    public static int CREATED_ROOMS;
    public static int NEW_ROOM_ID;

    public static ArrayList<Room> ROOMS;
    public static ArrayList<User> USERS;

    public static void connectedUsersPrompt() {
        System.out.println("Connected users = " + CONNECTED_USERS);
    }

    public static void createdRoomsPrompt() {
        System.out.println("Created rooms = " + CREATED_ROOMS);
    }

    public Main() {
        LogManager logManager = new LogManager();
        ServerSocket socket = null;
        try {
            CONNECTED_USERS = 0;
            CREATED_ROOMS = 0;
            NEW_USER_ID = 0;
            NEW_ROOM_ID = 0;
            ROOMS = new ArrayList<>();
            USERS = new ArrayList<>();
            System.out.println("Server started");
            connectedUsersPrompt();
            createdRoomsPrompt();
            socket = new ServerSocket(PORT);
            while (true) {
                Socket clientSocket = socket.accept();
                Runnable client = new ClientListener(clientSocket);
                new Thread(client).start();
            }
        } catch (Exception e) {
            logManager.addEntry(e.getMessage());
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ex) {
                    logManager.addEntry(ex.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}