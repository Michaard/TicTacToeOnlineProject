public class Room {
    private int id;
    private String name;
    private String host;
    private String gameType;
    private String password;
    private int players;

    public Room(int id, String name, String host, String gameType, String password, int players) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.gameType = gameType;
        this.password = password;
        this.players = players;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getGameType() {
        return gameType;
    }

    public String getPassword() {
        return password;
    }

    public int getPlayers() {
        return players;
    }
}
