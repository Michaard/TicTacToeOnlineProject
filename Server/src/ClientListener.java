import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;

public class ClientListener implements Runnable {
    private final int CMD_LOGIN = 0;
    private final int CMD_CREATE_ROOM = 1;
    private final int CMD_JOIN_GAME = 2;
    private final int CMD_LEAVE_GAME = 3;
    private final int CMD_KICK_PLAYER = 4;
    private final int CMD_SEND_MSG = 5;
    private final int CMD_MAKE_MOVE = 6;
    private final int CMD_REMATCH = 7;
    private final int CMD_LOGOUT = 8;
    private final int CMD_DESTROY_ROOM = 9;
    private final int CMD_SEND_ROOMS = 10;

    private final int GL_OPPONENT_CONNECTED = 201;
    private final int GL_OPPONENT_MOVED = 202;
    private final int GL_OPPONENT_SENT_MSG = 203;
    private final int GL_OPPONENT_LEFT = 204;
    private final int GL_GAME_OVER = 205;

    private Socket clientSocket;
    private BufferedReader clientPost;
    private PrintWriter serverPost;
    private User user;
    private LogManager logManager;

    private int X = 1;
    private int O = 2;

    private String toHex(String arg) {
        return String.format("%x", new BigInteger(1, arg.getBytes()));
    }

    private String fromHex(String data) throws Exception {
        byte[] bytes = DatatypeConverter.parseHexBinary(data);
        return new String(bytes, "UTF-8");
    }

    ClientListener(Socket in) {
        clientSocket = in;
        logManager = new LogManager();
    }

    private void requestFailedResponse(Exception e, String response) {
        System.out.println("Response: " + response);
        serverPost.println(toHex(response));
        System.out.println(e.getMessage());
        logManager.addEntry(e.getMessage());
    }

    public void run() {
        String entry = "User connected.";
        System.out.println(entry);
        logManager.addEntry(entry);
        try {
            clientPost = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            serverPost = new PrintWriter(clientSocket.getOutputStream(), true);

            user = new User(Main.NEW_USER_ID, clientSocket, serverPost);
            Main.NEW_USER_ID++;
            Main.USERS.add(user);

            String clientCmd;
            int command;

            while (true) {
                clientCmd = fromHex(clientPost.readLine());
                System.out.println("Received cmd: " + clientCmd);
                String[] commandStr = clientCmd.split("\\|");
                command = Integer.parseInt(commandStr[0]);

                switch (command) {
                    case CMD_LOGIN:
                        loginUser(clientCmd);
                        break;
                    case CMD_LOGOUT:
                        logoutUser(clientCmd);
                        return;
                    case CMD_CREATE_ROOM:
                        createRoom(clientCmd);
                        break;
                    case CMD_SEND_ROOMS:
                        sendRooms(clientCmd);
                        break;
                    case CMD_JOIN_GAME:
                        addUserToTheGame(clientCmd);
                        break;
                    case CMD_LEAVE_GAME:
                        destroyRoom(clientCmd);
                        break;
                    case CMD_SEND_MSG:
                        sendMsg(clientCmd);
                        break;
                    case CMD_MAKE_MOVE:
                        makeMove(clientCmd);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e1) {
            Main.CONNECTED_USERS--;
            Main.connectedUsersPrompt();
            System.out.println(e1.getMessage());
            logManager.addEntry(e1.getMessage());
        } catch (Exception e2) {
            entry = "User " + user.getName() + " disconnected.";
            System.out.println(entry);
            logManager.addEntry(entry);
        }
    }

    private void loginUser(String command) {
        String response;
        try {
            String[] data = command.split("\\|");
            String ip = data[1];
            Main.CONNECTED_USERS++;
            Main.connectedUsersPrompt();
            user.setIp(ip);

            response = CMD_LOGIN + "1";
            System.out.println("Response: " + response);
            serverPost.println(toHex(response));
            serverPost.println(toHex(String.valueOf(user.getId())));

            String entry = "User id: " + user.getId() + " ip: " + user.getIp() + " logged in.";
            System.out.println(entry);
            logManager.addEntry(entry);
        } catch (Exception e) {
            requestFailedResponse(e, CMD_LOGIN + "0");
        }
    }

    private void logoutUser(String command) {
        String response;
        try {
            String[] data = command.split("\\|");
            String ip = data[1];
            int userId = Integer.parseInt(data[2]);
            Main.connectedUsersPrompt();

            ArrayList<Room> newRooms = new ArrayList<>();
            for (Room room : Main.ROOMS) {
                if (room.getHostId() != user.getId()) {
                    newRooms.add(room);
                }
            }
            Main.ROOMS.clear();
            Main.ROOMS = newRooms;

            Main.USERS.remove(user);

            Main.CONNECTED_USERS--;

            response = CMD_LOGOUT + "1";
            System.out.println("Response: " + response);
            serverPost.println(toHex(response));
            clientSocket.close();
            serverPost.close();
            clientPost.close();

            String entry = "User id: " + user.getId() + " ip: " + user.getIp() + " logged out.";
            System.out.println(entry);
            logManager.addEntry(entry);
        } catch (Exception e) {
            requestFailedResponse(e, CMD_LOGOUT + "0");
        }
    }

    private void createRoom(String command) {
        String response;
        try {
            String[] data = command.split("\\|");
            String ip = data[1];
            String roomName = data[2];
            String hostName = data[3];
            int gameTypeInt = Integer.parseInt(data[4]);
            int chPass = Integer.parseInt(data[5]);
            String password = "";
            boolean chPassword = false;

            if (chPass == 1) {
                chPassword = true;
                password = data[6];
            }

            user.setIp(ip);
            user.setName(hostName);

            Room newRoom = new Room(Main.NEW_ROOM_ID, roomName, gameTypeInt, chPassword, password, user);
            Main.ROOMS.add(newRoom);
            response = CMD_CREATE_ROOM + "1";
            System.out.println("Response: " + response);
            serverPost.println(toHex(response));
            serverPost.println(toHex(String.valueOf(Main.NEW_ROOM_ID)));

            String entry = "User " + user.getId() + ". " + user.getName() + " ip: " + user.getIp() + " created a room " + Main.NEW_ROOM_ID + ". " + roomName + ".";
            System.out.println(entry);
            logManager.addEntry(entry);

            Main.NEW_ROOM_ID++;
            Main.CREATED_ROOMS++;
            Main.createdRoomsPrompt();

        } catch (Exception e) {
            requestFailedResponse(e, CMD_CREATE_ROOM + "0");
        }
    }

    private void addUserToTheGame(String command) {
        String response;
        try {
            String[] data = command.split("\\|");
            String ip = data[1];
            int roomId = Integer.parseInt(data[2]);
            String userName = data[3];
            int userId = Integer.parseInt(data[4]);

            Room roomToJoin = null;
            for (Room room : Main.ROOMS) {
                if (room.getId() == roomId) {
                    roomToJoin = room;
                    break;
                }
            }

            if (roomToJoin == null) throw new Exception("Room " + roomId + " not found.");

            user.setName(userName);
            user.setIp(ip);

            if (roomToJoin.getGuest() != null) throw new Exception("Room: " + roomToJoin.getId() + " - Room is full!");

            if (roomToJoin.getChPassword()) {
                serverPost.println(toHex(CMD_JOIN_GAME + "2"));
                response = clientPost.readLine();
                if (!response.equals("0")) response = fromHex(response);
                System.out.println("Received password: " + response);
                if (response == null || response.equals("") || response.equals("0")) {
                    serverPost.println(toHex("PASSWORD EMPTY"));
                    logManager.addEntry("Room " + roomId + ": user typed empty password or canceled connection.");
                    serverPost.println(toHex(CMD_JOIN_GAME + "3"));
                    return;
                } else if (response.equals(roomToJoin.getPassword())) {
                    serverPost.println(toHex("PASSWORD OK"));
                } else {
                    serverPost.println(toHex("PASSWORD WRONG"));
                    throw new Exception("Room " + roomId + ": user typed wrong password.");
                }
            } else {
                serverPost.println(toHex(CMD_JOIN_GAME + "1"));
            }
            roomToJoin.addPlayer(user);

            String msg = GL_OPPONENT_CONNECTED + "|" + user.getName();

            roomToJoin.getHost().sendToUser(toHex(msg));

            String entry = "User " + userName + " has joined the room " + roomToJoin.getName() + ".";
            System.out.println(entry);
            logManager.addEntry(entry);
        } catch (Exception e) {
            requestFailedResponse(e, CMD_JOIN_GAME + "0");
        }
    }

    private void sendRooms(String command) {
        String response;
        try {
            String[] data = command.split("\\|");
            String ip = data[1];
            int userId = Integer.parseInt(data[2]);

            response = CMD_SEND_ROOMS + "1";
            serverPost.println(toHex(response));

            int roomsCount = Main.ROOMS.size();
            serverPost.println(toHex(String.valueOf(roomsCount)));

            for (Room room : Main.ROOMS) {
                serverPost.println(toHex(room.toString()));
            }

            response = CMD_SEND_ROOMS + "1";
            System.out.println("Response: " + response);
            serverPost.println(toHex(response));

            String entry = "User id: " + user.getId() + " ip: " + user.getIp() + " downloaded the rooms list.";
            System.out.println(entry);
            logManager.addEntry(entry);

        } catch (Exception e) {
            requestFailedResponse(e, CMD_SEND_ROOMS + "0");
        }
    }

    private void destroyRoom(String command) {
        String response;
        try {
            String[] data = command.split("\\|");
            String ip = data[1];
            int roomId = Integer.parseInt(data[2]);
            int userId = Integer.parseInt(data[3]);

            Room roomToDestroy = null;
            User roomHost;
            User roomGuest;
            for (Room room : Main.ROOMS) {
                if (room.getId() == roomId) {
                    roomToDestroy = room;
                }
            }

            roomHost = roomToDestroy.getHost();
            roomGuest = roomToDestroy.getGuest();
            String entry;
            if (roomGuest != null && userId == roomHost.getId()) {
                entry = "User " + roomHost.getName() + " closed the room " + roomToDestroy.getName() + ".";
                roomHost.sendToUser(toHex(String.valueOf(CMD_DESTROY_ROOM)));
                roomHost.sendToUser(toHex(entry));
                roomGuest.sendToUser(toHex(String.valueOf(GL_OPPONENT_LEFT)));
            } else if (roomGuest == null) {
                entry = "User " + roomHost.getName() + " closed the room " + roomToDestroy.getName() + ".";
                roomHost.sendToUser(toHex(String.valueOf(CMD_DESTROY_ROOM)));
                roomHost.sendToUser(toHex(entry));
            } else {
                entry = "User " + roomGuest.getName() + " left the room " + roomToDestroy.getName() + ".";
                roomGuest.sendToUser(toHex(String.valueOf(CMD_DESTROY_ROOM)));
                roomGuest.sendToUser(toHex(entry));
                roomHost.sendToUser(toHex(String.valueOf(GL_OPPONENT_LEFT)));
            }

            roomHost.clearScore();
            if (roomGuest != null) roomGuest.clearScore();

            Main.ROOMS.remove(roomToDestroy);
            Main.CREATED_ROOMS--;
            response = CMD_LEAVE_GAME + "1";
            System.out.println("Response: " + response);
            serverPost.println(toHex(response));

            System.out.println(entry);
            logManager.addEntry(entry);

            entry = "Room " + roomToDestroy.getName() + " has been closed.";
            System.out.println(entry);
            logManager.addEntry(entry);

        } catch (Exception e) {
            serverPost.println(toHex(CMD_LEAVE_GAME + "0"));
            requestFailedResponse(e, CMD_LEAVE_GAME + "0");
        }
    }

    private void sendMsg(String command) {
        String response;
        try {
            String[] data = command.split("\\|");
            String ip = data[1];
            int roomId = Integer.parseInt(data[2]);
            int userId = Integer.parseInt(data[3]);
            String msg = data[4];

            Room operatingRoom = null;
            User roomHost;
            User roomGuest;

            for (Room room : Main.ROOMS) {
                if (room.getId() == roomId) {
                    operatingRoom = room;
                }
            }

            roomHost = operatingRoom.getHost();
            roomGuest = operatingRoom.getGuest();

            response = GL_OPPONENT_SENT_MSG + "|" + user.getName() + "|" + msg;
            if (userId == roomHost.getId()) {
                roomGuest.sendToUser(toHex(response));
            } else {
                roomHost.sendToUser(toHex(response));
            }
            response = CMD_SEND_MSG + "1";
            serverPost.println(toHex(response));
            System.out.println("Response: " + response);
            serverPost.println(toHex(response));
            logManager.addEntry("User " + userId + " send message: " + msg);
        } catch (Exception e) {
            serverPost.println(toHex(CMD_SEND_MSG + "0"));
            requestFailedResponse(e, CMD_SEND_MSG + "0");
        }
    }

    private void makeMove(String command) {
        String response;
        try {
            String[] data = command.split("\\|");
            String ip = data[1];
            int roomId = Integer.parseInt(data[2]);
            int userId = Integer.parseInt(data[3]);
            int i = Integer.parseInt(data[4]);
            int j = Integer.parseInt(data[5]);
            String userChar = data[6];

            Room operatingRoom = null;
            User roomHost;
            User roomGuest;

            for (Room room : Main.ROOMS) {
                if (room.getId() == roomId) {
                    operatingRoom = room;
                }
            }

            roomHost = operatingRoom.getHost();
            roomGuest = operatingRoom.getGuest();

            response = GL_OPPONENT_MOVED + "|" + user.getName() + "|" + i + "|" + j + "|" + userChar;
            if (userId == roomHost.getId()) {
                roomGuest.sendToUser(toHex(response));
            } else {
                roomHost.sendToUser(toHex(response));
            }
            response = CMD_MAKE_MOVE + "1";
            serverPost.println(toHex(response));
            System.out.println("Response: " + response);
            serverPost.println(toHex(response));

            int c;
            if (userChar.equals("X")) c = X;
            else c = O;

            operatingRoom.getBoard().putCharAt(c, i, j);

            gameOver(operatingRoom, i, j, c);
        } catch (Exception e) {
            serverPost.println(toHex(CMD_MAKE_MOVE + "0"));
            requestFailedResponse(e, CMD_MAKE_MOVE + "0");
        }

    }

    private void gameOver(Room room, int i, int j, int c) {
        try {
            boolean gameIsFinished = room.getBoard().isFinished(i, j);
            boolean gameIsOver = room.getBoard().isFull();
            if (gameIsFinished) {
                User winner;
                if (c == X) {
                    winner = room.getHost();
                } else {
                    winner = room.getGuest();
                }
                winner.addPoint();

                String entry = "Room: " + room.getName() + " - " + winner.getName() + " has won the game!";
                System.out.println(entry);
                String response = GL_GAME_OVER + "|" + winner.getId() + "|" + winner.getName() + "|" + winner.getScore();
                room.getHost().sendToUser(toHex(response));
                room.getGuest().sendToUser(toHex(response));

                room.getBoard().clear();
                logManager.addEntry(entry);
            } else if (gameIsOver) {
                String entry = "Room: " + room.getName() + " - It's a tie!";
                System.out.println(entry);
                logManager.addEntry(entry);
                String response = GL_GAME_OVER + "|" + -1 + "|" + null + "|" + -1;
                room.getHost().sendToUser(toHex(response));
                room.getGuest().sendToUser(toHex(response));

                room.getBoard().clear();
            }
        } catch (Exception e) {
            String entry = "Game over error ocured! " + e.getMessage();
            System.out.println(entry);
            logManager.addEntry(entry);
        }
    }
}
