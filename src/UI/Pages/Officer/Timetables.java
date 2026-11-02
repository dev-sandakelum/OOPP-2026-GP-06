package UI.Pages.Officer;

import Exception.DataAccessException;
import UI.Kit.Glass;
import UI.Pages.Components.Card;
import UI.Pages.Components.GlassDialog;
import UI.Pages.Components.Page;
import UI.Pages.Components.RoundedButton;
import data.DepartmentData;
import data.SemesterData;
import data.TechnicalOfficerData;
import data.TechnicalOfficerData.OfferingOption;
import model.Department;
import model.Semester;
import model.TechnicalOfficer;
import model.TimetableSlot;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

/**
 * Officer "Timetables" tab: weekly view of the published timetable.
 * Pick a semester and a department; the grid rows follow the real start/end times
 * in the database. Officers can change a slot (day, time, room, type), delete it,
 * or add a new one by clicking a free cell or the Add slot button.
 * Two courses in the same cell are shown together in amber.
 */
public class Timetables extends Page {

    private static final String[] WEEK = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter H_MM = DateTimeFormatter.ofPattern("H:mm");
    private static final String DOT = "  ·  ";

    private final TechnicalOfficerData officerData = new TechnicalOfficerData();

    private final JComboBox<Semester> semesterBox = new JComboBox<>();
    private final JComboBox<Department> departmentBox = new JComboBox<>();   // null item = all departments
    private final JPanel gridPanel = new JPanel(new GridBagLayout());

    private boolean loadingFilters = true;

    public Timetables(User user) {
        this(user, new RoundedButton("+ Add slot", true));
    }

    private Timetables(User user, RoundedButton add) {
        super("Timetables", "Click a slot to change it, or a free cell to add one", add);

        loadFilters(user);
        styleCombo(semesterBox, 260);
        styleCombo(departmentBox, 320);
        semesterBox.setRenderer(new LabelRenderer());
        departmentBox.setRenderer(new LabelRenderer());

        JPanel pickers = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pickers.setOpaque(false);
        pickers.add(label("Semester"));
        pickers.add(semesterBox);
        pickers.add(Box.createHorizontalStrut(12));
        pickers.add(label("Department"));
        pickers.add(departmentBox);

        add.addActionListener(e -> editSlot(null, null, null, null));

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        gridPanel.setOpaque(false);
        card.add(gridPanel, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(pickers, BorderLayout.NORTH);
        body.add(card, BorderLayout.CENTER);
        setScrollBody(body);

        loadingFilters = false;
        semesterBox.addActionListener(e -> reload());
        departmentBox.addActionListener(e -> reload());
        // pick up changes made elsewhere while this tab was hidden
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) reload();
        });
        reload();
    }

    // -----------------------------------------------------------------
    //  Filters
    // -----------------------------------------------------------------

    private void loadFilters(User user) {
        try {
            List<Semester> semesters = new ArrayList<>(new SemesterData().findAll());
            semesters.sort(Comparator.comparingInt(Semester::getSemester_id).reversed());
            Semester pick = null;
            for (Semester s : semesters) {
                semesterBox.addItem(s);
                if (pick == null && "ONGOING".equalsIgnoreCase(s.getStatus())) pick = s;
            }
            if (pick != null) semesterBox.setSelectedItem(pick);

            departmentBox.addItem(null);
            Department mine = null;
            int myDep = officerDepartment(user);
            for (Department d : new DepartmentData().findAll()) {
                departmentBox.addItem(d);
                if (d.getDep_id() == myDep) mine = d;
            }
            if (mine != null) departmentBox.setSelectedItem(mine);   // else stays on "All departments"
        } catch (DataAccessException ex) {
            info(this, "Could not load the timetable filters: " + ex.getMessage());
        }
    }

    private int officerDepartment(User user) {
        if (user == null) return 0;
        try {
            TechnicalOfficer to = officerData.findById(user.getUser_id());
            return to == null ? 0 : to.getDep_id();
        } catch (DataAccessException ex) {
            return 0;
        }
    }

    private static void styleCombo(JComboBox<?> box, int width) {
        box.setFont(font(Font.PLAIN, 14));
        box.setBackground(SURFACE);
        box.setForeground(INK);
        if (width > 0) box.setPreferredSize(new Dimension(width, 34));
    }

    private static class LabelRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean selected, boolean focus) {
            String text;
            if (value instanceof Semester s) {
                text = s.getAcademic_year() + DOT + "Semester " + s.getSemester_number()
                        + (s.getStatus() == null ? "" : "  (" + capitalize(s.getStatus()) + ")");
            } else if (value instanceof Department d) {
                text = d.getDep_name();
            } else {
                text = "All departments";
            }
            return super.getListCellRendererComponent(list, text, index, selected, focus);
        }
    }

    // -----------------------------------------------------------------
    //  Loading and grid rendering
    // -----------------------------------------------------------------

    private void reload() {
        if (loadingFilters) return;
        Semester sem = (Semester) semesterBox.getSelectedItem();
        Department dep = (Department) departmentBox.getSelectedItem();
        List<TimetableSlot> slots = new ArrayList<>();
        if (sem != null) {
            try {
                slots = officerData.getTimetableSlots(sem.getSemester_id(), dep == null ? 0 : dep.getDep_id());
            } catch (DataAccessException ex) {
                info(this, "Could not load the timetable: " + ex.getMessage());
            }
        }
        rebuild(slots);
    }

    private void rebuild(List<TimetableSlot> slots) {
        gridPanel.removeAll();

        if (slots.isEmpty()) {
            JLabel none = new JLabel("No timetable entries for this semester and department. Use Add slot to create one.",
                    SwingConstants.CENTER);
            none.setFont(font(Font.PLAIN, 14));
            none.setForeground(INK_2);
            none.setBorder(new EmptyBorder(40, 0, 40, 0));
            gridPanel.setLayout(new BorderLayout());
            gridPanel.add(none, BorderLayout.CENTER);
            gridPanel.revalidate();
            gridPanel.repaint();
            return;
        }
        gridPanel.setLayout(new GridBagLayout());

        // columns: Monday-Friday always, weekend only when something is scheduled there
        List<String> days = new ArrayList<>();
        for (int i = 0; i < WEEK.length; i++) {
            final String d = WEEK[i];
            if (i < 5 || slots.stream().anyMatch(s -> s.getDay().equals(d))) days.add(d);
        }

        // rows: every distinct start/end pair, earliest first
        TreeSet<String> rowKeys = new TreeSet<>();
        Map<String, List<TimetableSlot>> cells = new LinkedHashMap<>();
        for (TimetableSlot s : slots) {
            rowKeys.add(rowKey(s));
            cells.computeIfAbsent(s.getDay() + "|" + rowKey(s), k -> new ArrayList<>()).add(s);
        }

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(5, 5, 5, 5);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; gc.weighty = 0;
        gridPanel.add(Box.createHorizontalStrut(120), gc);
        for (int d = 0; d < days.size(); d++) {
            gc.gridx = d + 1; gc.gridy = 0; gc.weightx = 1;
            JLabel l = new JLabel(capitalize(days.get(d)), SwingConstants.CENTER);
            l.setFont(font(Font.BOLD, 13));
            l.setForeground(INK_2);
            l.setPreferredSize(new Dimension(110, 30));
            gridPanel.add(l, gc);
        }

        int row = 1;
        for (String key : rowKeys) {
            String[] times = key.split("-");
            gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
            JLabel l = new JLabel(times[0] + " - " + times[1]);
            l.setFont(font(Font.BOLD, 13));
            l.setForeground(INK_2);
            gridPanel.add(l, gc);

            for (int d = 0; d < days.size(); d++) {
                gc.gridx = d + 1; gc.weightx = 1;
                final String day = days.get(d);
                List<TimetableSlot> in = cells.getOrDefault(day + "|" + key, List.of());
                gridPanel.add(new SlotCell(in, () -> onCellClick(in, day, times[0], times[1])), gc);
            }
            row++;
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private static String rowKey(TimetableSlot s) {
        return s.getStart().format(HM) + "-" + s.getEnd().format(HM);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    // -----------------------------------------------------------------
    //  Changing the schedule
    // -----------------------------------------------------------------

    private void onCellClick(List<TimetableSlot> in, String day, String start, String end) {
        if (in.isEmpty()) {
            editSlot(null, day, start, end);          // free cell: add a slot here
        } else if (in.size() == 1) {
            editSlot(in.get(0), null, null, null);
        } else {
            JComboBox<String> pick = new JComboBox<>();
            for (TimetableSlot s : in) pick.addItem(s.getCourseCode() + "  " + s.getCourseName());
            styleCombo(pick, 0);
            if (!ask(this, "Which course do you want to change?", pick)) return;
            editSlot(in.get(pick.getSelectedIndex()), null, null, null);
        }
    }

    /** Add (existing == null) or change a slot. The presets only matter when adding. */
    private void editSlot(TimetableSlot existing, String presetDay, String presetStart, String presetEnd) {
        Semester sem = (Semester) semesterBox.getSelectedItem();
        if (sem == null) {
            info(this, "Pick a semester first.");
            return;
        }
        boolean adding = existing == null;

        JComboBox<OfferingOption> courseBox = new JComboBox<>();
        styleCombo(courseBox, 0);
        if (adding) {
            Department dep = (Department) departmentBox.getSelectedItem();
            try {
                for (OfferingOption o : officerData.getOfferingOptions(sem.getSemester_id(), dep == null ? 0 : dep.getDep_id())) {
                    courseBox.addItem(o);
                }
            } catch (DataAccessException ex) {
                info(this, "Could not load the courses: " + ex.getMessage());
                return;
            }
            if (courseBox.getItemCount() == 0) {
                info(this, "No courses are offered in this semester for the selected department.");
                return;
            }
        }

        String[] dayNames = new String[WEEK.length];
        for (int i = 0; i < WEEK.length; i++) dayNames[i] = capitalize(WEEK[i]);
        JComboBox<String> dayBox = new JComboBox<>(dayNames);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Lecture", "Practical"});
        styleCombo(dayBox, 0);
        styleCombo(typeBox, 0);
        JTextField startField = field(10);
        JTextField endField = field(10);
        JTextField hallField = field(22);

        if (adding) {
            if (presetDay != null) dayBox.setSelectedItem(capitalize(presetDay));
            if (presetStart != null) startField.setText(presetStart);
            if (presetEnd != null) endField.setText(presetEnd);
        } else {
            dayBox.setSelectedItem(capitalize(existing.getDay()));
            String type = capitalize(existing.getType());
            if (!type.isEmpty()) {
                boolean known = false;
                for (int i = 0; i < typeBox.getItemCount(); i++) known |= typeBox.getItemAt(i).equals(type);
                if (!known) typeBox.addItem(type);
                typeBox.setSelectedItem(type);
            }
            startField.setText(existing.getStart().format(HM));
            endField.setText(existing.getEnd().format(HM));
            hallField.setText(existing.getHall());
        }

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (adding) {
            addRow(p, "Course", courseBox);
        } else {
            JLabel course = new JLabel(existing.getCourseCode() + "  " + existing.getCourseName());
            course.setFont(font(Font.BOLD, 15));
            course.setForeground(BRAND);
            addRow(p, "Course", course);
        }
        addRow(p, "Day", dayBox);
        addRow(p, "Start time (HH:mm)", startField);
        addRow(p, "End time (HH:mm)", endField);
        addRow(p, "Room / lecture hall", hallField);
        addRow(p, "Type", typeBox);

        Object[] options = adding
                ? new Object[]{"Add slot", "Cancel"}
                : new Object[]{"Save", "Delete slot", "Cancel"};
        String title = adding ? "Add timetable slot" : "Change timetable slot";

        while (true) {
            JOptionPane pane = new JOptionPane(p, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
                    null, options, options[0]);
            Object choice = GlassDialog.show(this, title, pane);

            if (choice == null || "Cancel".equals(choice)) return;

            if ("Delete slot".equals(choice)) {
                if (confirm(this, "Remove " + existing.getCourseCode() + " on " + capitalize(existing.getDay())
                        + " at " + existing.getStart().format(HM) + " - " + existing.getEnd().format(HM) + "?")) {
                    try {
                        officerData.deleteTimetableSlot(existing.getId());
                    } catch (DataAccessException ex) {
                        info(this, "Could not delete the slot: " + ex.getMessage());
                        return;
                    }
                    reload();
                }
                return;
            }

            LocalTime start = parseTime(startField.getText());
            LocalTime end = parseTime(endField.getText());
            String hall = hallField.getText().trim();
            String problem = null;
            if (start == null || end == null) problem = "Enter the times as HH:mm, for example 08:00.";
            else if (!end.isAfter(start)) problem = "The end time must be after the start time.";
            else if (hall.isEmpty()) problem = "Enter the room or lecture hall.";
            if (problem != null) {
                info(this, problem);
                continue;                                  // reopen with the values still filled in
            }

            String day = ((String) dayBox.getSelectedItem()).toUpperCase();
            String type = ((String) typeBox.getSelectedItem()).toUpperCase();
            Time sqlStart = Time.valueOf(start);
            Time sqlEnd = Time.valueOf(end);

            try {
                List<String> busy = officerData.findRoomConflicts(sem.getSemester_id(), day, hall,
                        sqlStart, sqlEnd, adding ? 0 : existing.getId());
                if (!busy.isEmpty() && !confirm(this, hall + " is already booked on " + capitalize(day)
                        + " at that time by " + String.join(", ", busy) + ".\nSave anyway?")) {
                    continue;
                }
                if (adding) {
                    OfferingOption course = (OfferingOption) courseBox.getSelectedItem();
                    officerData.addTimetableSlot(sem.getSemester_id(), course.offeringId(), day, sqlStart, sqlEnd, hall, type);
                } else {
                    officerData.updateTimetableSlot(existing.getId(), day, sqlStart, sqlEnd, hall, type);
                }
            } catch (DataAccessException ex) {
                info(this, "Could not save the slot: " + ex.getMessage());
                return;
            }
            reload();
            return;
        }
    }

    private static void addRow(JPanel p, String text, JComponent field) {
        JLabel l = label(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        p.add(Box.createVerticalStrut(10));
    }

    private static LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text.trim(), H_MM);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    // -----------------------------------------------------------------
    //  One cell of the grid: free, one course, or several courses
    // -----------------------------------------------------------------

    private static class SlotCell extends JButton {
        private static final int LINE_H = 38;

        private final List<TimetableSlot> slots;

        SlotCell(List<TimetableSlot> slots, Runnable onClick) {
            this.slots = slots;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setPreferredSize(new Dimension(110, Math.max(76, slots.size() * LINE_H + 16)));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> onClick.run());

            if (slots.isEmpty()) setToolTipText("Free. Click to add a slot");
            else if (slots.size() > 1) setToolTipText(slots.size() + " courses share this slot. Click to change one");
            else setToolTipText(slots.get(0).getCourseName() + ". Click to change");
        }

        @Override
        protected void paintComponent(Graphics g) {
            boolean empty = slots.isEmpty();
            boolean multi = slots.size() > 1;
            boolean hover = getModel().isRollover();

            Graphics2D g2 = Glass.aa(g);
            Shape shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 18, 18);

            if (empty) {
                g2.setColor(hover ? GLASS : GLASS_FAINT);
                g2.fill(shape);
                g2.setColor(LINE_2);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
                g2.draw(shape);
                if (hover) {
                    g2.setFont(font(Font.BOLD, 20));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.setColor(INK_2);
                    g2.drawString("+", (getWidth() - fm.stringWidth("+")) / 2, (getHeight() + fm.getAscent()) / 2 - 3);
                }
                g2.dispose();
                return;
            }

            if (multi) {
                g2.setColor(Glass.alpha(WARN, hover ? 70 : 45));
                g2.fill(shape);
                g2.setColor(WARN);
                g2.draw(shape);
            } else {
                g2.setColor(hover ? Glass.alpha(BRAND_LT, 80) : BRAND_SOFT);
                g2.fill(shape);
                g2.setColor(GLASS_LINE);
                g2.draw(shape);
            }

            int y = (getHeight() - slots.size() * LINE_H) / 2;
            for (TimetableSlot s : slots) {
                g2.setFont(font(Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(multi ? WARN.darker() : BRAND);
                g2.drawString(s.getCourseCode(), (getWidth() - fm.stringWidth(s.getCourseCode())) / 2, y + 16);

                g2.setFont(font(Font.PLAIN, 12));
                fm = g2.getFontMetrics();
                g2.setColor(INK_2);
                String kind = s.getType().isEmpty() ? "" : DOT + s.getType().substring(0, 1);
                String sub = fit(s.getHall() + kind, fm, getWidth() - 12);
                g2.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, y + 32);
                y += LINE_H;
            }
            g2.dispose();
        }

        private String fit(String text, FontMetrics fm, int max) {
            if (fm.stringWidth(text) <= max) return text;
            while (text.length() > 1 && fm.stringWidth(text + "...") > max) text = text.substring(0, text.length() - 1);
            return text + "...";
        }
    }
}
