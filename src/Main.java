import UI.Pages.AdminPages;
import data.UserData;
import database.Database;
import model.User;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            Database.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        User u = new User();
        u.setUser_id(1);
        u.setName("Mrs. N. P. Jayasuriya");
        u.setEmail("admin@fot.ruh.ac.lk");
        SwingUtilities.invokeLater(() -> new AdminPages(u).setVisible(true));

    }
}