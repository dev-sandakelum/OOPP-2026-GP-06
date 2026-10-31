package UI.Pages.admin;

import UI.Pages.Components.BarMeter;
import UI.Pages.Components.Dot;
import UI.Pages.Components.KpiCard;
import UI.Pages.Components.NavIcon;
import UI.Pages.Components.Page;
import UI.Pages.Components.SectionCard;
import data.AdminData;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

import static UI.Kit.Palette.*;

/**
 * Admin "Overview" tab.
 *
 * All numbers come from AdminData — no hard-coded placeholder data.
 * The layout mirrors the original mock-up:
 *   - top row  : 4 KPI cards (total users, active courses, notices, pending medicals)
 *   - bottom row: "Users by role" bar chart  |  "Requires attention" alert list
 */
public class Overview extends Page {

    private final AdminData adminData = new AdminData();

    // KPI values fetched once on construction
    private final int totalUsers;
    private final int activeCourses;
    private final int publishedNotices;
    private final int pendingMedicals;

    // Role counts fetched once on construction — List of {label, count}
    private final List<String[]> roleCounts;

    // Attention items built from live counts
    private final Object[][] attention;

    public Overview(User user) {
        super("Overview",
              "Hello, " + (user != null && user.getName() != null ? user.getName() : "Administrator"),
              null);

        // --- fetch from DB (all in one try so a DB failure shows a clear error) ---
        int tu = 0, ac = 0, pn = 0, pm = 0;
        List<String[]> rc = List.of();
        try {
            tu = adminData.countByRole("ADMIN")
               + adminData.countByRole("UNDERGRADUATE")
               + adminData.countByRole("LECTURER")
               + adminData.countByRole("TECHNICAL_OFFICER");
            ac = adminData.countActiveCourses();
            pn = adminData.countPublishedNotices();
            pm = adminData.countPendingMedicals();
            rc = adminData.userCountsByRole();
        } catch (Exception ex) {
            // DB not reachable yet — values stay at 0, UI still renders
            ex.printStackTrace();
        }
        totalUsers       = tu;
        activeCourses    = ac;
        publishedNotices = pn;
        pendingMedicals  = pm;
        roleCounts       = rc;

        // Build attention rows from the same live counts
        int lowAtt = 0, unassigned = 0, expiring = 0;
        try {
            lowAtt     = adminData.countLowAttendance(80);
            unassigned = adminData.countUnassignedSlots();
            expiring   = adminData.countExpiringNotices();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        attention = new Object[][]{
            {pm + " medical" + (pm == 1 ? "" : "s") + " pending",
                "Require review and decision", WARN},
            {lowAtt + " student" + (lowAtt == 1 ? "" : "s") + " below 80% attendance",
                "May not be eligible for the final exam", BAD},
            {unassigned + " timetable slot" + (unassigned == 1 ? "" : "s") + " unassigned",
                "Open the Timetables tab to assign courses", WARN},
            {expiring + " notice" + (expiring == 1 ? "" : "s") + " expire this week",
                "Review or renew before the deadline", INK_2}
        };

        // --- layout ---
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(kpiRow(), BorderLayout.NORTH);
        body.add(lowerRow(), BorderLayout.CENTER);
        setScrollBody(body);
    }

    // -------------------------------------------------------------------------
    //  KPI cards
    // -------------------------------------------------------------------------

    private JComponent kpiRow() {
        String[][] kpis = {
            {"Total users",      String.valueOf(totalUsers),       "Across all roles"},
            {"Active courses",   String.valueOf(activeCourses),    "This semester"},
            {"Notices",          String.valueOf(publishedNotices), "Published"},
            {"Pending medicals", String.valueOf(pendingMedicals),  "Awaiting decision"}
        };
        NavIcon[] icons = {NavIcon.USERS, NavIcon.STUDENTS, NavIcon.MEGA, NavIcon.PLUS_BOX};
        JPanel row = new JPanel(new GridLayout(1, kpis.length, 16, 0));
        row.setOpaque(false);
        for (int i = 0; i < kpis.length; i++) {
            row.add(new KpiCard(kpis[i][0], kpis[i][1], kpis[i][2], icons[i], i == 0));
        }
        return row;
    }

    // -------------------------------------------------------------------------
    //  Lower row
    // -------------------------------------------------------------------------

    private JComponent lowerRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.add(new SectionCard("Users by role",      NavIcon.USERS, rolesBody()));
        row.add(new SectionCard("Requires attention", NavIcon.BELL,  attentionBody()));
        return row;
    }

    // -------------------------------------------------------------------------
    //  "Users by role" bar chart
    // -------------------------------------------------------------------------

    private JComponent rolesBody() {
        int total = 0;
        for (String[] r : roleCounts) {
            try { total += Integer.parseInt(r[1]); } catch (NumberFormatException ignored) {}
        }
        final int grand = total;

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        for (String[] r : roleCounts) {
            int count = 0;
            try { count = Integer.parseInt(r[1]); } catch (NumberFormatException ignored) {}
            p.add(roleRow(r[0], count, grand));
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

    // -------------------------------------------------------------------------
    //  "Requires attention" list
    // -------------------------------------------------------------------------

    private JComponent attentionBody() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        for (int i = 0; i < attention.length; i++) {
            p.add(attentionRow(
                    (String) attention[i][0],
                    (String) attention[i][1],
                    (Color)  attention[i][2]));
            if (i < attention.length - 1) {
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
