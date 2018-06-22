import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainWindow extends JFrame {
    private final int WINDOW_WIDTH = 500;
    private final int WINDOW_HEIGHT = 400;

    private final static String TITLE = "TicTacToe Online";
    private DefaultTableModel TABLE_MODEL;

    private ServerManagement serverManagement;

    private int USER_ID;
    private int ROOM_ID;
    private GameRoom GAME_ROOM;

    private JPanel PANEL_MAIN;
    private JTextField TF_USER_NAME;
    private JTable TABLE_ROOMS;
    private JButton BTN_HOST;
    private JButton BTN_JOIN;
    private JButton BTN_EXIT;
    private JTextArea TA_SERVER_MSGS;
    private JButton BTN_REFRESH;

    private int SELECTED_TABLE_ROW;

    public void showErrorMsg(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void sendRoomData(String roomName, int gameType, int chPass, String password) {
        try {
            if (serverManagement != null) {
                String userName = TF_USER_NAME.getText();
                if (!userName.equals("")) {
                    ROOM_ID = serverManagement.createRoom(roomName, userName, gameType, chPass, password);
                    GAME_ROOM = new GameRoom(1, roomName, ROOM_ID, gameType, userName, userName, USER_ID, MainWindow.this);
                    setVisible(false);
                } else {
                    showErrorMsg("Please provide user name!");
                }
            } else {
                showErrorMsg("Connection error occured!");
                setVisible(true);
            }
        } catch (Exception e) {
            setVisible(true);
            showErrorMsg(e.getMessage());
        }
    }

    public void leaveRoom(int roomId) {
        try {
            if (serverManagement != null) {
                String response = serverManagement.leaveRoom(roomId, USER_ID);
                GAME_ROOM = null;
                downloadRooms();
                displayServerMsg(response);
            }
        } catch (Exception e) {
            System.out.println("ERROR");
            showErrorMsg(e.getMessage());
        }
    }

    public void leaveRoom() {
        try {
            GAME_ROOM = null;
            displayServerMsg("Your opponent left the game!");
            downloadRooms();
        } catch (Exception e) {
            System.out.println("ERROR");
            showErrorMsg(e.getMessage());
        }
    }

    public String gameRoomListener() {
        try {
            if (serverManagement != null) {
                String request = serverManagement.gameRoomListener();
                System.out.println("Request from the server: " + request);
                return request;
            }
            return null;
        } catch (Exception e) {
            showErrorMsg(e.getMessage());
            return null;
        }
    }

    public void sendChatBoxMsg(String msg) {
        try {
            if (serverManagement != null) {
                serverManagement.sendChatBoxMsg(ROOM_ID, USER_ID, msg);
            }
        } catch (Exception e) {
            showErrorMsg(e.getMessage());
        }
    }

    public void sendMoveInfo(int i, int j, String userChar) {
        try {
            if (serverManagement != null) {
                serverManagement.makeMove(ROOM_ID, USER_ID, i, j, userChar);
            }
        } catch (Exception e) {
            showErrorMsg(e.getMessage());
        }
    }

    private void addRoomTableRow(int id, String roomName, String host, String gameType, String password, int players) {
        TABLE_MODEL.addRow(new Object[]{id, roomName, host, gameType, password, players + "/2"});
    }

    private void downloadRooms() {
        TABLE_MODEL = new TableModel(new String[]{"ID", "Room", "Host", "Game Type", "Password", "Players"}, 0);
        TABLE_ROOMS.setModel(TABLE_MODEL);
        try {
            if (serverManagement != null) {
                ArrayList<Room> rooms = serverManagement.getRooms(USER_ID);
                for (Room room : rooms) {
                    addRoomTableRow(room.getId(), room.getName(), room.getHost(), room.getGameType(), room.getPassword(), room.getPlayers());
                }
            }
        } catch (Exception e) {
            showErrorMsg(e.getMessage());
        }
    }

    private void displayServerMsg(String msg) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        TA_SERVER_MSGS.append("[" + dateFormat.format(date) + "] Server: " + msg + "\n");
        TA_SERVER_MSGS.setCaretPosition(TA_SERVER_MSGS.getDocument().getLength());
    }

    private void closeApp() {
        try {
            if (serverManagement != null) {
                serverManagement.logoutUser(USER_ID);
            } else {
                showErrorMsg("Connection error occured!");
            }
        } catch (Exception e) {
            showErrorMsg(e.getMessage());
        } finally {
            System.exit(0);
        }
    }

    public MainWindow(String title) {
        super(title);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                closeApp();
            }
        });

        BTN_EXIT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                closeApp();
            }
        });
        BTN_HOST.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!TF_USER_NAME.getText().equals("")) {
                    HostGameWindow hostGameWindow = new HostGameWindow("Host a game", MainWindow.this);
                    setVisible(false);
                } else {
                    showErrorMsg("Please provide user name!");
                }
            }
        });
        BTN_REFRESH.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                downloadRooms();
            }
        });
        BTN_JOIN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!TF_USER_NAME.getText().equals("")) {
                    try {
                        ROOM_ID = (int) TABLE_ROOMS.getValueAt(SELECTED_TABLE_ROW, 0);
                        String roomName = (String) TABLE_ROOMS.getValueAt(SELECTED_TABLE_ROW, 1);
                        String gameTypeStr = (String) TABLE_ROOMS.getValueAt(SELECTED_TABLE_ROW, 3);
                        String userName = TF_USER_NAME.getText();
                        String host = (String) TABLE_ROOMS.getValueAt(SELECTED_TABLE_ROW, 2);

                        int gameType = 0;
                        switch (gameTypeStr) {
                            case "3x3":
                                gameType = 0;
                                break;
                            case "4x4":
                                gameType = 1;
                                break;
                            case "5x5":
                                gameType = 2;
                                break;
                            case "6x6":
                                gameType = 3;
                                break;
                            default:
                                break;
                        }

                        if (serverManagement.joinRoom(ROOM_ID, userName, USER_ID)) {
                            System.out.println("Correct password!");
                            GAME_ROOM = new GameRoom(0, roomName, ROOM_ID, gameType, host, userName, USER_ID, MainWindow.this);
                            setVisible(false);
                        }
                    } catch (Exception e) {
                        showErrorMsg(e.getMessage());
                    }
                } else {
                    showErrorMsg("Please provide user name!");
                }
            }
        });
        TABLE_ROOMS.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    super.mouseClicked(mouseEvent);
                    SELECTED_TABLE_ROW = TABLE_ROOMS.rowAtPoint(mouseEvent.getPoint());
                    BTN_JOIN.setEnabled(true);
                } catch (Exception e) {
                    showErrorMsg(e.getMessage());
                }
            }
        });

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            showErrorMsg(e.getMessage());
        }

        TABLE_MODEL = new TableModel(new String[]{"ID", "Room", "Host", "Game Type", "Password", "Players"}, 0);
        TABLE_ROOMS.setModel(TABLE_MODEL);

        setContentPane(PANEL_MAIN);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        seassion();
    }

    public static void main(String[] args) {
        new MainWindow(TITLE);
    }

    private void seassion() {
        try {
            serverManagement = new ServerManagement("127.0.0.1", 80);
            displayServerMsg("Logging in...");
            USER_ID = serverManagement.loginUser();
            TF_USER_NAME.setText("User#" + USER_ID);
            displayServerMsg("Connected to TicTacToe Online server.");
            downloadRooms();
            waitForRoom();
        } catch (Exception e) {
            try {
                displayServerMsg(e.getMessage());
                Thread.sleep(1000);
                displayServerMsg("Reconnecting...");
                Thread.sleep(1000);
                seassion();
            } catch (Exception ex) {
                displayServerMsg(ex.getMessage());
            }
        }
    }

    public void waitForRoom() {
        try {
            while (true) {
                Thread.sleep(1000);
                if (GAME_ROOM != null) {
                    System.out.println("Awaiting room command...");
                    GAME_ROOM.gameSeassion();
                    break;
                }
            }
        } catch (Exception e) {
            showErrorMsg(e.getMessage());
        }
    }
}
