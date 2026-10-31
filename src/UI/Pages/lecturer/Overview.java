package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.ReadOnlyTableModel;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.table;

/**
 * Lecturer Overview page
 * - 4 summary cards on top
 * - Today's schedule (left) and recent notices & medical requests (right)
 *
 * Every place marked "NEED TO ADD DATA FROM DATABASE" must be connected to MySQL.
 * The sample values in the code are only dummy data so the screen is not empty.
 */
public class Overview extends LecturerUI.Page {

    DefaultTableModel scheduleModel = new ReadOnlyTableModel(new String[]{"Time", "Module", "Hall"});
    JPanel activityList = new JPanel();

    public Overview(User user) {
        super("Overview",
                "Welcome back, " + ((user != null && user.getName() != null) ? user.getName() : "Lecturer")
                        + "  ·  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                null);

        // ----- Bottom row: schedule on the left, notices on the right -----
        JPanel bottom = new JPanel(new GridLayout(1, 2, 16, 0));
        bottom.setOpaque(false);
        bottom.add(sectionCard("Today's Schedule", scheduleTable()));
        bottom.add(sectionCard("Recent Notices & Medical Requests", activityList));

        // ----- Main body -----
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(kpiRow(), BorderLayout.NORTH);
        body.add(bottom, BorderLayout.CENTER);
        setScrollBody(body);

        loadSchedule();
        loadActivity();
    }

    // =====================================================================
    // KPI CARDS
    // =====================================================================
    private JComponent kpiRow() {
        // NEED TO ADD DATA FROM DATABASE:
        // - My Modules:         SELECT COUNT(*) FROM courses WHERE lecturer_id = ?
        // - Undergraduates:     number of students enrolled in the lecturer's modules
        // - Average Attendance: average attendance % across the lecturer's modules
        // - Pending Medicals:   SELECT COUNT(*) FROM medicals ... WHERE status = 'Pending'

        // ---- dummy data ----  {label, value, small text}
        String[][] kpis = {
                {"My Modules", "4", "This semester"},
                {"Undergraduates", "312", "Across my modules"},
                {"Average Attendance", "86%", "Across my modules"},
                {"Pending Medicals", "3", "Awaiting decision"}
        };

        JPanel row = new JPanel(new GridLayout(1, kpis.length, 16, 0));
        row.setOpaque(false);
        for (int i = 0; i < kpis.length; i++) {
            row.add(kpiCard(kpis[i][0], kpis[i][1], kpis[i][2], i == 0));   // first card is dark green
        }
        return row;
    }

    private JComponent kpiCard(String label, String value, String sub, boolean dark) {
        Card card = new Card(dark);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setPreferredSize(new Dimension(0, 124));
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

        card.add(l);
        card.add(Box.createVerticalGlue());
        card.add(v);
        card.add(s);
        return card;
    }

    // =====================================================================
    // TODAY'S SCHEDULE
    // =====================================================================
    private JComponent scheduleTable() {
        JTable t = table(scheduleModel, null, -1);   // shared flat table style
        t.setFocusable(false);
        t.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, LINE));
        t.getColumnModel().getColumn(0).setPreferredWidth(70);
        t.getColumnModel().getColumn(1).setPreferredWidth(240);
        t.getColumnModel().getColumn(2).setPreferredWidth(80);

        // header + rows without a scroll pane, so the card grows with the rows
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(t.getTableHeader(), BorderLayout.NORTH);
        panel.add(t, BorderLayout.CENTER);
        return panel;
    }

    private void loadSchedule() {
        // NEED TO ADD DATA FROM DATABASE:
        // SELECT t.start_time, c.course_code, c.course_name, t.hall
        // FROM timetable t JOIN courses c ON c.course_id = t.course_id
        // WHERE c.lecturer_id = ? AND t.day = <today>
        // ORDER BY t.start_time
        // Call scheduleModel.addRow(...) for each row.

        // ---- dummy data ----
        scheduleModel.setRowCount(0);
        scheduleModel.addRow(new Object[]{"08:30", "CS101 - Programming", "LH-2"});
        scheduleModel.addRow(new Object[]{"10:30", "SE301 - Software Engineering", "Lab-1"});
        scheduleModel.addRow(new Object[]{"14:00", "AM02 - Machine Learning", "LH-5"});
    }

    // =====================================================================
    // RECENT NOTICES & MEDICAL REQUESTS
    // =====================================================================
    private void loadActivity() {
        // NEED TO ADD DATA FROM DATABASE:
        // - latest notices:  SELECT title, posted_date FROM notices WHERE lecturer_id = ? ORDER BY posted_date DESC LIMIT 3
        // - latest medicals: SELECT s.name, s.reg_no, m.submitted_date FROM medicals m JOIN students s ...
        //                    WHERE m.status = 'Pending' ORDER BY m.submitted_date DESC LIMIT 3
        // Call addActivityRow(type, title, details) for each row.

        activityList.setOpaque(false);
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activityList.removeAll();

        // ---- dummy data ----
        addActivityRow("Notice", "Mid exam timetable released", "Posted 1 day ago");
        addActivityRow("Medical", "Medical submitted - IT2034 Nirmal R.", "Awaiting review");
        addActivityRow("Notice", "Assignment submission deadline extended", "Posted 3 days ago");
    }

    // One row: colored chip (Notice / Medical) + title + small details text
    private void addActivityRow(String type, String title, String details) {
        Chip chip = new Chip(type, type.equals("Medical") ? WARN : OK);
        chip.setPreferredSize(new Dimension(70, 24));
        JPanel chipBox = new JPanel(new GridBagLayout());   // keeps the chip vertically centered
        chipBox.setOpaque(false);
        chipBox.add(chip);

        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 13));
        t.setForeground(INK);
        JLabel d = new JLabel(details);
        d.setFont(font(Font.PLAIN, 12));
        d.setForeground(INK_2);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(t);
        text.add(d);

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, LINE),        // line under every row
                new EmptyBorder(10, 0, 10, 0)));
        row.add(chipBox, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        activityList.add(row);
    }

    // =====================================================================
    // SMALL UI HELPER
    // =====================================================================

    // White rounded card with a heading and the content below it
    private JComponent sectionCard(String heading, JComponent content) {
        JLabel title = new JLabel(heading);
        title.setFont(font(Font.BOLD, 15));
        title.setForeground(INK);

        JPanel top = new JPanel(new BorderLayout());   // keeps the content at its natural height
        top.setOpaque(false);
        top.add(content, BorderLayout.NORTH);

        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.add(title, BorderLayout.NORTH);
        card.add(top, BorderLayout.CENTER);
        return card;
    }
}
