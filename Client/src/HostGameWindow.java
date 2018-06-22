import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class HostGameWindow extends JFrame {
    private final int WINDOW_WIDTH = 300;
    private final int WINDOW_HEIGHT = 300;

    private JPanel PANEL_HOST_GAME;
    private JTextField TF_ROOM_NAME;
    private JComboBox CB_GAME_TYPE;
    private JCheckBox CB_PASSWORD;
    private JPasswordField PF_PASSWORD;
    private JButton BTN_CANCEL;
    private JButton BTN_CREATE;
    private MainWindow PARRENT;

    private void closeWindow() {
        dispose();
    }

    public HostGameWindow(String title, MainWindow parrent) {
        super(title);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                closeWindow();
            }
        });

        BTN_CREATE.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (PARRENT != null) {
                    String roomName = TF_ROOM_NAME.getText();
                    boolean chPassword = CB_PASSWORD.isSelected();
                    int chPassInt = 0;
                    int gameType = CB_GAME_TYPE.getSelectedIndex();
                    if (!roomName.equals("")) {
                        if (chPassword) {
                            chPassInt = 1;
                            String password = new String(PF_PASSWORD.getPassword());
                            if (!password.equals("")) {
                                System.out.println(roomName + gameType + chPassInt + password);
                                PARRENT.sendRoomData(roomName, gameType, chPassInt, password);
                                closeWindow();
                            } else {
                                PARRENT.showErrorMsg("You have to provide the password!");
                            }
                        } else {
                            System.out.println(roomName + gameType + chPassInt);
                            PARRENT.sendRoomData(roomName, gameType, chPassInt, null);
                            closeWindow();
                        }
                    } else {
                        PARRENT.showErrorMsg("You have to provide the room name!");
                    }
                }
            }
        });

        BTN_CANCEL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                closeWindow();
                PARRENT.setVisible(true);
            }
        });

        PARRENT = parrent;
        setContentPane(PANEL_HOST_GAME);
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        setLocationRelativeTo(PARRENT);
        setVisible(true);
    }
}
