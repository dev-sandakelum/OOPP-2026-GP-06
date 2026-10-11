package UI.Pages.admin;

import UI.Pages.admin.AdminUi.Btn;
import UI.Pages.admin.AdminUi.Card;
import UI.Pages.admin.AdminUi.Page;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.sql.Date;

import static UI.Pages.admin.AdminUi.*;

// Admin "My Profile" tab: shows and edits the logged-in user (the User model is not changed).
public class MyProfile extends Page {

    private final User user;
    private final JTextField name = field(20), email = field(20), contact = field(20),
            dob = field(20), address = field(20), gender = field(20);

    public MyProfile(User user) {
        super("My Profile", "Your account details", null);
        this.user = user;

        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(24, 26, 24, 26));
        card.add(identity(), BorderLayout.NORTH);
        card.add(form(), BorderLayout.CENTER);
        card.add(buttons(), BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH);
        setScrollBody(wrap);
        load();
    }

    private void load() {
        if (user == null) return;
        name.setText(nz(user.getName()));
        email.setText(nz(user.getEmail()));
        contact.setText(nz(user.getContact_number()));
        dob.setText(user.getDOB() == null ? "" : user.getDOB().toString());
        address.setText(nz(user.getAddress()));
        gender.setText(nz(user.getGender()));
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private JComponent identity() {
        String n = (user != null && user.getName() != null && !user.getName().isBlank()) ? user.getName() : "Administrator";
        String[] parts = n.trim().split("\\s+");
        String ini = parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "");

        JComponent avatar = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, BRAND_LT.darker(), getWidth(), getHeight(), BRAND_DK));
                g2.fill(new Ellipse2D.Float(0, 0, getWidth() - 1, getHeight() - 1));
                g2.setColor(Color.WHITE);
                g2.setFont(font(Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String s = ini.toUpperCase();
                g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(64, 64));

        JLabel t = new JLabel(n);
        t.setFont(font(Font.BOLD, 20));
        t.setForeground(INK);
        JLabel s = new JLabel("Administrator");
        s.setFont(font(Font.PLAIN, 13));
        s.setForeground(INK_2);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(t);
        text.add(s);
        text.add(Box.createVerticalGlue());

        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);
        p.add(avatar, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private JComponent form() {
        JPanel g = new JPanel(new GridLayout(0, 2, 20, 14));
        g.setOpaque(false);
        addField(g, "Full name", name);
        addField(g, "Email", email);
        addField(g, "Contact number", contact);
        addField(g, "Date of birth (yyyy-mm-dd)", dob);
        addField(g, "Address", address);
        addField(g, "Gender", gender);
        return g;
    }

    private void addField(JPanel g, String label, JTextField f) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(label(label), BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        g.add(p);
    }

    private JComponent buttons() {
        Btn save = new Btn("Save changes", true);
        save.addActionListener(e -> save());
        Btn reset = new Btn("Reset", false);
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
            try { d = Date.valueOf(dob.getText().trim()); }
            catch (IllegalArgumentException ex) { info(this, "Date of birth must look like 1985-03-21."); return; }
        }
        user.setName(name.getText().trim());
        user.setEmail(email.getText().trim());
        user.setContact_number(contact.getText().trim());
        user.setDOB(d);
        user.setAddress(address.getText().trim());
        user.setGender(gender.getText().trim());
        info(this, "Profile saved.");
    }
}
