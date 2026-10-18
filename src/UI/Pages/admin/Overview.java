package UI.Pages.admin;

import UI.Pages.Components.BarMeter;
import UI.Pages.Components.Card;
import UI.Pages.Components.Dot;
import UI.Pages.Components.Page;
import data.UserData;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static UI.Kit.Palette.*;

// Admin "Overview" tab: 4 KPI cards, users by role, requires attention.
public class Overview extends Page {

    // ---------- Placeholder data (swap for services later) ----------
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

    private final UserData userData = new UserData();

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

    // ---------- KPI cards ----------
    private JComponent kpiRow() {
        String[][] kpis = {
                {"Total users", String.valueOf(userData.count()), "Across all roles"},
                {"Active courses", "6", "This semester"},
                {"Notices", "5", "Published"},
                {"Pending medicals", "8", "Awaiting decision"}

        };
        JPanel row = new JPanel(new GridLayout(1, kpis.length, 16, 0));
        row.setOpaque(false);
        for (int i = 0; i < kpis.length; i++) {
            row.add(kpiCard(kpis[i][0], kpis[i][1], kpis[i][2], i == 0));
        }
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

    // ---------- Lower cards ----------
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
        JLabel name = new JLabel(label);
        name.setFont(font(Font.PLAIN, 14));
        name.setForeground(INK);
        name.setPreferredSize(new Dimension(150, 24));

        JLabel num = new JLabel(String.valueOf(count), SwingConstants.RIGHT);
        num.setFont(font(Font.BOLD, 14));
        num.setForeground(INK);
        num.setPreferredSize(new Dimension(30, 24));

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.add(name, BorderLayout.WEST);
        row.add(new BarMeter(total == 0 ? 0 : (double) count / total), BorderLayout.CENTER);
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

    private JComponent attentionRow(String title, String sub, Color dotColor) {
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
        left.add(new Dot(dotColor), BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.add(left, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }
}
