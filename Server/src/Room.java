import java.util.ArrayList;

public class Room {
    private final int GT_3X3 = 0;
    private final int GT_4X4 = 1;
    private final int GT_5X5 = 2;

    private int id;
    private String name;
    private int gameType;
    private boolean chPassword;
    private String password;
    private User host;
    private User guest;
    private Board board;

    public Room(int id, String name, int gameType, boolean chPassword, String password, User host) {
        this.id = id;
        this.name = name;
        this.gameType = gameType;
        this.chPassword = chPassword;
        this.password = password;
        this.host = host;

        switch (gameType) {
            case GT_3X3:
                board = new Board3x3();
                break;
            case GT_4X4:
                board = new Board4x4();
                break;
            case GT_5X5:
                board = new Board5x5();
                break;
            default:
                board = new Board3x3();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getHostId() {
        return host.getId();
    }

    public User getHost() {
        return host;
    }

    public User getGuest() {
        return guest;
    }

    public int getGameType() {
        return gameType;
    }

    public boolean getChPassword() {
        return chPassword;
    }

    public String getPassword() {
        return password;
    }

    public boolean addPlayer(User player) {
        if (guest == null) {
            guest = player;
            return true;
        }
        return false;
    }

    public String toString() {
        String gameTypeStr = "-";

        switch (gameType) {
            case GT_3X3:
                gameTypeStr = "3x3";
                break;
            case GT_4X4:
                gameTypeStr = "4x4";
                break;
            case GT_5X5:
                gameTypeStr = "5x5";
                break;
            default:
                gameTypeStr = "3x3";
        }

        String passStr = "No";
        if (chPassword) passStr = "Yes";

        int players = 1;
        if (guest != null) players++;

        return id + "|" + name + "|" + host.getName() + "|" + gameTypeStr + "|" + passStr + "|" + players;
    }

    public Board getBoard() {
        return board;
    }
}
