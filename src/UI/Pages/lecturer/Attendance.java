package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.ReadOnlyTableModel;
import data.AttendanceData;
import data.BatchData;
import data.CourseData;
import data.CourseOfferingData;
import data.EnrollmentData;
import data.LecturerData;
import data.SessionData;
import data.UndergraduateData;
import data.UserData;
import model.Batch;
import model.Course;
import model.CourseOffering;
import model.Enrollment;
import model.Lecturer;
import model.Session;
import model.Undergraduate;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.table;

public class Attendance extends LecturerUI.Page {

    // fixed column widths so the header and every row line up (Student takes the rest)
    private static final int COL_NO = 48, COL_REG = 150, COL_STATUS = 230;
    private static final int ROW_H = 52;

    // ---------- Components ----------
    private JComboBox<String> batchBox;
    private JComboBox<String> courseBox;
    private JSpinner dateSpinner;
    private JComboBox<String> startBox;   // start of the session time range
    private JComboBox<String> endBox;     // end of the session time range

    private final JLabel enrolledValue = new JLabel("0");
    private final JLabel presentValue  = new JLabel("0");
    private final JLabel absentValue   = new JLabel("0");
    private final JLabel rateValue     = new JLabel("0%");
    private final JLabel studentCount  = new JLabel();

    // Eligibility table: attendance above 80% = Eligible, otherwise Not eligible
    private static final double ELIGIBLE_ABOVE = 80;
    private static final int STATUS_COL = 5;
    private final DefaultTableModel eligibilityModel = new ReadOnlyTableModel(
            new String[]{"Reg. No", "Student", "Attended", "Total Sessions", "Attendance %", "Status"});
    private final JTable eligibilityTable = table(eligibilityModel, statusColors(), STATUS_COL);

    private JPanel rowsPanel;
    private final List<StudentRow> studentRows = new ArrayList<>();

    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final UserData userData = new UserData();
    private final SessionData sessionData = new SessionData();
    private final AttendanceData attendanceData = new AttendanceData();
    private final LecturerData lecturerData = new LecturerData();
    private final BatchData batchData = new BatchData();
    private final int lecturerId;
    private List<Batch> batches = new ArrayList<>();   // batches of the lecturer's department (same order as batchBox)
    private List<CourseOffering> offerings = new ArrayList<>();
    private boolean updating = false;   // true while a dropdown is being refilled (its listeners are skipped)
    private final UndergraduateData undergraduateData = new UndergraduateData();

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
        body.add(Box.createVerticalStrut(16));
        body.add(stretch(buildEligibilityCard()));
        setScrollBody(body);

        loadBatches();   // selecting the first batch loads its courses
    }

    // =====================================================================
    //  UI
    // =====================================================================

    /** Card with Course / Date / Session on the left and the two actions on the right. */
    private JComponent buildControls() {
        batchBox = new JComboBox<>();
        styleInput(batchBox, 200);
        batchBox.addActionListener(e -> {   // a new batch has other courses and other students
            loadCourses();
            loadEligibility();
            loadStudents();
        });

        courseBox = new JComboBox<>();
        styleInput(courseBox, 280);
        courseBox.addActionListener(e -> {
            if (updating) return;
            loadEligibility();   // eligibility of the chosen course
            loadStudents();      // the saved attendance of this course
        });

        dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        styleInput(dateSpinner, 150);

        // start and end time: 08:00 to 16:00, every 30 minutes
        startBox = new JComboBox<>(timeOptions());
        styleInput(startBox, 100);
        startBox.removeItemAt(startBox.getItemCount() - 1);   // 16:00 can only be an end time
        endBox = new JComboBox<>();
        styleInput(endBox, 100);
        startBox.addActionListener(e -> {
            fillEndBox();
            loadStudents();
        });
        endBox.addActionListener(e -> {
            if (!updating) loadStudents();
        });
        dateSpinner.addChangeListener(e -> loadStudents());
        fillEndBox();

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filters.setOpaque(false);
        filters.add(labelled("Batch", batchBox));
        filters.add(Box.createHorizontalStrut(12));
        filters.add(labelled("Course", courseBox));
        filters.add(Box.createHorizontalStrut(12));
        filters.add(labelled("Date", dateSpinner));
        filters.add(Box.createHorizontalStrut(12));
        filters.add(labelled("Start session", startBox));
        filters.add(Box.createHorizontalStrut(12));
        filters.add(labelled("End session", endBox));

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

    /** Card with the eligibility table: every enrolled student of the selected course. */
    private JComponent buildEligibilityCard() {
        JLabel title = new JLabel("Attendance eligibility");
        title.setFont(font(Font.BOLD, 15));
        title.setForeground(INK);
        JLabel hint = new JLabel("Eligible when attendance is above " + (int) ELIGIBLE_ABOVE + "%");
        hint.setFont(font(Font.PLAIN, 12));
        hint.setForeground(INK_2);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(new EmptyBorder(4, 12, 12, 12));
        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(hint, BorderLayout.EAST);

        // header + rows without a scroll pane, so the card grows with the rows
        JPanel grid = new JPanel(new BorderLayout());
        grid.setOpaque(false);
        grid.add(eligibilityTable.getTableHeader(), BorderLayout.NORTH);
        grid.add(eligibilityTable, BorderLayout.CENTER);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 8, 8, 8));
        card.add(titleBar, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // chip colors of the Status column
    private static Map<String, Color> statusColors() {
        Map<String, Color> colors = new HashMap<>();
        colors.put("Eligible", OK);
        colors.put("Not eligible", BAD);
        return colors;
    }

    // =====================================================================
    //  DATA LOADING  (loaded through the data layers)
    // =====================================================================

    /** Fills the batch dropdown with the batches of the lecturer's department only. */
    private void loadBatches() {
        Lecturer lecturer = lecturerData.findById(lecturerId);
        if (lecturer == null) return;

        batches = batchData.findByDepartment(lecturer.getDep_id());
        for (Batch b : batches) {
            batchBox.addItem(b.getBatch_name() + " (" + b.getIntake_year() + ")");
        }
    }

    /**
     * Fills the course dropdown with the lecturer's courses of the selected batch.
     * A course belongs to a batch when its batch (e.g. "2025/26") starts with the intake year of the batch (2025).
     */
    private void loadCourses() {
        updating = true;
        offerings = new ArrayList<>();
        courseBox.removeAllItems();

        int batchIndex = batchBox.getSelectedIndex();
        if (batchIndex < 0) {
            updating = false;
            return;
        }
        String intakeYear = String.valueOf(batches.get(batchIndex).getIntake_year());

        for (CourseOffering o : offeringData.findByLecturer(lecturerId)) {
            Course c = courseData.findById(o.getCourse_id());
            if (c == null || c.getBatch() == null || !c.getBatch().startsWith(intakeYear)) continue;
            offerings.add(o);
            courseBox.addItem(c.getCourse_code() + " - " + c.getCourse_name());
        }
        updating = false;
    }

    // The end session list only has the times after the start session (the old end time is kept if it is still valid)
    private void fillEndBox() {
        updating = true;
        Object oldEnd = endBox.getSelectedItem();
        endBox.removeAllItems();

        LocalTime start = LocalTime.parse((String) startBox.getSelectedItem());
        for (String time : timeOptions()) {
            if (LocalTime.parse(time).isAfter(start)) endBox.addItem(time);
        }
        endBox.setSelectedItem(oldEnd);                          // does nothing if oldEnd is not in the list
        if (endBox.getSelectedItem() == null || oldEnd == null) {
            endBox.setSelectedIndex(endBox.getItemCount() - 1);   // default: 16:00
        }
        updating = false;
    }

    // "08:00", "08:30", ... "16:00"
    private static String[] timeOptions() {
        List<String> times = new ArrayList<>();
        for (LocalTime t = LocalTime.of(8, 0); !t.isAfter(LocalTime.of(16, 0)); t = t.plusMinutes(30)) {
            times.add(t.toString());   // LocalTime prints as HH:mm
        }
        return times.toArray(new String[0]);
    }

    /**
     * Fills the eligibility table for the selected course.
     * Attendance % = sessions the student was PRESENT / sessions that have attendance recorded for the student.
     */
    private void loadEligibility() {
        eligibilityModel.setRowCount(0);

        int index = courseBox.getSelectedIndex();
        if (index < 0) return;
        int offeringId = offerings.get(index).getOffering_id();

        for (Enrollment en : enrollmentData.findByOffering(offeringId)) {
            User student = userData.findById(en.getStudent_id());

            int total = 0, attended = 0;
            for (model.Attendance a : attendanceData.findByStudentAndOffering(en.getStudent_id(), offeringId)) {
                total++;
                if ("PRESENT".equals(a.getAttendStatus())) attended++;
            }

            double percentage = total == 0 ? 0 : attended * 100.0 / total;
            String status = percentage > ELIGIBLE_ABOVE ? "Eligible" : "Not eligible";
            eligibilityModel.addRow(new Object[]{String.valueOf(en.getStudent_id()),
                    student == null ? "" : student.getName(), attended, total,
                    String.format("%.1f%%", percentage), status});
        }
    }

    /** The saved session of the selected course, date, start and end time (null if it does not exist yet). */
    private Session findSession() {
        int courseIndex = courseBox.getSelectedIndex();
        if (courseIndex < 0 || startBox.getSelectedItem() == null || endBox.getSelectedItem() == null) return null;

        java.sql.Date date = selectedDate();
        LocalTime start = LocalTime.parse((String) startBox.getSelectedItem());
        LocalTime end = LocalTime.parse((String) endBox.getSelectedItem());

        for (Session s : sessionData.findByOffering(offerings.get(courseIndex).getOffering_id())) {
            if (s.getSessionDate() != null && s.getSessionDate().toString().equals(date.toString())
                    && s.getStartTime() != null && s.getStartTime().toLocalTime().equals(start)
                    && s.getEndTime() != null && s.getEndTime().toLocalTime().equals(end)) {
                return s;
            }
        }
        return null;
    }

    // the date picked in the date spinner
    private java.sql.Date selectedDate() {
        Date picked = (Date) dateSpinner.getValue();
        return java.sql.Date.valueOf(picked.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * Lists ALL the students of the selected batch.
     * If attendance was already saved for the selected course, date and time, it is shown;
     * otherwise every student is PRESENT by default.
     */
    private void loadStudents() {
        if (rowsPanel == null) return;

        rowsPanel.removeAll();
        studentRows.clear();

        int batchIndex = batchBox.getSelectedIndex();
        if (batchIndex >= 0) {
            // saved attendance of this session (student id -> status)
            Map<Integer, String> saved = new HashMap<>();
            Session session = findSession();
            if (session != null) {
                for (model.Attendance a : attendanceData.findBySession(session.getSessionId())) {
                    saved.put(a.getStudentId(), a.getAttendStatus());
                }
            }

            for (Undergraduate student : undergraduateData.findByBatch(batches.get(batchIndex).getBatch_id())) {
                StudentRow row = new StudentRow(studentRows.size() + 1,
                        String.valueOf(student.getUser_id()), student.getName());
                if ("ABSENT".equals(saved.get(student.getUser_id()))) row.absent.setSelected(true);
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
        int courseIndex = courseBox.getSelectedIndex();
        if (studentRows.isEmpty()) {
            UI.Pages.Components.Ui.info(this, "No students to submit. Please select a batch.");
            return;
        }
        if (courseIndex < 0) {
            UI.Pages.Components.Ui.info(this, "Please select a course first.");
            return;
        }

        try {
            // the session (course + date + start/end time) holds the time; it is created if it does not exist yet
            Session session = findSession();
            if (session == null) session = createSession(offerings.get(courseIndex).getOffering_id());

            for (StudentRow r : studentRows) {
                String status = r.isPresent() ? "PRESENT" : "ABSENT";
                attendanceData.save(new model.Attendance(Integer.parseInt(r.regNo),
                        session.getSessionId(), status, session.getSessionDate()));
            }
            UI.Pages.Components.Ui.info(this, "Attendance submitted for " + studentRows.size() + " students\n("
                    + session.getSessionDate() + ", " + startBox.getSelectedItem() + " - " + endBox.getSelectedItem() + ").");
            loadEligibility();   // the attendance % changed
        } catch (Exception ex) {
            UI.Pages.Components.Ui.info(this, "Could not save attendance: " + ex.getMessage());
        }
    }

    // Saves a new session with the selected date, start time and end time
    private Session createSession(int offeringId) {
        Session session = new Session();
        session.setOfferingId(offeringId);
        session.setSessionNumber(sessionData.findByOffering(offeringId).size() + 1);
        session.setSessionType("LECTURE");
        session.setSessionDate(selectedDate());
        session.setStartTime(java.sql.Time.valueOf(LocalTime.parse((String) startBox.getSelectedItem())));
        session.setEndTime(java.sql.Time.valueOf(LocalTime.parse((String) endBox.getSelectedItem())));
        return sessionData.insert(session);
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
