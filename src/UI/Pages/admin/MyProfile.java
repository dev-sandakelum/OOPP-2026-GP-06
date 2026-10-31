package UI.Pages.admin;

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

// Admin "My Profile" tab: shows and persists edits for the logged-in admin user.
public class MyProfile extends Page {

    private final UserData userData = new UserData();
    private final User     user;

    private final JTextField nameField    = field(20);
    private final JTextField emailField   = field(20);
    private final JTextField contactField = field(20);
    private final JTextField dobField     = field(20);
    private final JTextField addressField = field(20);
    private final JTextField genderField  = field(20);

    public MyProfile(User user) {
        super("My Profile", "Your account details", null);
        this.user = user;

        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(24, 26, 24, 26));
        card.add(identityRow(), BorderLayout.NORTH);
        card.add(fieldsGrid(),  BorderLayout.CENTER);
        card.add(buttonsRow(),  BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH);
        setScrollBody(wrap);

        load();
    }

    // ----------------------------------------------------------------
    //  Load / reset
    // ----------------------------------------------------------------

    private void load() {
        if (user == null) return;
        nameField.setText(nz(user.getName()));
        emailField.setText(nz(user.getEmail()));
        contactField.setText(nz(user.getContact_number()));
        dobField.setText(user.getDOB() == null ? "" : user.getDOB().toString());
        addressField.setText(nz(user.getAddress()));
        genderField.setText(nz(user.getGender()));
    }

    private static String nz(String s) { return s == null ? "" : s; }

    // ----------------------------------------------------------------
    //  Identity row (avatar + name + role label)
    // ----------------------------------------------------------------

    private JComponent identityRow() {
        String shown = (user != null && user.getName() != null && !user.getName().isBlank())
                ? user.getName() : "Administrator";

        JLabel nameLabel = new JLabel(shown);
        nameLabel.setFont(font(Font.BOLD, 20));
        nameLabel.setForeground(INK);

        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(font(Font.PLAIN, 13));
        roleLabel.setForeground(INK_2);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(nameLabel);
        text.add(roleLabel);
        text.add(Box.createVerticalGlue());

        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.add(new Avatar(shown, 64), BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    // ----------------------------------------------------------------
    //  Fields grid
    // ----------------------------------------------------------------

    private JComponent fieldsGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 14));
        grid.setOpaque(false);
        addField(grid, "Full name",                    nameField);
        addField(grid, "Email",                        emailField);
        addField(grid, "Contact number",               contactField);
        addField(grid, "Date of birth (yyyy-mm-dd)",   dobField);
        addField(grid, "Address",                      addressField);
        addField(grid, "Gender",                       genderField);
        return grid;
    }

    private void addField(JPanel grid, String caption, JTextField f) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(label(caption), BorderLayout.NORTH);
        p.add(f,              BorderLayout.CENTER);
        grid.add(p);
    }

    // ----------------------------------------------------------------
    //  Buttons row
    // ----------------------------------------------------------------

    private JComponent buttonsRow() {
        RoundedButton save  = new RoundedButton("Save changes", true);
        save.addActionListener(e -> save());
        RoundedButton reset = new RoundedButton("Reset", false);
        reset.addActionListener(e -> load());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        p.add(reset);
        p.add(save);
        return p;
    }

    // ----------------------------------------------------------------
    //  Save — validates fields then persists to DB via UserData
    // ----------------------------------------------------------------

    private void save() {
        if (user == null) return;

        if (nameField.getText().isBlank() || emailField.getText().isBlank()) {
            info(this, "Name and email are required.");
            return;
        }

        Date dob = null;
        if (!dobField.getText().isBlank()) {
            try {
                dob = Date.valueOf(dobField.getText().trim());
            } catch (IllegalArgumentException ex) {
                info(this, "Date of birth must look like 1985-03-21.");
                return;
            }
        }

        // Update the in-memory user object
        user.setName(nameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setContact_number(contactField.getText().trim());
        user.setDOB(dob);
        user.setAddress(addressField.getText().trim());
        user.setGender(genderField.getText().trim());

        // Persist to the database
        try {
            boolean ok = userData.update(user);
            if (ok) {
                info(this, "Profile saved successfully.");
            } else {
                info(this, "No changes were saved — the record may not exist.");
            }
        } catch (Exception ex) {
            info(this, "Could not save profile: " + ex.getMessage());
        }
    }
}
