import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {
    private final String LOG_FILE = "ttto_server.log";

    public boolean addEntry(String entry) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true));
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            bw.append(dateFormat.format(date) + ": " + entry + "\n\r");
            System.out.println(dateFormat.format(date) + " - Entry added to: " + LOG_FILE);
            bw.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
