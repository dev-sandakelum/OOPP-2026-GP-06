import UI.Pages.LoginPage;
import database.Database;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // Connect to the database once at startup
        try {
            Database.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Open the login window — it handles routing to the right dashboard
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
