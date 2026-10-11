package UI.Pages.admin;

import UI.Pages.admin.AdminUi.Card;
import UI.Pages.admin.AdminUi.Page;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static UI.Pages.admin.AdminUi.*;

// Admin "Overview" tab: 4 KPI cards, users by role, requires attention.
public class Overview extends Page {

    // ---------- Placeholder data (swap for MockData / services later) ----------
    private static final String[][] KPIS = {
            {"Total users", "30", "Across all roles"},
            {"Active courses", "6", "This semester"},
            {"Notices", "5", "Published"},
            {"Pending medicals", "8", "Awaiting decision"}
    };
    private static final Object[][] ROLES = {
            {"Undergraduates", 20}, {"Lecturers", 5}, {"Technical Officers", 4}, {"Administrators", 1}
    };
    private static final Object[][] ATTENTION = {
            {"8 medicals pending", "Technical Officers need to decide", WARN},
            {"3 undergraduates below 80% attendance", "Not eligible for final exam", BAD},
            {"1 timetable slot unassigned", "Thursday, slot 3", WARN},
            {"2 notices expire this week", "Review or renew", INK_2}
    };
    // ---------- end placeholder data ----------

    public Overview(User user) {
        super("Overview",
                "Welcome back, " + ((user != null && user.getName() != null) ? user.getName() : "Administrator")
                        + "  ·  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                null);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(kpiRow(), BorderLayout.NORTH);
        body.add(lowerRow(), BorderLayout.CENTER);
        setScrollBody(body);
    }

    private JComponent kpiRow() {
        JPanel row = new JPanel(new GridLayout(1, KPIS.length, 16, 0));
        row.setOpaque(false);
        for (int i = 0; i < KPIS.length; i++) row.add(kpiCard(KPIS[i][0], KPIS[i][1], KPIS[i][2], i == 0));
        return row;
    }

    private JComponent kpiCard(String label, String value, String sub, boolean dark) {
        Card c = new Card(dark);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(18, 20, 18, 20));
        c.setPreferredSize(new Dimension(0, 124));
        Color muted = dark ? new Color(190, 215, 200) : INK_2;

        JLabel l = new JLabel(label);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(muted);
        JLabel v = new JLabel(value);
        v.setFont(font(Font.BOLD, 34));
        v.setForeground(dark ? Color.WHITE : INK);
        JLabel s = new JLabel(sub);
        s.setFont(font(Font.PLAIN, 12));
        s.setForeground(muted);

        c.add(l);
        c.add(Box.createVerticalGlue());
        c.add(v);
        c.add(s);
        return c;
    }

    private JComponent lowerRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.add(infoCard("Users by role", rolesBody()));
        row.add(infoCard("Requires attention", attentionBody()));
        return row;
    }

    private JComponent infoCard(String title, JComponent content) {
        Card c = new Card(false);
        c.setLayout(new BorderLayout(0, 14));
        c.setBorder(new EmptyBorder(20, 22, 20, 22));
        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 16));
        t.setForeground(INK);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(content, BorderLayout.NORTH);
        c.add(t, BorderLayout.NORTH);
        c.add(top, BorderLayout.CENTER);
        return c;
    }

    private JComponent rolesBody() {
        int total = 0;
        for (Object[] r : ROLES) total += (Integer) r[1];
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (Object[] r : ROLES) {
            p.add(roleRow((String) r[0], (Integer) r[1], total));
            p.add(Box.createVerticalStrut(10));
        }
        return p;
    }

    private JComponent roleRow(String label, int count, int total) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel name = new JLabel(label);
        name.setFont(font(Font.PLAIN, 14));
        name.setForeground(INK);
        name.setPreferredSize(new Dimension(150, 24));
        JLabel num = new JLabel(String.valueOf(count), SwingConstants.RIGHT);
        num.setFont(font(Font.BOLD, 14));
        num.setForeground(INK);
        num.setPreferredSize(new Dimension(30, 24));

        final double share = total == 0 ? 0 : (double) count / total;
        JComponent bar = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int h = 8, y = (getHeight() - h) / 2;
                g2.setColor(SURFACE_2);
                g2.fill(new RoundRectangle2D.Float(0, y, getWidth(), h, h, h));
                g2.setColor(BRAND);
                g2.fill(new RoundRectangle2D.Float(0, y, (float) (getWidth() * share), h, h, h));
                g2.dispose();
            }
        };
        row.add(name, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(num, BorderLayout.EAST);
        return row;
    }

    private JComponent attentionBody() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (int i = 0; i < ATTENTION.length; i++) {
            p.add(attentionRow((String) ATTENTION[i][0], (String) ATTENTION[i][1], (Color) ATTENTION[i][2]));
            if (i < ATTENTION.length - 1) {
                JSeparator sep = new JSeparator();
                sep.setForeground(LINE);
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                p.add(sep);
            }
        }
        return p;
    }

    private JComponent attentionRow(String title, String sub, Color dot) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JComponent d = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dot);
                g2.fill(new Ellipse2D.Float(2, 6, 9, 9));
                g2.dispose();
            }
        };
        d.setPreferredSize(new Dimension(14, 20));

        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 14));
        t.setForeground(INK);
        JLabel s = new JLabel(sub);
        s.setFont(font(Font.PLAIN, 12));
        s.setForeground(INK_2);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(t);
        text.add(s);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(d, BorderLayout.NORTH);
        row.add(left, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }
}
