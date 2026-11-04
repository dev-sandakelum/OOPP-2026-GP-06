package UI.Pages.Officer;

import Exception.DataAccessException;
import UI.Pages.Components.Dot;
import UI.Pages.Components.KpiCard;
import UI.Pages.Components.NavIcon;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.SectionCard;
import data.MedicalData;
import data.TechnicalOfficerData;
import data.UserData;
import model.Medical;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Officer "Overview" tab: 4 KPI cards on top, course attendance table + pending medicals below.
// Everything comes from the database. refresh() reloads it (called after a medical decision
// or after attendance is saved).
public class Overview extends Page {

    private static final String[] SUMMARY_COLS = {"Code", "Course", "Sessions", "Avg %"};
    private static final int PENDING_LIST_LIMIT = 6;

    private final TechnicalOfficerData officerData = new TechnicalOfficerData();
    private final MedicalData medicalData = new MedicalData();
    private final UserData userData = new UserData();

    private final JPanel body = new JPanel(new BorderLayout(0, 20));

    public Overview(User user) {
        super("Overview",
                "Welcome back, " + ((user != null && user.getName() != null) ? user.getName() : "Technical Officer")
                        + "  ·  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                null);

        body.setOpaque(false);
        setScrollBody(body);
        refresh();
    }

    /** Reloads every number and list on this page from the database. */
    public void refresh() {
        int students = 0, sessionsToday = 0, pending = 0, courses = 0;
        List<String[]> summary = List.of();
        List<Medical> pendingList = List.of();
        try {
            students = officerData.countTotalStudents();
            sessionsToday = officerData.countSessionsToday();
            pending = officerData.countPendingMedicals();
            courses = officerData.countCurrentCourses();
            summary = officerData.getCourseAttendanceSummary();
            pendingList = medicalData.findByStatus("PENDING");
        } catch (DataAccessException ex) {
            info(this, "Failed to load the overview: " + ex.getMessage());
        }

        body.removeAll();
        body.add(kpiRow(students, sessionsToday, pending, courses), BorderLayout.NORTH);
        body.add(lowerRow(summary, pendingList), BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
    }

    // -------------------------------------------------------------------------
    //  KPI cards
    // -------------------------------------------------------------------------

    private JComponent kpiRow(int students, int sessionsToday, int pending, int courses) {
        String[][] kpis = {
                {"Total students", String.valueOf(students), "Undergraduates"},
                {"Sessions today", String.valueOf(sessionsToday), "Recorded today"},
                {"Pending medicals", String.valueOf(pending), "Need review"},
                {"Courses active", String.valueOf(courses), "This semester"}
        };
        NavIcon[] icons = {NavIcon.STUDENTS, NavIcon.REGISTER, NavIcon.PLUS_BOX, NavIcon.BOOK};
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

    private JComponent lowerRow(List<String[]> summary, List<Medical> pendingList) {
        JComponent attendance = new SectionCard("Course attendance", NavIcon.REGISTER, summaryTable(summary));
        JComponent medicals = new SectionCard("Pending medicals", NavIcon.MEDICAL, pendingBody(pendingList));
        // width 1 so the weights below decide the split instead of the content's preferred width
        attendance.setPreferredSize(new Dimension(1, attendance.getPreferredSize().height));
        medicals.setPreferredSize(new Dimension(1, medicals.getPreferredSize().height));

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;

        g.gridx = 0; g.weightx = 0.6; g.insets = new Insets(0, 0, 0, 8);
        row.add(attendance, g);
        g.gridx = 1; g.weightx = 0.4; g.insets = new Insets(0, 8, 0, 0);
        row.add(medicals, g);
        return row;
    }

    // course_code, course_name, sessions_count, avg_attendance% (offering_id is not shown)
    private JComponent summaryTable(List<String[]> summary) {
        ReadOnlyTableModel model = new ReadOnlyTableModel(SUMMARY_COLS);
        for (String[] r : summary) {
            model.addRow(new String[]{r[1], r[2], r[3], r[4]});
        }
        JTable table = table(model, null, -1);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        return tableCard(table);
    }

    private JComponent pendingBody(List<Medical> pendingList) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        if (pendingList.isEmpty()) {
            JLabel none = new JLabel("No pending medicals.");
            none.setFont(font(Font.PLAIN, 14));
            none.setForeground(INK_2);
            p.add(none);
            return p;
        }

        int shown = Math.min(PENDING_LIST_LIMIT, pendingList.size());
        for (int i = 0; i < shown; i++) {
            Medical m = pendingList.get(i);
            User student = userData.findById(m.getStudentId());
            String name = student == null ? "Student " + m.getStudentId() : student.getName();
            String reason = m.getReason() == null ? "" : "  ·  " + m.getReason();
            p.add(pendingRow(name, m.getMedicalDate() + reason));
            if (i < shown - 1) {
                JSeparator sep = new JSeparator();
                sep.setForeground(LINE);
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                p.add(sep);
            }
        }
        if (pendingList.size() > shown) {
            JLabel more = new JLabel("+ " + (pendingList.size() - shown) + " more in the Medicals tab");
            more.setFont(font(Font.PLAIN, 12));
            more.setForeground(INK_2);
            more.setBorder(new EmptyBorder(10, 0, 0, 0));
            p.add(more);
        }
        return p;
    }

    private JComponent pendingRow(String title, String sub) {
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
        left.add(new Dot(WARN), BorderLayout.NORTH);

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