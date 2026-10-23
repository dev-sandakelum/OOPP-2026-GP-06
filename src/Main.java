import UI.Pages.AdminPages;
import UI.Pages.OfficerPages;
import UI.Pages.LecturerPage;
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


        UserData ud = new UserData();
        User u2 = new User(ud.findById(3));

        SwingUtilities.invokeLater(() -> new AdminPages(u2).setVisible(true));

    }
}