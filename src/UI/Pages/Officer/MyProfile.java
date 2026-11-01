package UI.Pages.Officer;

import Exception.DataAccessException;
import UI.Pages.Components.Avatar;
import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import UI.Pages.Components.RoundedButton;
import data.DepartmentData;
import data.TechnicalOfficerData;
import model.Department;
import model.TechnicalOfficer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Officer "My Profile" tab: shows and saves the logged-in technical officer's details.
// Department is shown as a label (read-only).
public class MyProfile extends Page {

    private final TechnicalOfficerData officerData = new TechnicalOfficerData();
    private final TechnicalOfficer user;
    private final JTextField name = field(20);
    private final JTextField email = field(20);
    private final JTextField contact = field(20);
    private final JTextField dob = field(20);
    private final JTextField address = field(20);
    private final JLabel gender = new JLabel();
    private final JLabel department = new JLabel();

    public MyProfile(User user) {
        super("My Profile", "Your account details", null);
        this.user = user;
        refreshFromDb();

        department.setFont(font(Font.PLAIN, 14));
        department.setForeground(INK);
        loadDepartment();

        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(24, 26, 24, 26));
        card.add(identityRow(), BorderLayout.NORTH);
        card.add(fieldsGrid(), BorderLayout.CENTER);
        card.add(buttonsRow(), BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH);
        setScrollBody(wrap);

        load();
    }
    // The login object only has basic columns, so read the full record first.
    private void refreshFromDb() {
        if (user == null) return;
        try {
            TechnicalOfficer fromDb = officerData.findById(user.getUser_id());
            if (fromDb == null) return;
            user.setName(fromDb.getName());
            user.setEmail(fromDb.getEmail());
            user.setContact_number(fromDb.getContact_number());
            user.setDOB(fromDb.getDOB());
            user.setAddress(fromDb.getAddress());
            user.setGender(fromDb.getGender());
            user.setDep_id(fromDb.getDep_id());
        } catch (DataAccessException ex) {
            info(this, "Failed to load your profile: " + ex.getMessage());
        }
    }

    private void loadDepartment() {
        if (user == null) return;
        try {
            Department d = new DepartmentData().findById(user.getDep_id());
            department.setText(d == null ? "—" : d.getDep_name());
        } catch (DataAccessException ex) {
            department.setText("—");
            info(this, "Failed to load your department: " + ex.getMessage());
        }
    }

    // copies the user into the text fields
    private void load() {
        if (user == null) return;
        name.setText(nz(user.getName()));
        email.setText(nz(user.getEmail()));
        contact.setText(nz(user.getContact_number()));
        dob.setText(user.getDOB() == null ? "" : user.getDOB().toString());
        address.setText(nz(user.getAddress()));
        gender.setText(nz(user.getGender()).isEmpty() ? "—" : user.getGender());
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private JComponent identityRow() {
        String shown = (user != null && user.getName() != null && !user.getName().isBlank())
                ? user.getName() : "Technical Officer";

        JLabel t = new JLabel(shown);
        t.setFont(font(Font.BOLD, 20));
        t.setForeground(INK);
        JLabel s = new JLabel("Technical Officer");
        s.setFont(font(Font.PLAIN, 13));
        s.setForeground(INK_2);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(t);
        text.add(s);
        text.add(Box.createVerticalGlue());

        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.add(new Avatar(shown, user != null ? user.getPhoto() : null, 64), BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JComponent fieldsGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 14));
        grid.setOpaque(false);
        addField(grid, "Full name", name);
        addField(grid, "Email", email);
        addField(grid, "Contact number", contact);
        addField(grid, "Date of birth (yyyy-mm-dd)", dob);
        addField(grid, "Address", address);
        addField(grid, "Gender (read-only)", gender);
        addField(grid, "Department", department);   // read-only label
        return grid;
    }

    private void addField(JPanel grid, String caption, JComponent f) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(label(caption), BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        grid.add(p);
    }

    private JComponent buttonsRow() {
        RoundedButton save = new RoundedButton("Save changes", true);
        save.addActionListener(e -> save());
        RoundedButton reset = new RoundedButton("Reset", false);
        reset.addActionListener(e -> load());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        p.add(reset);
        p.add(save);
        return p;
    }

    private void save() {
        if (user == null) return;
        if (name.getText().isBlank() || email.getText().isBlank()) {
            info(this, "Name and email are required.");
            return;
        }
        Date d = null;
        if (!dob.getText().isBlank()) {
            try {
                d = Date.valueOf(dob.getText().trim());
            } catch (IllegalArgumentException ex) {
                info(this, "Date of birth must look like 1985-03-21.");
                return;
            }
        }
        user.setName(name.getText().trim());
        user.setEmail(email.getText().trim());
        user.setContact_number(contact.getText().trim());
        user.setDOB(d);
        user.setAddress(address.getText().trim());

        try {
            if (officerData.updateProfile(user)) {
                refreshFromDb();
                info(this, "Profile saved successfully.");
            } else {
                info(this, "No changes were saved — the record may not exist.");
            }
        } catch (DataAccessException ex) {
            info(this, "Failed to save your profile: " + ex.getMessage());
        }
    }
}