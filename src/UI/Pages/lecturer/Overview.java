package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static UI.Kit.Palette.*;

public class Overview extends LecturerUI.Page {

    private static final Color ROW_LINE = new Color(0xEEF2EF);
    private static final Color PILL_BG  = new Color(0xE1EDE3);
    private static final Color PILL_FG  = new Color(0x1F4D36);

    private final JPanel scheduleRows = rowsPanel();
    private final JPanel noticeRows   = rowsPanel();
    private final JLabel scheduleCount = new JLabel();

    public Overview(User user) {

        super("Overview",
                "Welcome back, " + ((user != null && user.getName() != null) ? user.getName() : "Administrator")
                        + "  ·  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                null);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(kpiRow(), BorderLayout.NORTH);
        body.add(topAligned(bottomRow()), BorderLayout.CENTER);
        setScrollBody(body);

        // TODO: Setup database here - replace loadMockData() with a method that loads
        //       today's schedule and recent notices for this lecturer from MySQL
        loadMockData();
    }

    /* ===================== KPI ROW (unchanged) ===================== */

    private JComponent kpiRow() {
        // TODO: Setup database here - retrieve these KPI values for the logged-in lecturer:
        //       - My Modules:         count of modules assigned to the lecturer
        //       - Undergraduates:     count of students enrolled in the lecturer's modules
        //       - Average Attendance: average attendance % across the lecturer's modules
        //       - Pending medicals:   count of medicals with status 'Pending' for the lecturer's modules
        String[][] kpis = {
                {"My Modules", "7", "Credits"},
                {"Undergraduates", "6", "This semester"},
                {"Average Attendance", "5", "across my module"},
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

    /* ===================== SCHEDULE + NOTICES ===================== */

    private JComponent bottomRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.gridy = 0;
        gc.weighty = 0;

        gc.gridx = 0; gc.weightx = 1.4; gc.insets = new Insets(0, 0, 0, 8);
        row.add(scheduleCard(), gc);

        gc.gridx = 1; gc.weightx = 1.0; gc.insets = new Insets(0, 8, 0, 0);
        row.add(noticesCard(), gc);
        return row;
    }

    private JComponent scheduleCard() {
        scheduleCount.setFont(font(Font.PLAIN, 12));
        scheduleCount.setForeground(INK_2);
        return listCard("Today's Schedule", scheduleCount, scheduleRows);
    }

    private JComponent noticesCard() {
        JLabel viewAll = new JLabel("View all");
        viewAll.setFont(font(Font.PLAIN, 12));
        viewAll.setForeground(INK_2);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // viewAll.addMouseListener(...) -> open the Notices page
        return listCard("Recent Notices", viewAll, noticeRows);
    }

    private JComponent listCard(String title, JComponent right, JPanel rows) {
        Card c = new Card(false);
        c.setLayout(new BorderLayout(0, 12));
        c.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 15));
        t.setForeground(INK);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(t, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);

        c.add(head, BorderLayout.NORTH);
        c.add(topAligned(rows), BorderLayout.CENTER);
        return c;
    }

    /* ===================== PUBLIC API (use with MySQL data later) ===================== */

    /** Each item: { time, title, details }  e.g. {"10:30", "Database Systems", "CS3012 · LT-2 · 86 students"} */
    public void setSchedule(List<String[]> items) {
        scheduleRows.removeAll();
        for (int i = 0; i < items.size(); i++) {
            String[] s = items.get(i);
            scheduleRows.add(scheduleRow(s[0], s[1], s[2], i == items.size() - 1));
        }
        scheduleCount.setText(items.size() + (items.size() == 1 ? " session" : " sessions"));
        scheduleRows.revalidate();
        scheduleRows.repaint();
    }

    /** Each item: { title, details }  e.g. {"Mid-term timetable released", "Posted 20 Oct · All courses"} */
    public void setNotices(List<String[]> items) {
        noticeRows.removeAll();
        for (int i = 0; i < items.size(); i++) {
            String[] n = items.get(i);
            noticeRows.add(noticeRow(n[0], n[1], i == items.size() - 1));
        }
        noticeRows.revalidate();
        noticeRows.repaint();
    }

    /* ===================== ROWS ===================== */

    private JComponent scheduleRow(String time, String title, String details, boolean last) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(rowBorder(last));

        JPanel pillWrap = new JPanel(new GridBagLayout());   // centres the pill vertically
        pillWrap.setOpaque(false);
        pillWrap.add(new TimePill(time));

        row.add(pillWrap, BorderLayout.WEST);
        row.add(twoLines(title, details), BorderLayout.CENTER);
        return row;
    }

    private JComponent noticeRow(String title, String details, boolean last) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(rowBorder(last));
        row.add(twoLines(title, details), BorderLayout.CENTER);
        return row;
    }

    private static javax.swing.border.Border rowBorder(boolean last) {
        EmptyBorder pad = new EmptyBorder(11, 0, 11, 0);
        return last ? pad : BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, ROW_LINE), pad);
    }

    private static JComponent twoLines(String title, String details) {
        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 13));
        t.setForeground(INK);
        JLabel d = new JLabel(details);
        d.setFont(font(Font.PLAIN, 12));
        d.setForeground(INK_2);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(2));
        p.add(d);
        return p;
    }

    /* ===================== HELPERS ===================== */

    private static JPanel rowsPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    /** Keeps content at its natural height instead of stretching. */
    private static JComponent topAligned(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.NORTH);
        return p;
    }

    /** Rounded green time label (10:30). */
    private static class TimePill extends JLabel {
        TimePill(String text) {
            super(text, SwingConstants.CENTER);
            setOpaque(false);
            setFont(font(Font.BOLD, 12));
            setForeground(PILL_FG);
            setPreferredSize(new Dimension(66, 30));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PILL_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ===================== MOCK DATA (replace with DB calls) ===================== */

    private void loadMockData() {
        // TODO: Setup database here - retrieve today's timetable sessions for the lecturer
        //       (time, module name, module code · hall · student count), ordered by time
        List<String[]> schedule = new ArrayList<>();
        schedule.add(new String[]{"10:30", "Software Engineering Principles", "CS3022 · Lab 3 · 92 students"});
        schedule.add(new String[]{"13:00", "Data Structures & Algorithms",     "CS2042 · LT-1 · 104 students"});
        schedule.add(new String[]{"15:30", "Project Consultation",             "CS4091 · Office 2.14 · by appointment"});
        setSchedule(schedule);

        // TODO: Setup database here - retrieve the latest notices for the lecturer's modules
        //       (title, posted date · module code), newest first
        List<String[]> notices = new ArrayList<>();
        notices.add(new String[]{"CS3012 – Assignment 2 deadline extended", "Posted 25 Oct · CS3012"});
        notices.add(new String[]{"Lab session rescheduled to Thursday",      "Posted 22 Oct · CS3022"});
        notices.add(new String[]{"Mid-term exam timetable released",          "Posted 20 Oct · All courses"});
        setNotices(notices);
    }
}