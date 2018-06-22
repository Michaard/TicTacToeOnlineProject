import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ServerManagement {
    private final int CMD_LOGIN = 0;
    private final int CMD_CREATE_ROOM = 1;
    private final int CMD_JOIN_GAME = 2;
    private final int CMD_LEAVE_GAME = 3;
    private final int CMD_KICK_PLAYER = 4;
    private final int CMD_SEND_MSG = 5;
    private final int CMD_MAKE_MOVE = 6;
    private final int CMD_REMATCH = 7;
    private final int CMD_LOGOUT = 8;
    private final int CMD_DOWNLOAD_ROOMS = 10;

    private Socket socket;
    private BufferedReader serverPost;
    private PrintWriter clientPost;

    private String toHex(String arg) {
        return String.format("%x", new BigInteger(1, arg.getBytes()));
    }

    private String fromHex(String data) throws Exception {
        byte[] bytes = DatatypeConverter.parseHexBinary(data);
        return new String(bytes, "UTF-8");
    }

    private String commandLogin() {
        String command = CMD_LOGIN + "|" + getIp();
        System.out.println(command);
        return toHex(command);
    }

    private String commandLogout(int id) {
        String command = CMD_LOGOUT + "|" + getIp() + "|" + id;
        System.out.println(command);
        return toHex(command);
    }

    private String commandCreateRoom(String roomName, String host, int gameType, int chPass, String password) {
        String command;
        if (chPass == 0) {
            command = CMD_CREATE_ROOM + "|" + getIp() + "|" + roomName + "|" + host + "|" + gameType + "|" + chPass;
        } else {
            command = CMD_CREATE_ROOM + "|" + getIp() + "|" + roomName + "|" + host + "|" + gameType + "|" + chPass + "|" + password;
        }
        System.out.println(command);
        return toHex(command);
    }

    private String commandDownloadRooms(int id) {
        String command = CMD_DOWNLOAD_ROOMS + "|" + getIp() + "|" + id;
        System.out.println(command);
        return toHex(command);
    }

    private String commandJoinRoom(int roomId, String userName, int userId) {
        String command = CMD_JOIN_GAME + "|" + getIp() + "|" + roomId + "|" + userName + "|" + userId;
        System.out.println(command);
        return toHex(command);
    }

    private String commandLeaveRoom(int roomId, int userId) {
        String command = CMD_LEAVE_GAME + "|" + getIp() + "|" + roomId + "|" + userId;
        System.out.println(command);
        return toHex(command);
    }

    private String commandSendMsg(int roomId, int userId, String msg) {
        String command = CMD_SEND_MSG + "|" + getIp() + "|" + roomId + "|" + userId + "|" + msg;
        System.out.println(command);
        return toHex(command);
    }

    private String commandMakeMove(int roomId, int userId, int i, int j, String userChar) {
        String command = CMD_MAKE_MOVE + "|" + getIp() + "|" + roomId + "|" + userId + "|" + i + "|" + j + "|" + userChar;
        System.out.println(command);
        return toHex(command);
    }

    private String getIp() {
        try {
            InetAddress ipAddr = InetAddress.getLocalHost();
            return ipAddr.getHostAddress();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ServerManagement(String host, int port) throws Exception {
        socket = new Socket(host, port);
        serverPost = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        clientPost = new PrintWriter(socket.getOutputStream(), true);
        //br = new BufferedReader(new InputStreamReader(System.in));
    }

    public int loginUser() throws Exception {
        String response;
        int userId;

        String commandLogin = commandLogin();
        System.out.println("Command: " + commandLogin);
        clientPost.println(commandLogin);

        response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);
        if (response.equals(CMD_LOGIN + "0") || !response.equals(CMD_LOGIN + "1"))
            throw new Exception("Cannot login to the server!");
        userId = Integer.parseInt(fromHex(serverPost.readLine()));
        return userId;
    }

    public void logoutUser(int id) throws Exception {
        String response;
        String commandLogout = commandLogout(id);
        System.out.println("Command: " + commandLogout);
        clientPost.println(commandLogout);

        response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);
        if (response.equals(CMD_LOGOUT + "0") || !response.equals(CMD_LOGOUT + "1"))
            throw new Exception("Cannot logout from the server!");
        socket.close();
        clientPost.close();
        serverPost.close();
    }

    public int createRoom(String roomName, String host, int gameType, int chPass, String password) throws Exception {
        String response;
        int roomId;
        String commandCreateRoom = commandCreateRoom(roomName, host, gameType, chPass, password);
        System.out.println("Command: " + commandCreateRoom);
        clientPost.println(commandCreateRoom);

        response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);
        if (response.equals(CMD_CREATE_ROOM + "0") || !response.equals(CMD_CREATE_ROOM + "1"))
            throw new Exception("Cannot create new game room!");
        roomId = Integer.parseInt(fromHex(serverPost.readLine()));
        return roomId;
    }

    public ArrayList<Room> getRooms(int id) throws Exception {
        ArrayList<Room> rooms = new ArrayList<>();

        String response;
        int roomsCount;

        String commandGetRooms = commandDownloadRooms(id);
        System.out.println("Command: " + commandGetRooms);
        clientPost.println(commandGetRooms);

        response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);

        if (response.equals(CMD_DOWNLOAD_ROOMS + "0") || !response.equals(CMD_DOWNLOAD_ROOMS + "1"))
            throw new Exception("Cannot download rooms!");

        response = serverPost.readLine();
        System.out.println("Rooms count in hex: " + response);
        roomsCount = Integer.parseInt(fromHex(response));
        System.out.println("Rooms count: " + roomsCount);

        for (int i = 0; i < roomsCount; i++) {
            response = serverPost.readLine();
            System.out.println("Response in hex: " + response);
            response = fromHex(response);
            System.out.println("Response: " + response);
            String[] roomData = response.split("\\|");
            rooms.add(new Room(Integer.parseInt(roomData[0]), roomData[1], roomData[2], roomData[3], roomData[4], Integer.parseInt(roomData[5])));
        }

        response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);
        if (response.equals(CMD_DOWNLOAD_ROOMS + "0") || !response.equals(CMD_DOWNLOAD_ROOMS + "1"))
            throw new Exception("Rooms download failed!");

        return rooms;
    }

    public boolean joinRoom(int roomId, String userName, int userId) throws Exception {
        String response;
        String commandJoinRoom = commandJoinRoom(roomId, userName, userId);
        System.out.println("Command: " + commandJoinRoom);
        clientPost.println(commandJoinRoom);

        response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);
        if (response.equals(CMD_JOIN_GAME + "2")) {
            String password = JOptionPane.showInputDialog("Room password:");

            if (password == null) {
                System.out.println("NULL");
                password = "";
            }

            clientPost.println(toHex(password));

            response = serverPost.readLine();
            System.out.println("Response in hex: " + response);
            response = fromHex(response);
            System.out.println("Response: " + response);
            if (response.equals("PASSWORD EMPTY")) {
                response = serverPost.readLine();
                System.out.println("Response in hex: " + response);
                response = fromHex(response);
                System.out.println("Response: " + response);
                return false;
            }
            if (!response.equals("PASSWORD OK")) {
                response = serverPost.readLine();
                System.out.println("Response in hex: " + response);
                response = fromHex(response);
                System.out.println("Response: " + response);
                throw new Exception("Incorrect password!");
            }
        } else if (response.equals(CMD_JOIN_GAME + "0") || !response.equals(CMD_JOIN_GAME + "1"))
            throw new Exception("Cannot join the game room!");
        return true;
    }

    public String leaveRoom(int roomId, int userId) throws Exception {
        String response;
        String commandLeaveRoom = commandLeaveRoom(roomId, userId);
        System.out.println("Command: " + commandLeaveRoom);
        clientPost.println(commandLeaveRoom);

        String result = serverPost.readLine();
        System.out.println("Response in hex: " + result);
        result = fromHex(result);
        System.out.println("Response: " + result);

        response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);
        if (response.equals(CMD_LEAVE_GAME + "0") || !response.equals(CMD_LEAVE_GAME + "1"))
            throw new Exception("Something went wrong while leaving the room!");
        return result;
    }

    public String gameRoomListener() throws Exception {
        String response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);
        return response;
    }

    public void sendChatBoxMsg(int roomId, int userId, String msg) throws Exception {
        String response;
        String commandSendMsg = commandSendMsg(roomId, userId, msg);
        System.out.println("Command: " + commandSendMsg);
        clientPost.println(commandSendMsg);

        response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);
        if (response.equals(CMD_SEND_MSG + "0") || !response.equals(CMD_SEND_MSG + "1"))
            throw new Exception("Cannot send message!");
    }

    public void makeMove(int roomId, int userId, int i, int j, String userChar) throws Exception {
        String response;
        String commandMakeMove = commandMakeMove(roomId, userId, i, j, userChar);
        System.out.println("Command: " + commandMakeMove);
        clientPost.println(commandMakeMove);

        response = serverPost.readLine();
        System.out.println("Response in hex: " + response);
        response = fromHex(response);
        System.out.println("Response: " + response);
        if (response.equals(CMD_MAKE_MOVE + "0") || !response.equals(CMD_MAKE_MOVE + "1"))
            throw new Exception("Cannot make that move!");
    }
}
