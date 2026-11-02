package UI.Pages.Officer;

import Exception.DataAccessException;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.SimpleDocListener;
import data.AttendanceData;
import data.SessionData;
import data.TechnicalOfficerData;
import model.Attendance;
import model.Session;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Officer "Attendance Register" tab: pick course + date + session, mark every student, save.
public class AttendanceRegister extends Page {

    private static final String[] COLS = {"Student ID", "Name", "Batch", "Present / Absent"};
    private static final int STATUS_COL = 3;
    private static final String[] MARKS = {"PRESENT", "ABSENT"};
    private static final Pattern DATE_FORMAT = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private final TechnicalOfficerData officerData = new TechnicalOfficerData();
    private final SessionData sessionData = new SessionData();
    private final AttendanceData attendanceData = new AttendanceData();
    private final Runnable onChanged;   // lets the Overview refresh after a save

    private final List<String[]> courses = new ArrayList<>();   // {offering_id, code, name, sessions, avg%}
    private final List<Session> sessions = new ArrayList<>();   // sessions of the picked course on the picked date
    private boolean loading;                                    // ignore combo events while refilling

    private final JComboBox<String> coursePicker = new JComboBox<>();
    private final JTextField dateField = field(10);
    private final JComboBox<String> sessionPicker = new JComboBox<>();

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == STATUS_COL;   // only the Present/Absent cell can be changed
        }
    };
    private final JTable table;

    public AttendanceRegister(Runnable onChanged) {
        super("Attendance Register", "Mark attendance on behalf of lecturers", null);
        this.onChanged = onChanged == null ? () -> { } : onChanged;

        table = table(model, Map.of(
                "PRESENT", OK,
                "ABSENT", BAD,
                "LATE", WARN,
                "MEDICAL", BRAND
        ), STATUS_COL);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(STATUS_COL)
                .setCellEditor(new DefaultCellEditor(new JComboBox<>(MARKS)));

        dateField.setText(LocalDate.now().toString());
        coursePicker.setPreferredSize(new Dimension(300, 36));
        sessionPicker.setPreferredSize(new Dimension(230, 36));

        coursePicker.addActionListener(e -> { if (!loading) loadSessions(); });
        dateField.getDocument().addDocumentListener(new SimpleDocListener(() -> { if (!loading) loadSessions(); }));
        sessionPicker.addActionListener(e -> { if (!loading) loadRegister(); });

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(toolbar(), BorderLayout.NORTH);
        body.add(tableCard(table), BorderLayout.CENTER);
        setBody(body);

        loadCourses();
    }

    private JComponent toolbar() {
        RoundedButton newBtn = new RoundedButton("+ New session", false);
        newBtn.addActionListener(e -> newSession());

        RoundedButton deleteBtn = new RoundedButton("Delete session", false);
        deleteBtn.addActionListener(e -> deleteSession());

        RoundedButton saveBtn = new RoundedButton("Save attendance", true);
        saveBtn.addActionListener(e -> saveAttendance());

        // row 1: which register to show
        JPanel pickers = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pickers.setOpaque(false);
        pickers.add(coursePicker);
        pickers.add(dateField);
        pickers.add(sessionPicker);

        // row 2: actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(newBtn);
        actions.add(deleteBtn);
        actions.add(saveBtn);

        JPanel bar = new JPanel(new GridLayout(2, 1, 0, 10));
        bar.setOpaque(false);
        bar.add(pickers);
        bar.add(actions);
        return bar;
    }

    // ---------------------------------------------------------------- loading

    private void loadCourses() {
        courses.clear();
        loading = true;
        coursePicker.addActionListener(e -> { if (!loading) jumpToLatestSession(); });
        try {
            courses.addAll(officerData.getCourseAttendanceSummary());
        } catch (DataAccessException ex) {
            info(this, "Failed to load the courses: " + ex.getMessage());
        }
        for (String[] c : courses) {
            coursePicker.addItem(c[1] + " — " + c[2]);
        }
        loading = false;
        jumpToLatestSession();
    }

    // Puts the date field on the course's most recent session date (if it has any),
    // then loads the sessions for that date.
    private void jumpToLatestSession() {
        if (coursePicker.getSelectedIndex() >= 0) {
            try {
                Date latest = null;
                for (Session s : sessionData.findByOffering(offeringId())) {
                    Date d = s.getSessionDate();
                    if (d != null && (latest == null || d.after(latest))) latest = d;
                }
                if (latest != null) {
                    loading = true;                    // don't reload while setting the text
                    dateField.setText(latest.toString());
                    loading = false;
                }
            } catch (DataAccessException ex) {
                info(this, "Failed to load the sessions: " + ex.getMessage());
            }
        }
        loadSessions();
    }

    // sessions of the picked course on the picked date (date must look like 2026-10-22)
    private void loadSessions() {
        sessions.clear();
        loading = true;
        sessionPicker.removeAllItems();

        String date = dateField.getText().trim();
        int c = coursePicker.getSelectedIndex();
        if (c >= 0 && DATE_FORMAT.matcher(date).matches()) {
            try {
                Date picked = Date.valueOf(date);
                for (Session s : sessionData.findByOffering(offeringId())) {
                    if (picked.equals(s.getSessionDate())) sessions.add(s);
                }
            } catch (IllegalArgumentException ignored) {
                // e.g. 2026-13-45 — treated as "no date yet"
            } catch (DataAccessException ex) {
                info(this, "Failed to load the sessions: " + ex.getMessage());
            }
        }
        for (Session s : sessions) {
            sessionPicker.addItem(sessionLabel(s));
        }
        loading = false;
        loadRegister();
    }

    private String sessionLabel(Session s) {
        String time = s.getStartTime() == null ? "" : " · " + hhmm(s.getStartTime())
                                                      + (s.getEndTime() == null ? "" : "-" + hhmm(s.getEndTime()));
        String type = s.getSessionType() == null ? "" : " · " + s.getSessionType();
        return "Session " + s.getSessionNumber() + type + time;
    }

    private static String hhmm(Time t) {
        return t.toString().substring(0, 5);
    }

    // one row per enrolled student, pre-filled with the saved mark (new marks start as PRESENT)
    private void loadRegister() {
        stopEditing();
        model.setRowCount(0);
        Session s = selectedSession();
        if (s == null) return;
        try {
            for (String[] r : officerData.getSessionRegister(s.getSessionId())) {
                String mark = r[3].isEmpty() ? "PRESENT" : r[3];
                model.addRow(new Object[]{r[0], r[1], r[2], mark});
            }
        } catch (DataAccessException ex) {
            info(this, "Failed to load the register: " + ex.getMessage());
        }
    }

    private int offeringId() {
        return Integer.parseInt(courses.get(coursePicker.getSelectedIndex())[0]);
    }

    private Session selectedSession() {
        int i = sessionPicker.getSelectedIndex();
        return i < 0 || i >= sessions.size() ? null : sessions.get(i);
    }

    private void stopEditing() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
    }

    // ---------------------------------------------------------------- actions

    private void newSession() {
        // Build subject dropdown (all available courses)
        JComboBox<String> subjectCombo = new JComboBox<>();
        int preselectedIndex = -1;
        for (int i = 0; i < courses.size(); i++) {
            String[] c = courses.get(i);
            subjectCombo.addItem(c[1] + " — " + c[2]);  // code — name
            if (coursePicker.getSelectedIndex() >= 0 && i == coursePicker.getSelectedIndex()) {
                preselectedIndex = i;
            }
        }
        if (preselectedIndex >= 0) {
            subjectCombo.setSelectedIndex(preselectedIndex);
        }
        if (courses.isEmpty()) {
            info(this, "No courses available.");
            return;
        }

        subjectCombo.setFont(font(Font.PLAIN, 14));
        subjectCombo.setBackground(SURFACE);
        subjectCombo.setForeground(INK);

        // Date input
        JTextField dateInput = field(20);
        dateInput.setText(dateField.getText().trim());

        // Type dropdown
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"LECTURE", "PRACTICAL"});
        typeCombo.setFont(font(Font.PLAIN, 14));
        typeCombo.setBackground(SURFACE);
        typeCombo.setForeground(INK);

        // Start time
        JTextField startInput = field(20);
        startInput.setText("08:00");

        // End time
        JTextField endInput = field(20);
        endInput.setText("10:00");

        // Room dropdown
        JComboBox<String> roomCombo = new JComboBox<>(new String[]{"", "LH-01", "LH-02", "LH-03", "ICT Lab 1", "ICT Lab 2"});
        roomCombo.setFont(font(Font.PLAIN, 14));
        roomCombo.setBackground(SURFACE);
        roomCombo.setForeground(INK);

        // Build main panel with BoxLayout
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // Subject (full width)
        JLabel subjectLabel = label("Subject");
        subjectLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(subjectLabel);
        p.add(Box.createVerticalStrut(4));
        subjectCombo.setAlignmentX(LEFT_ALIGNMENT);
        subjectCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, subjectCombo.getPreferredSize().height));
        p.add(subjectCombo);
        p.add(Box.createVerticalStrut(12));

        // Date and Type row
        JPanel dateTypeRow = new JPanel(new GridLayout(1, 2, 12, 0));
        dateTypeRow.setOpaque(false);
        dateTypeRow.setAlignmentX(LEFT_ALIGNMENT);
        dateTypeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
        datePanel.setOpaque(false);
        JLabel dateLabel = label("Date (yyyy-MM-dd)");
        dateLabel.setAlignmentX(LEFT_ALIGNMENT);
        datePanel.add(dateLabel);
        datePanel.add(Box.createVerticalStrut(4));
        dateInput.setAlignmentX(LEFT_ALIGNMENT);
        datePanel.add(dateInput);

        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
        typePanel.setOpaque(false);
        JLabel typeLabel = label("Type");
        typeLabel.setAlignmentX(LEFT_ALIGNMENT);
        typePanel.add(typeLabel);
        typePanel.add(Box.createVerticalStrut(4));
        typeCombo.setAlignmentX(LEFT_ALIGNMENT);
        typePanel.add(typeCombo);

        dateTypeRow.add(datePanel);
        dateTypeRow.add(typePanel);
        p.add(dateTypeRow);
        p.add(Box.createVerticalStrut(12));

        // Start and End time row
        JPanel timeRow = new JPanel(new GridLayout(1, 2, 12, 0));
        timeRow.setOpaque(false);
        timeRow.setAlignmentX(LEFT_ALIGNMENT);
        timeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel startPanel = new JPanel();
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
        startPanel.setOpaque(false);
        JLabel startLabel = label("Start time (HH:mm)");
        startLabel.setAlignmentX(LEFT_ALIGNMENT);
        startPanel.add(startLabel);
        startPanel.add(Box.createVerticalStrut(4));
        startInput.setAlignmentX(LEFT_ALIGNMENT);
        startPanel.add(startInput);

        JPanel endPanel = new JPanel();
        endPanel.setLayout(new BoxLayout(endPanel, BoxLayout.Y_AXIS));
        endPanel.setOpaque(false);
        JLabel endLabel = label("End time (HH:mm)");
        endLabel.setAlignmentX(LEFT_ALIGNMENT);
        endPanel.add(endLabel);
        endPanel.add(Box.createVerticalStrut(4));
        endInput.setAlignmentX(LEFT_ALIGNMENT);
        endPanel.add(endInput);

        timeRow.add(startPanel);
        timeRow.add(endPanel);
        p.add(timeRow);
        p.add(Box.createVerticalStrut(12));

        // Room (full width)
        JLabel roomLabel = label("Room");
        roomLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(roomLabel);
        p.add(Box.createVerticalStrut(4));
        roomCombo.setAlignmentX(LEFT_ALIGNMENT);
        roomCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, roomCombo.getPreferredSize().height));
        p.add(roomCombo);

        if (!ask(this, "New session", p)) return;

        // Get selected course offering ID
        int selectedCourseIndex = subjectCombo.getSelectedIndex();
        if (selectedCourseIndex < 0) {
            info(this, "Select a subject.");
            return;
        }
        int offering = Integer.parseInt(courses.get(selectedCourseIndex)[0]);

        try {
            // Auto-increment session number for this course
            int next = 1;
            for (Session s : sessionData.findByOffering(offering)) {
                next = Math.max(next, s.getSessionNumber() + 1);
            }

            // Parse date
            Date sessionDate;
            try {
                sessionDate = Date.valueOf(dateInput.getText().trim());
            } catch (IllegalArgumentException ex) {
                info(this, "Enter the date as yyyy-MM-dd (for example 2026-10-22).");
                return;
            }

            // Parse times
            String startText = startInput.getText().trim();
            String endText   = endInput.getText().trim();
            Time startTime, endTime;
            try {
                startTime = startText.isEmpty() ? null : Time.valueOf(startText + ":00");
                endTime   = endText.isEmpty()   ? null : Time.valueOf(endText   + ":00");
            } catch (IllegalArgumentException ex) {
                info(this, "Check the times — use HH:mm format (for example 08:00).");
                return;
            }

            String selectedRoom = (String) roomCombo.getSelectedItem();
            String selectedType = (String) typeCombo.getSelectedItem();

            Session s = new Session();
            s.setOfferingId(offering);
            s.setSessionNumber(next);
            s.setSessionType(selectedType == null || selectedType.isEmpty() ? null : selectedType);
            s.setSessionDate(sessionDate);
            s.setStartTime(startTime);
            s.setEndTime(endTime);
            s.setRoom(selectedRoom == null || selectedRoom.isEmpty() ? null : selectedRoom);

            Session saved = sessionData.insert(s);
            
            // Update UI to show the newly created session's course and date
            loading = true;
            coursePicker.setSelectedIndex(selectedCourseIndex);
            dateField.setText(sessionDate.toString());
            loading = false;
            loadSessions();
            
            // Select the newly created session in the dropdown
            for (int i = 0; i < sessions.size(); i++) {
                if (sessions.get(i).getSessionId() == saved.getSessionId()) {
                    sessionPicker.setSelectedIndex(i);
                    break;
                }
            }
            onChanged.run();
        } catch (DataAccessException ex) {
            info(this, "Failed to create the session: " + ex.getMessage());
        }
    }

    private void deleteSession() {
        Session s = selectedSession();
        if (s == null) {
            info(this, "Select a session first.");
            return;
        }
        if (!confirm(this, "Delete \"" + sessionLabel(s) + "\" and all its attendance marks?")) return;
        try {
            sessionData.delete(s.getSessionId());
        } catch (DataAccessException ex) {
            info(this, "Failed to delete the session: " + ex.getMessage());
            return;
        }
        loadSessions();
        onChanged.run();
    }

    // Saves row by row, so marks saved before a failure stay saved and are visible immediately.
    private void saveAttendance() {
        stopEditing();
        Session s = selectedSession();
        if (s == null) {
            info(this, "Select a session first.");
            return;
        }
        if (model.getRowCount() == 0) {
            info(this, "No students are enrolled in this course.");
            return;
        }

        int saved = 0;
        try {
            for (int i = 0; i < model.getRowCount(); i++) {
                int studentId = Integer.parseInt((String) model.getValueAt(i, 0));
                String status = (String) model.getValueAt(i, STATUS_COL);
                attendanceData.save(new Attendance(studentId, s.getSessionId(), status, s.getSessionDate()));
                saved++;
            }
        } catch (DataAccessException ex) {
            info(this, "Failed to save attendance (" + saved + " of " + model.getRowCount()
                    + " rows were saved): " + ex.getMessage());
            onChanged.run();
            return;
        }
        info(this, "Attendance saved for " + saved + " students.");
        onChanged.run();
    }
}