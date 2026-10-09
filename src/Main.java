import UI.Pages.AdminPages;
import database.Database;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Database.getConnection();
        SwingUtilities.invokeLater(() -> new AdminPages().setVisible(true));
    }

}