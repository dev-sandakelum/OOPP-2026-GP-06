package UI.Pages;

import UI.Kit.FrameData;
import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder — Lecturer dashboard.
 * Senitha will implement the full page (see docs/issue-lecturer-ui-data-connect.md).
 */
public class LecturerPages extends JFrame implements FrameData {

    public LecturerPages(User user) {
        setTitle("AFMS — Lecturer");
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(width, height));
        getContentPane().setBackground(PAGE_BG);
        getContentPane().setLayout(new BorderLayout());

        JLabel msg = new JLabel(
                "<html><div style='text-align:center'>"
                + "<b style='font-size:18px'>Lecturer Dashboard</b><br/><br/>"
                + "Coming soon — assigned to Senitha.<br/>"
                + "See <i>docs/issue-lecturer-ui-data-connect.md</i>"
                + "</div></html>",
                SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(13, 31, 22));
        getContentPane().add(msg, BorderLayout.CENTER);
    }
}
