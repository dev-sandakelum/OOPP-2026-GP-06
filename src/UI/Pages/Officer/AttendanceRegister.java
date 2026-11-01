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
        if (coursePicker.getSelectedIndex() < 0) {
            info(this, "Select a course first.");
            return;
        }
        String date = dateField.getText().trim();
        Date sessionDate;
        try {
            sessionDate = Date.valueOf(date);
        } catch (IllegalArgumentException ex) {
            info(this, "Enter the session date as yyyy-MM-dd (for example 2026-10-22).");
            return;
        }

        int offering = offeringId();
        try {
            int next = 1;
            for (Session s : sessionData.findByOffering(offering)) {
                next = Math.max(next, s.getSessionNumber() + 1);
            }

            String[] labels = {"Session number", "Type (LECTURE / PRACTICAL)",
                    "Start time (HH:mm)", "End time (HH:mm)", "Room"};
            String[] v = form(this, "New session on " + date, labels,
                    new String[]{String.valueOf(next), "LECTURE", "08:00", "10:00", ""});
            if (v == null) return;

            Session s = new Session();
            s.setOfferingId(offering);
            s.setSessionNumber(Integer.parseInt(v[0]));
            s.setSessionType(v[1].isEmpty() ? null : v[1].toUpperCase());
            s.setSessionDate(sessionDate);
            s.setStartTime(v[2].isEmpty() ? null : Time.valueOf(v[2] + ":00"));
            s.setEndTime(v[3].isEmpty() ? null : Time.valueOf(v[3] + ":00"));
            s.setRoom(v[4].isEmpty() ? null : v[4]);

            Session saved = sessionData.insert(s);
            loadSessions();
            for (int i = 0; i < sessions.size(); i++) {
                if (sessions.get(i).getSessionId() == saved.getSessionId()) {
                    sessionPicker.setSelectedIndex(i);
                }
            }
            onChanged.run();
        } catch (IllegalArgumentException ex) {
            info(this, "Check the session number and the times (HH:mm).");
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