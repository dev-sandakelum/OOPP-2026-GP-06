package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import data.AttendanceData;
import data.CourseData;
import data.CourseOfferingData;
import data.EnrollmentData;
import data.SessionData;
import data.UserData;
import model.Course;
import model.CourseOffering;
import model.Enrollment;
import model.Session;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static UI.Kit.Palette.*;

public class Attendance extends LecturerUI.Page {

    // fixed column widths so the header and every row line up (Student takes the rest)
    private static final int COL_NO = 48, COL_REG = 150, COL_STATUS = 230;
    private static final int ROW_H = 52;

    // ---------- Components ----------
    private JComboBox<String> courseBox;
    private JSpinner dateSpinner;
    private JComboBox<String> sessionBox;

    private final JLabel enrolledValue = new JLabel("0");
    private final JLabel presentValue  = new JLabel("0");
    private final JLabel absentValue   = new JLabel("0");
    private final JLabel rateValue     = new JLabel("0%");
    private final JLabel studentCount  = new JLabel();

    private JPanel rowsPanel;
    private final List<StudentRow> studentRows = new ArrayList<>();

    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final UserData userData = new UserData();
    private final SessionData sessionData = new SessionData();
    private final AttendanceData attendanceData = new AttendanceData();
    private final int lecturerId;
    private List<CourseOffering> offerings = new ArrayList<>();
    private List<Session> sessions = new ArrayList<>();   // sessions shown in sessionBox

    public Attendance(User user) {
        super("Attendance", "Mark attendance for a session", null);
        this.lecturerId = user == null ? 0 : user.getUser_id();

        //  1. filters + actions   2. summary cards   3. student list
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(stretch(buildControls()));
        body.add(Box.createVerticalStrut(16));
        body.add(stretch(buildStats()));
        body.add(Box.createVerticalStrut(16));
        body.add(stretch(buildTable()));
        setScrollBody(body);

        loadCourses();
    }

    // =====================================================================
    //  UI
    // =====================================================================

    /** Card with Course / Date / Session on the left and the two actions on the right. */
    private JComponent buildControls() {
        courseBox = new JComboBox<>();
        styleInput(courseBox, 280);

        dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        styleInput(dateSpinner, 150);

        sessionBox = new JComboBox<>();
        styleInput(sessionBox, 170);
        sessionBox.addActionListener(e -> loadStudents());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filters.setOpaque(false);
        filters.add(labelled("Course", courseBox));
        filters.add(Box.createHorizontalStrut(12));
        filters.add(labelled("Date", dateSpinner));
        filters.add(Box.createHorizontalStrut(12));
        filters.add(labelled("Session", sessionBox));
        filters.add(Box.createHorizontalStrut(12));

        // sessions are only looked up when the lecturer presses Search
        LecturerUI.Btn search = new LecturerUI.Btn("Search sessions", true);
        search.addActionListener(e -> searchSessions());
        filters.add(labelled(" ", search));   // blank caption keeps the button level with the inputs

        LecturerUI.Btn markAll = new LecturerUI.Btn("Mark all present", false);
        markAll.addActionListener(e -> markAllPresent());
        LecturerUI.Btn submit = new LecturerUI.Btn("Submit attendance", true);
        submit.addActionListener(e -> submitAttendance());

        // actions go on their own row below the filters, so they never overlap on narrow windows
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, LINE), new EmptyBorder(14, 0, 0, 0)));
        actions.add(markAll);
        actions.add(submit);

        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.add(filters, BorderLayout.NORTH);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    /** Four summary cards, same style as the Overview KPIs. */
    private JComponent buildStats() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard(enrolledValue, "Enrolled", "Students in this course", true));
        stats.add(statCard(presentValue, "Present", "Marked present", false));
        stats.add(statCard(absentValue, "Absent", "Marked absent", false));
        stats.add(statCard(rateValue, "Attendance rate", "For this session", false));
        return stats;
    }

    private JComponent statCard(JLabel value, String label, String sub, boolean dark) {
        Card c = new Card(dark);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(16, 20, 16, 20));
        c.setPreferredSize(new Dimension(0, 112));
        Color muted = dark ? ON_BRAND_2 : INK_2;

        JLabel l = new JLabel(label);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(muted);
        value.setFont(font(Font.BOLD, 30));
        value.setForeground(dark ? Color.WHITE : INK);
        JLabel s = new JLabel(sub);
        s.setFont(font(Font.PLAIN, 12));
        s.setForeground(muted);

        c.add(l);
        c.add(Box.createVerticalGlue());
        c.add(value);
        c.add(s);
        return c;
    }

    /** Card with a title bar, column header and one line per student. */
    private JComponent buildTable() {
        JLabel title = new JLabel("Students");
        title.setFont(font(Font.BOLD, 15));
        title.setForeground(INK);
        studentCount.setFont(font(Font.PLAIN, 12));
        studentCount.setForeground(INK_2);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(new EmptyBorder(4, 12, 12, 12));
        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(studentCount, BorderLayout.EAST);

        JPanel head = new HeaderStrip();
        head.setBorder(new EmptyBorder(0, 12, 0, 12));
        head.setPreferredSize(new Dimension(0, 40));
        addRowCells(head, headerLabel("#"), headerLabel("REG. NO"), headerLabel("STUDENT"), headerLabel("STATUS"));

        rowsPanel = new JPanel();
        rowsPanel.setOpaque(false);
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleBar, BorderLayout.NORTH);
        top.add(head, BorderLayout.SOUTH);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 8, 8, 8));   // keeps the header strip inside the rounded corners
        card.add(top, BorderLayout.NORTH);
        card.add(rowsPanel, BorderLayout.CENTER);
        return card;
    }

    // =====================================================================
    //  DATA LOADING  (loaded through the data layers)
    // =====================================================================

    /** Fills the course dropdown. */
    private void loadCourses() {
        offerings = offeringData.findByLecturer(lecturerId);
        courseBox.removeAllItems();
        for (CourseOffering o : offerings) {
            Course c = courseData.findById(o.getCourse_id());
            courseBox.addItem(c == null ? "Offering " + o.getOffering_id()
                    : c.getCourse_code() + " - " + c.getCourse_name());
        }
    }

    /** Called by the "Search sessions" button: finds the sessions of the chosen course and date. */
    private void searchSessions() {
        if (courseBox.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course first.");
            return;
        }
        loadSessions();
        if (sessions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sessions found for this course on the selected date.");
        }
    }

    /** Fills the session dropdown with the sessions of the selected course on the selected date. */
    private void loadSessions() {
       sessions.clear();
       sessionBox.removeAllItems();

        int index = courseBox.getSelectedIndex();
        if (index >= 0) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format((Date) dateSpinner.getValue());
            for (Session s : sessionData.findByOffering(offerings.get(index).getOffering_id())) {
                if (s.getSessionDate() != null && s.getSessionDate().toString().equals(date)) {
                    sessions.add(s);
                    String time = s.getStartTime() == null ? "" : " " + s.getStartTime().toString().substring(0, 5);
                    sessionBox.addItem(s.getSessionType() + time);
                }
            }
        }
        loadStudents();
    }

    /** Fills the student list for the selected session. */
    private void loadStudents() {
        if (rowsPanel == null) return;

        rowsPanel.removeAll();
        studentRows.clear();

        int courseIndex = courseBox.getSelectedIndex();
        int sessionIndex = sessionBox.getSelectedIndex();
        if (courseIndex >= 0 && sessionIndex >= 0 && sessionIndex < sessions.size()) {
            // students already saved for this session (id -> status)
            java.util.Map<Integer, String> saved = new java.util.HashMap<>();
            for (model.Attendance a : attendanceData.findBySession(sessions.get(sessionIndex).getSessionId())) {
                saved.put(a.getStudentId(), a.getAttendStatus());
            }

            // every enrolled student; PRESENT by default when nothing is saved yet
            for (Enrollment en : enrollmentData.findByOffering(offerings.get(courseIndex).getOffering_id())) {
                User student = userData.findById(en.getStudent_id());
                String regNo = String.valueOf(en.getStudent_id());
                StudentRow row = new StudentRow(studentRows.size() + 1, regNo, student == null ? "" : student.getName());
                if ("ABSENT".equals(saved.get(en.getStudent_id()))) row.absent.setSelected(true);
                studentRows.add(row);
                rowsPanel.add(row.panel);
            }
        }

        updateStats();
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    // =====================================================================
    //  ACTIONS
    // =====================================================================
    private void markAllPresent() {
        for (StudentRow r : studentRows) {
            r.present.setSelected(true);
        }
        updateStats();
    }

    private void submitAttendance() {
        int sessionIndex = sessionBox.getSelectedIndex();
        if (studentRows.isEmpty() || sessionIndex < 0) {
            JOptionPane.showMessageDialog(this, "No students to submit.");
            return;
        }

        // record the date of the session itself, so editing a past session keeps its date
        Session session = sessions.get(sessionIndex);
        int sessionId = session.getSessionId();
        java.sql.Date sessionDate = session.getSessionDate();
        try {
            for (StudentRow r : studentRows) {
                String status = r.isPresent() ? "PRESENT" : "ABSENT";
                attendanceData.save(new model.Attendance(Integer.parseInt(r.regNo), sessionId, status, sessionDate));
            }
            JOptionPane.showMessageDialog(this, "Attendance submitted successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save attendance: " + ex.getMessage());
        }
    }

    private void updateStats() {
        int total = studentRows.size();
        int present = 0;
        for (StudentRow r : studentRows) {
            if (r.isPresent()) present++;
        }
        enrolledValue.setText(String.valueOf(total));
        presentValue.setText(String.valueOf(present));
        absentValue.setText(String.valueOf(total - present));
        rateValue.setText(total == 0 ? "0%" : Math.round(present * 100.0 / total) + "%");
        studentCount.setText(total + (total == 1 ? " student" : " students"));
    }

    // =====================================================================
    //  Helpers
    // =====================================================================
    private static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 11));
        l.setForeground(INK_2);
        return l;
    }

    /** Small caption above an input. */
    private static JComponent labelled(String caption, JComponent input) {
        JLabel l = new JLabel(caption);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(INK_2);
        l.setAlignmentX(LEFT_ALIGNMENT);
        input.setAlignmentX(LEFT_ALIGNMENT);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(input);
        return p;
    }

    private static void styleInput(JComponent c, int width) {
        c.setFont(font(Font.PLAIN, 13));
        c.setForeground(INK);
        c.setBackground(SURFACE);
        Dimension d = new Dimension(width, 38);
        c.setPreferredSize(d);
        c.setMaximumSize(d);
    }

    /** Lets a section fill the full width inside the vertical BoxLayout. */
    private static JComponent stretch(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return c;
    }

    /** Lays out 4 cells: fixed widths for #, Reg. no and Status, Student fills the rest. */
    private static void addRowCells(JPanel panel, Component no, Component reg, Component name, Component status) {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        fixWidth(no, COL_NO);
        fixWidth(reg, COL_REG);
        fixWidth(name, 0);
        fixWidth(status, COL_STATUS);

        gc.gridx = 0; gc.weightx = 0; panel.add(no, gc);
        gc.gridx = 1; gc.weightx = 0; panel.add(reg, gc);
        gc.gridx = 2; gc.weightx = 1; panel.add(name, gc);
        gc.gridx = 3; gc.weightx = 0; panel.add(status, gc);
    }

    private static void fixWidth(Component c, int w) {
        Dimension d = new Dimension(w, c.getPreferredSize().height);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
    }

    // =====================================================================
    //  Inner classes
    // =====================================================================

    /** One student line: number, reg no, name, Present / Absent options. */
    private class StudentRow {
        final String regNo;
        final String name;
        final JRadioButton present = new JRadioButton("Present", true);
        final JRadioButton absent  = new JRadioButton("Absent");
        final JPanel panel = new JPanel();

        StudentRow(int number, String regNo, String name) {
            this.regNo = regNo;
            this.name = name;

            ButtonGroup group = new ButtonGroup();
            group.add(present);
            group.add(absent);

            for (JRadioButton rb : new JRadioButton[]{present, absent}) {
                rb.setOpaque(false);
                rb.setFont(font(Font.PLAIN, 13));
                rb.setForeground(INK);
                rb.setFocusPainted(false);
                rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                rb.addActionListener(e -> updateStats());
            }

            JLabel noLbl = new JLabel(String.valueOf(number));
            noLbl.setFont(font(Font.PLAIN, 13));
            noLbl.setForeground(INK_2);

            JLabel regLbl = new JLabel(regNo);
            regLbl.setFont(font(Font.PLAIN, 13));
            regLbl.setForeground(INK_2);

            JLabel nameLbl = new JLabel(name);
            nameLbl.setFont(font(Font.BOLD, 13));
            nameLbl.setForeground(INK);

            JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            status.setOpaque(false);
            status.add(present);
            status.add(Box.createHorizontalStrut(18));
            status.add(absent);

            panel.setOpaque(false);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(0, 12, 0, 12)));
            panel.setPreferredSize(new Dimension(0, ROW_H));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_H));
            panel.setAlignmentX(LEFT_ALIGNMENT);
            addRowCells(panel, noLbl, regLbl, nameLbl, status);
        }

        boolean isPresent() {
            return present.isSelected();
        }
    }

    /** Soft rounded strip behind the column titles. */
    private static class HeaderStrip extends JPanel {
        HeaderStrip() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(SURFACE_2);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
