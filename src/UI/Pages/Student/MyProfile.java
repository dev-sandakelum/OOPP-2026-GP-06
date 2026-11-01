package UI.Pages.Student;

import UI.Pages.Components.Avatar;
import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import UI.Pages.Components.RoundedButton;
import data.UserData;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

public class MyProfile extends Page {

    private final UserData userData = new UserData();
    private final User user;

    // Fields
    private final JTextField nameField = field(20);
    private final JTextField usernameField = field(20);
    private final JTextField roleField = field(20);
    private final JTextField indexField = field(20);
    private final JTextField emailField = field(20);
    private final JTextField phoneField = field(20);
    private final JTextField dobField = field(20);
    private final JTextField addressField = field(20);
    private final JTextField genderField = field(20);

    public MyProfile(User user) {
        super("My profile", "Undergraduates may update contact details and profile picture only", null);
        this.user = user;

        Card card = new Card(false);
        card.setLayout(new BorderLayout(30, 20));
        card.setBorder(new EmptyBorder(24, 26, 24, 26));

        // Photo section (Left)
        card.add(createPhotoSection(), BorderLayout.WEST);

        // Form section (Center)
        card.add(createFormSection(), BorderLayout.CENTER);

        // Footer section (Bottom)
        card.add(createFooterSection(), BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH);
        setScrollBody(wrap);

        load();
    }

    private void load() {
        if (user == null) return;
        String shownName = (user.getName() != null) ? user.getName() : "";
        String email = (user.getEmail() != null) ? user.getEmail() : "";
        String phone = (user.getContact_number() != null) ? user.getContact_number() : "";

        nameField.setText(shownName);
        usernameField.setText(""); // Placeholder - username not in User model
        roleField.setText("Undergraduate");
        indexField.setText(""); // Placeholder - can be retrieved from student_id if needed

        emailField.setText(email);
        phoneField.setText(phone);
        dobField.setText(user.getDOB() == null ? "" : user.getDOB().toString());
        addressField.setText(user.getAddress() == null ? "" : user.getAddress());
        genderField.setText(user.getGender() == null ? "" : user.getGender());
    }

    private JPanel createPhotoSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 10));

        String shownName = (user != null && user.getName() != null && !user.getName().isBlank())
                ? user.getName() : "Silva";

        Avatar avatar = new Avatar(shownName, 80);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedButton changePhotoBtn = new RoundedButton("Change photo", false);
        changePhotoBtn.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        changePhotoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);


        changePhotoBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();

                info(this, "Selected photo: " + selectedFile.getName());
            }
        });

        panel.add(avatar);
        panel.add(Box.createVerticalStrut(15));
        panel.add(changePhotoBtn);

        return panel;
    }

    private JPanel createFormSection() {
        JPanel grid = new JPanel(new GridLayout(5, 2, 25, 15));
        grid.setOpaque(false);

        // Row 1: Locked
        grid.add(createFieldPanel("Full name", nameField, true));
        grid.add(createFieldPanel("Username", usernameField, true));

        // Row 2: Locked
        grid.add(createFieldPanel("Role", roleField, true));
        grid.add(createFieldPanel("Index number", indexField, true));

        // Row 3: Editable
        grid.add(createFieldPanel("Email", emailField, false));
        grid.add(createFieldPanel("Phone", phoneField, false));

        // Row 4: Editable
        grid.add(createFieldPanel("Date of birth (yyyy-mm-dd)", dobField, false));
        grid.add(createFieldPanel("Address", addressField, false));

        // Row 5: Editable
        grid.add(createFieldPanel("Gender (MALE/FEMALE/OTHER)", genderField, false));
        grid.add(new JPanel()); // Empty cell

        return grid;
    }

    private JPanel createFieldPanel(String caption, JTextField f, boolean isLocked) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);
        labelPanel.add(label(caption), BorderLayout.WEST);

        if (isLocked) {
            JLabel lockLbl = new JLabel(" locked");
            lockLbl.setFont(font(Font.PLAIN, 11));
            lockLbl.setForeground(Color.GRAY);
            labelPanel.add(lockLbl, BorderLayout.EAST);

            f.setEditable(false);
            f.setFocusable(false);
            f.setBackground(SURFACE_2);
            f.setForeground(Color.DARK_GRAY);
        }

        p.add(labelPanel, BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        return p;
    }

    private JPanel createFooterSection() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JLabel info = new JLabel("Only contact details and the photo can be changed, per faculty rules.");
        info.setFont(font(Font.PLAIN, 12));
        info.setForeground(Color.GRAY);

        RoundedButton saveBtn = new RoundedButton("Save changes", true);
        saveBtn.setBackground(BRAND);
        saveBtn.addActionListener(e -> save());

        footer.add(info, BorderLayout.WEST);
        footer.add(saveBtn, BorderLayout.EAST);

        return footer;
    }

    private void save() {
        if (user == null) return;
        if (emailField.getText().isBlank() || phoneField.getText().isBlank()) {
            info(this, "Email and phone are required.");
            return;
        }

        Date d = null;
        if (!dobField.getText().isBlank()) {
            try {
                d = Date.valueOf(dobField.getText().trim());
            } catch (IllegalArgumentException ex) {
                info(this, "Date of birth must look like 1985-03-21.");
                return;
            }
        }

        user.setEmail(emailField.getText().trim());
        user.setContact_number(phoneField.getText().trim());
        user.setDOB(d);
        user.setAddress(addressField.getText().trim());
        user.setGender(genderField.getText().trim());

        try {
            boolean ok = userData.update(user);
            info(this, ok ? "Profile updated successfully." : "Save failed — check the console.");
        } catch (Exception ex) {
            info(this, "Could not save profile: " + ex.getMessage());
        }
    }
}