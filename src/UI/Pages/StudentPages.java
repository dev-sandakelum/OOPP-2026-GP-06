package UI.Pages;

import UI.Kit.FrameData;
import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder — Student (Undergraduate) dashboard.
 * Dilmi will implement the full page (see docs/issue-student-ui-data-connect.md).
 */
public class StudentPages extends JFrame implements FrameData {

    public StudentPages(User user) {
        setTitle("AFMS — Student");
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(width, height));
        getContentPane().setBackground(PAGE_BG);
        getContentPane().setLayout(new BorderLayout());

        JLabel msg = new JLabel(
                "<html><div style='text-align:center'>"
                + "<b style='font-size:18px'>Student Dashboard</b><br/><br/>"
                + "Coming soon — assigned to Dilmi.<br/>"
                + "See <i>docs/issue-student-ui-data-connect.md</i>"
                + "</div></html>",
                SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(13, 31, 22));
        getContentPane().add(msg, BorderLayout.CENTER);
    }
}
