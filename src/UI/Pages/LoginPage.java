package UI.Pages;

import UI.Kit.Assets;
import UI.Kit.Backdrop;
import UI.Kit.FrameData;
import UI.Kit.Palette;
import UI.Pages.Components.Card;
import UI.Pages.Components.LoadingOverlay;
import UI.Pages.Components.PhotoPanel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.Ui;
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
    private final JButton signInBtn        = new RoundedButton("Sign in", true);

    // spinner shown over the whole window while the login runs
    private final LoadingOverlay overlay = LoadingOverlay.install(this);
    private boolean busy;

    // how long the "Welcome" tick stays on screen before the dashboard opens
    private static final int WELCOME_MS = 900;

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
        if (busy) return;                               // ignore a second click / Enter while running
        String email    = emailField.getText().trim();
        String password = new String(passField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter your email and password.");
            return;
        }

        errorLabel.setText(" ");
        setBusy(true);
        overlay.start("Signing in…", "Checking your details");

        // The database call runs off the UI thread so the spinner keeps turning.
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                return new UserData().login(email, password);
            }

            @Override
            protected void done() {
                User user;
                try {
                    user = get();
                } catch (Exception ex) {
                    fail("Could not connect to the database.");
                    ex.printStackTrace();
                    return;
                }
                if (user == null) {
                    fail("Incorrect email or password.");
                    passField.setText("");
                    passField.requestFocusInWindow();
                    return;
                }
                loginSucceeded(user);
            }
        }.execute();
    }

    /** Login failed: drop the overlay, re-enable the form and show the reason. */
    private void fail(String reason) {
        overlay.dismiss();
        setBusy(false);
        errorLabel.setText(reason);
    }

    /**
     * Login worked: show the welcome tick while the dashboard is built on a background
     * thread, then show the dashboard and close this window.
     *
     * The dashboard pages load their data in their constructors, which takes a while.
     * Building the (not yet visible) frame off the event thread keeps the spinner turning;
     * it is only made visible back on the event thread.
     */
    private void loginSucceeded(User user) {
        // From here on, DB access uses the account for this user's role
        Database.useRoleFor(user.getUserType());

        String first = user.getName() == null ? "" : user.getName().trim().split("\\s+")[0];
        overlay.finish(first.isEmpty() ? "Welcome" : "Welcome, " + first, "Opening your dashboard");

        final long welcomeShownAt = System.currentTimeMillis();
        final boolean[] built = {false};

        // after the welcome moment, go back to the spinner if the dashboard is still loading
        Timer welcome = new Timer(WELCOME_MS, e -> {
            if (!built[0]) overlay.start("Opening your dashboard…", "Loading your data");
        });
        welcome.setRepeats(false);
        welcome.start();

        new SwingWorker<JFrame, Void>() {
            @Override
            protected JFrame doInBackground() {
                return buildDashboard(user);
            }

            @Override
            protected void done() {
                built[0] = true;
                JFrame dashboard;
                try {
                    dashboard = get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    fail("Could not open the dashboard.");
                    return;
                }
                if (dashboard == null) {
                    fail(" ");                                   // role problem: dialog already shown
                    return;
                }
                // keep the welcome tick on screen for its full moment before switching windows
                long remaining = WELCOME_MS - (System.currentTimeMillis() - welcomeShownAt);
                if (remaining > 0) {
                    Timer t = new Timer((int) remaining, e -> showDashboard(dashboard));
                    t.setRepeats(false);
                    t.start();
                } else {
                    showDashboard(dashboard);
                }
            }
        }.execute();
    }

    private void showDashboard(JFrame dashboard) {
        dashboard.setVisible(true);
        dispose();                                               // close the login window
    }

    private void setBusy(boolean b) {
        busy = b;
        emailField.setEnabled(!b);
        passField.setEnabled(!b);
        signInBtn.setEnabled(!b);
    }


    /**
     * Builds (but does not show) the dashboard for the user's role, or returns null after
     * showing an error when the role is missing or unknown. Safe to call off the event thread
     * because the frame is not realised until showDashboard().
     */
    private JFrame buildDashboard(User user) {
        String type = user.getUserType();

        if (type == null || type.isBlank()) {
            loginError("User role is missing. Please check the database.");
            return null;
        }

        // users.user_type is an ENUM: ADMIN, LECTURER, TECHNICAL_OFFICER, UNDERGRADUATE
        return switch (type.toLowerCase()) {
            case "lecturer"                      -> new LecturerPage(user);
            case "technical_officer", "officer"  -> new OfficerPages(user);
            case "undergraduate", "student"      -> new StudentPages(user);
            case "admin", "administrator"        -> new AdminPages(user);
            default -> {
                loginError("Unknown user role: " + type);
                yield null;
            }
        };
    }

    private void loginError(String message) {
        SwingUtilities.invokeLater(() -> Ui.error(this, "Login Error", message));
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
