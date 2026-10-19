package UI.Pages.Officer;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static UI.Kit.Palette.*;
import static UI.Kit.Palette.font;

public class Overview extends Page {
    public Overview(User user) {
        super("Overview",
                "Welcome back, " + ((user != null && user.getName() != null) ? user.getName() : "Administrator")
                        + "  ·  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                null);
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(new JLabel("Medicals awaiting approval"));
        body.add(kpiRow(), BorderLayout.NORTH);
        setScrollBody(body);
    }

    // ---------- KPI cards ----------
    private JComponent kpiRow() {
        String[][] kpis = {
                {"Modules", "6", "6 modules * 15 sessions"},
                {"Registers", "90", "theory & practical"},
                {"Undergraduates", "5", "2 repeat . 2 batch_missed"},
                {"Pending medicals", "3", "awaiting your decision"}
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
}
