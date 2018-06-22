import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameRoom extends JFrame {
    private final int WINDOW_WIDTH = 500;
    private final int WINDOW_HEIGHT = 600;

    private final int GT_3X3 = 0;
    private final int GT_4X4 = 1;
    private final int GT_5X5 = 2;

    private final int OPPONENT_CONNECTED = 201;
    private final int OPPONENT_MOVED = 202;
    private final int OPPONENT_SENT_MSG = 203;
    private final int OPPONENT_LEFT = 204;
    private final int GAME_OVER = 205;

    private JPanel PANEL_GAME_ROOM;
    private JTextArea TA_CHAT_BOX;
    private JTextField TF_CHAT_MSG;
    private JButton BTN_SEND_MSG;
    private JLabel LBL_HOST_SCORE;
    private JLabel LBL_GUEST_SCORE;
    private JButton BTN_LEAVE_ROOM;
    private JPanel PANEL_X_O;

    private MainWindow PARRENT;

    private int ROOM_ID;
    private int USER_TYPE;
    private String USER_CHAR;
    private String USER_NAME;
    private int USER_ID;

    private JButton[][] BOARD;

    private int SCORE_HOST;
    private int SCORE_GUEST;

    private boolean USER_TURN;

    private boolean IS_OPPONENT_CONNECTED;

    private void closeWindow() {
        PARRENT.leaveRoom(ROOM_ID);
        PARRENT.setVisible(true);
        dispose();
    }

    private void closeWindowAlt() {
        PARRENT.leaveRoom();
        PARRENT.setVisible(true);
        dispose();
    }

    private void displayServerMsg(String msg) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        TA_CHAT_BOX.append("[" + dateFormat.format(date) + "] Server: " + msg + "\n");
        TA_CHAT_BOX.setCaretPosition(TA_CHAT_BOX.getDocument().getLength());
    }

    private void displayChatBoxMsg(String userName, String msg) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        TA_CHAT_BOX.append("[" + dateFormat.format(date) + "] " + userName + ": " + msg + "\n");
        TA_CHAT_BOX.setCaretPosition(TA_CHAT_BOX.getDocument().getLength());
    }

    private void clearBoard() {
        int l = BOARD.length;
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < l; j++) {
                BOARD[i][j].setText("");
            }
        }
    }

    private void createBoard(int gameType) {
        int dim;
        switch (gameType) {
            case GT_3X3:
                dim = 3;
                break;
            case GT_4X4:
                dim = 4;
                break;
            case GT_5X5:
                dim = 5;
                break;
            default:
                dim = 3;
        }

        GridLayout layout = new GridLayout(dim, dim);
        PANEL_X_O.setLayout(layout);

        BOARD = new JButton[dim][dim];

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                JButton button = new JButton();
                int dimX = i;
                int dimY = j;

                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        if (USER_TURN && !(button.getText().equals("X") || button.getText().equals("O"))) {
                            button.setText(USER_CHAR);
                            USER_TURN = false;
                            PARRENT.sendMoveInfo(dimX, dimY, USER_CHAR);
                            displayServerMsg("Opponent's turn!");
                        }
                    }
                });
                BOARD[i][j] = button;
                PANEL_X_O.add(BOARD[i][j]);
            }
        }
    }

    public GameRoom(int userType, String roomName, int roomId, int gameType, String hostName, String userName, int userId, MainWindow parrent) {
        super(roomName);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                closeWindow();
            }
        });

        BTN_LEAVE_ROOM.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                closeWindow();
            }
        });
        BTN_SEND_MSG.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String msg = TF_CHAT_MSG.getText();
                if (!msg.equals("") && !msg.isEmpty() && IS_OPPONENT_CONNECTED) {
                    displayChatBoxMsg(USER_NAME, msg);
                    PARRENT.sendChatBoxMsg(msg);
                } else if (!msg.equals("") && !msg.isEmpty()) {
                    displayChatBoxMsg(USER_NAME, msg);
                }
                TF_CHAT_MSG.setText("");
            }
        });

        PARRENT = parrent;
        USER_TYPE = userType;
        ROOM_ID = roomId;
        USER_NAME = userName;
        USER_ID = userId;
        SCORE_HOST = 0;
        SCORE_GUEST = 0;
        IS_OPPONENT_CONNECTED = false;
        USER_TURN = false;
        if (userType == 1) {
            USER_CHAR = "X";
            LBL_HOST_SCORE.setText(userName + ": " + SCORE_HOST);
            LBL_GUEST_SCORE.setText("Awaiting player...");
        } else {
            IS_OPPONENT_CONNECTED = true;
            USER_CHAR = "O";
            LBL_HOST_SCORE.setText((hostName + ": " + SCORE_HOST));
            LBL_GUEST_SCORE.setText(userName + ": " + SCORE_GUEST);
        }

        createBoard(gameType);

        setContentPane(PANEL_GAME_ROOM);
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        setLocationRelativeTo(PARRENT);
        setVisible(true);
        if (USER_TYPE == 1) displayServerMsg("Awaiting opponent...");
        else {
            displayServerMsg("You have joined the room...");
            displayServerMsg("Awaiting opponent's move...");
        }
    }

    public void gameSeassion() {
        try {
            String serverResponse = listen();
            if (serverResponse != null) {
                String[] instructions = serverResponse.split("\\|");
                int request = Integer.parseInt(instructions[0]);
                System.out.println("Instruction id: " + request);
                switch (request) {
                    case OPPONENT_CONNECTED:
                        opponentConnectedActions(instructions);
                        break;
                    case OPPONENT_MOVED:
                        opponentMovedActions(instructions);
                        break;
                    case OPPONENT_SENT_MSG:
                        opponentSendMsgActions(instructions);
                        break;
                    case OPPONENT_LEFT:
                        opponentLeftActions(instructions);
                        break;
                    case GAME_OVER:
                        gameOverActions(instructions);
                        break;
                    default:
                        break;
                }
                PARRENT.waitForRoom();
            }
        } catch (Exception e) {
            PARRENT.showErrorMsg(e.getMessage());
        }
    }

    public String listen() {
        return PARRENT.gameRoomListener();
    }

    private void opponentConnectedActions(String[] instructions) {
        IS_OPPONENT_CONNECTED = true;
        String opponent = instructions[1];
        displayServerMsg("User " + opponent + " has joined.");
        LBL_GUEST_SCORE.setText(opponent + ": 0");
        USER_TURN = true;
        displayServerMsg("Your turn!");
    }

    private void opponentMovedActions(String[] instructions) {
        USER_TURN = true;
        String opponent = instructions[1];
        int i = Integer.parseInt(instructions[2]);
        int j = Integer.parseInt(instructions[3]);
        String opponentChar = instructions[4];
        displayServerMsg(opponent + " put " + opponentChar + " at [" + i + "][" + j + "].");
        displayServerMsg("Your turn!");
        BOARD[i][j].setText(opponentChar);
    }

    private void opponentSendMsgActions(String[] instructions) {
        String opponent = instructions[1];
        String msg = instructions[2];
        displayChatBoxMsg(opponent, msg);
    }

    private void opponentLeftActions(String[] instructions) {
        closeWindowAlt();
    }

    private void gameOverActions(String[] instructions) {
        int winnerId = Integer.parseInt(instructions[1]);
        String winnerName = instructions[2];
        int winnerScore = Integer.parseInt(instructions[3]);

        USER_TURN = false;

        if (winnerId == -1) {
            String msg = "It's a tie!";
            displayServerMsg(msg);
            JOptionPane.showMessageDialog(null, msg);
            clearBoard();
            if (USER_TYPE == 1) {
                USER_TURN = true;
                displayServerMsg("Your turn!");
            }
        } else {
            if (winnerId == USER_ID && USER_TYPE == 1)
                LBL_HOST_SCORE.setText(winnerName + ": " + winnerScore);
            else if (winnerId == USER_ID)
                LBL_GUEST_SCORE.setText(winnerName + ": " + winnerScore);
            else if (USER_TYPE == 1)
                LBL_GUEST_SCORE.setText(winnerName + ": " + winnerScore);
            else
                LBL_HOST_SCORE.setText(winnerName + ": " + winnerScore);

            String msg = winnerName + " has won the game!";
            displayServerMsg(msg);
            JOptionPane.showMessageDialog(null, msg);
            clearBoard();
            if (USER_ID != winnerId) {
                USER_TURN = true;
                displayServerMsg("Your turn!");
            }
        }
    }
}
