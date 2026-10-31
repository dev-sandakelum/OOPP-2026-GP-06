package UI.Pages;

import UI.Kit.Assets;
import UI.Kit.Backdrop;
import UI.Kit.FrameData;
import UI.Kit.Palette;
import UI.Pages.Components.Card;
import UI.Pages.Components.PhotoPanel;
import UI.Pages.Components.RoundedButton;
import data.UserData;
import database.Database;
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
        Database.useLoginRole();    // logout lands here too: drop back to the login-only DB account
        setTitle("AFMS — Login");
        setSize(860, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImage(new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/assets/img.png"))).getImage());

        setContentPane(new Backdrop.Panel(new BorderLayout(16, 0)));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        // left: cropped faculty photo with the brand caption (falls back to a blue panel without the image)
        PhotoPanel photo = new PhotoPanel(Assets.SIDEBAR_PHOTO, 0.30f, 0.66f, Palette.RADIUS_CARD);
        photo.setPreferredSize(new Dimension(380, 0));
        photo.add(PhotoPanel.caption("AFMS", "FACULTY OF TECHNOLOGY  ·  UoR",
                "Academic & Faculty Management System"), BorderLayout.SOUTH);
        getContentPane().add(photo, BorderLayout.WEST);
        getContentPane().add(buildForm(), BorderLayout.CENTER);
    }

    // ── form panel ────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel panel = new Card(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(44, 40, 36, 40));

        // Title
        JLabel title = new JLabel("Sign in to AFMS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Palette.INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Academic & Faculty Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(Palette.INK_2);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        errorLabel.setForeground(Palette.BAD);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(errorLabel);
        panel.add(Box.createVerticalStrut(14));

        // Sign-in button
        JButton signInBtn = new RoundedButton("Sign in", true);
        signInBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
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

            // From here on, DB access uses the account for this user's role
            Database.useRoleFor(user.getUserType());

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
        if (type == null) type = "admin";       // safe default for old records

        JFrame dashboard;
        // users.user_type is an ENUM: ADMIN, LECTURER, TECHNICAL_OFFICER, UNDERGRADUATE
        switch (type.toLowerCase()) {
            case "lecturer"                      -> dashboard = new LecturerPage(user);
            case "technical_officer", "officer"  -> dashboard = new OfficerPages(user);
            case "undergraduate", "student"      -> dashboard = new StudentPages(user);
            case "admin", "administrator" ->
                    dashboard = new AdminPages(user);

            default -> {
                JOptionPane.showMessageDialog(
                        this,
                        "Unknown user role: " + type,
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }
        dashboard.setVisible(true);
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(Palette.INK_2);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE_2, 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        field.setBackground(Color.WHITE);
    }
}
