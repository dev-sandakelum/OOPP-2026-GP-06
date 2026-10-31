package UI.Pages;

import UI.Kit.FrameData;
import data.UserData;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

/**
 * Login frame — opens first on startup.
 * On successful login it opens the appropriate dashboard and closes itself.
 */
public class LoginPage extends JFrame implements FrameData {

    private final JTextField emailField    = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final JLabel errorLabel        = new JLabel(" ");

    public LoginPage() {
        setTitle("FOTIMS — Login");
        setSize(440, 360);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImage(new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/assets/img.png"))).getImage());

        getContentPane().setBackground(PAGE_BG);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildForm(), BorderLayout.CENTER);
    }

    // ── form panel ────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel panel = new JPanel();
        panel.setBackground(PAGE_BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(32, 48, 32, 48));

        // Title
        JLabel title = new JLabel("Sign in to FOTIMS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(18, 64, 42));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Faculty of Technology Information Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(100, 112, 106));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(28));

        // Email
        panel.add(fieldLabel("Email address"));
        panel.add(Box.createVerticalStrut(4));
        styleField(emailField);
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(14));

        // Password
        panel.add(fieldLabel("Password"));
        panel.add(Box.createVerticalStrut(4));
        styleField(passField);
        panel.add(passField);
        panel.add(Box.createVerticalStrut(6));

        // Error message
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        errorLabel.setForeground(new Color(180, 40, 30));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(errorLabel);
        panel.add(Box.createVerticalStrut(14));

        // Sign-in button
        JButton signInBtn = new JButton("Sign in");
        signInBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        signInBtn.setBackground(new Color(18, 64, 42));
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFocusPainted(false);
        signInBtn.setBorderPainted(false);
        signInBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signInBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        signInBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInBtn.addActionListener(e -> doLogin());
        panel.add(signInBtn);

        // Allow Enter key in password field
        passField.addActionListener(e -> doLogin());

        return panel;
    }

    // ── login logic ───────────────────────────────────────────────────────────
    private void doLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter your email and password.");
            return;
        }

        try {
            UserData ud   = new UserData();
            User     user = ud.login(email, password);

            if (user == null) {
                errorLabel.setText("Incorrect email or password.");
                passField.setText("");
                return;
            }

            // Open the right dashboard based on user_type
            openDashboard(user);
            dispose();                   // close the login window

        } catch (Exception ex) {
            errorLabel.setText("Could not connect to the database.");
            ex.printStackTrace();
        }
    }

    /** Opens the correct frame based on userType stored in the DB. */
    private void openDashboard(User user) {
        String type = user.getUserType();
        if (type == null) type = "ADMIN";       // safe default for old records

        JFrame dashboard;
        switch (type.toUpperCase()) {
            case "LECTURER"          -> dashboard = new LecturerPages(user);
            case "TECHNICAL_OFFICER" -> dashboard = new OfficerPages(user);
            case "UNDERGRADUATE"     -> dashboard = new StudentPages(user);
            default                  -> dashboard = new AdminPages(user);   // "ADMIN"
        }
        dashboard.setVisible(true);
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(60, 70, 65));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 205), 1),
                new EmptyBorder(5, 10, 5, 10)));
        field.setBackground(Color.WHITE);
    }
}
