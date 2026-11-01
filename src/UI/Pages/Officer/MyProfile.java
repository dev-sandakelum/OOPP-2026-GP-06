package UI.Pages.Officer;

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

// Officer "My Profile" tab: shows and edits the logged-in user (saved with UserData).
public class MyProfile extends Page {

    private final UserData userData = new UserData();
    private final User user;
    private final JTextField name = field(20);
    private final JTextField email = field(20);
    private final JTextField contact = field(20);
    private final JTextField dob = field(20);
    private final JTextField address = field(20);
    private final JTextField gender = field(20);


    public MyProfile(User user) {
        super("My Profile", "Your account details", null);
        this.user = user;

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

    // copies the user into the text fields
    private void load() {
        if (user == null) return;
        name.setText(nz(user.getName()));
        email.setText(nz(user.getEmail()));
        contact.setText(nz(user.getContact_number()));
        dob.setText(user.getDOB() == null ? "" : user.getDOB().toString());
        address.setText(nz(user.getAddress()));
        gender.setText(nz(user.getGender()));
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
        addField(grid, "Gender (MALE/FEMALE/OTHER)", gender);
        return grid;
    }

    private void addField(JPanel grid, String caption, JTextField f) {
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
        user.setGender(gender.getText().trim());
        
        try {
            boolean ok = userData.update(user);
            info(this, ok ? "Profile saved successfully." : "Save failed — check the console.");
        } catch (Exception ex) {
            info(this, "Could not save profile: " + ex.getMessage());
        }
    }
}
