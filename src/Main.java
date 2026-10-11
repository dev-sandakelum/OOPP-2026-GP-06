import UI.Pages.AdminPages;
import UI.Pages.LecturerPage;
import UI.Pages.LecturerPage;
import database.Database;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Database.getConnection();
        SwingUtilities.invokeLater(() -> new LecturerPage().setVisible(true));
    }

}