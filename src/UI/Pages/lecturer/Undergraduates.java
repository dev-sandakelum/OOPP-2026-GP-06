package UI.Pages.lecturer;

import UI.Pages.Components.Avatar;
import UI.Pages.Components.AvatarCellRenderer;
import UI.Pages.Components.Card;
import data.AttendanceData;
import data.BatchData;
import data.CourseData;
import data.CourseOfferingData;
import data.EnrollmentData;
import data.ResultData;
import data.UndergraduateData;
import data.UserData;
import model.Attendance;
import model.Batch;
import model.Course;
import model.CourseOffering;
import model.Enrollment;
import model.Result;
import model.Undergraduate;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static UI.Kit.Palette.*;

public class Undergraduates extends LecturerUI.Page {

    private static final String ALL_BATCHES = "All batches";
    private static final String ALL_COURSES = "All my courses";

    // table columns
    private static final String[] COLS = {"Reg. no", "Name", "Batch", "Email", "Attendance"};
    private static final int ATT_COL = 4;

    // ---------- UI ----------
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> batchBox  = new JComboBox<>();
    private final JComboBox<String> courseBox = new JComboBox<>();

    private final StudentModel model = new StudentModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<StudentModel> sorter = new TableRowSorter<>(model);
    private final JLabel countLbl = new JLabel(" ");

    private final Avatar avatar = new Avatar("", 84);
    private final JLabel nameLbl  = new JLabel(" ");
    private final JLabel subLbl   = new JLabel(" ");
    private final JLabel regLbl   = new JLabel(" ");
    private final JLabel batchLbl = new JLabel(" ");
    private final JLabel emailLbl = new JLabel(" ");
    private final JLabel phoneLbl = new JLabel(" ");
    private final JLabel gpaLbl   = new JLabel(" ");
    private final JLabel attLbl   = new JLabel(" ");

    // ---------- Data layers ----------
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final UserData userData = new UserData();
    private final UndergraduateData undergraduateData = new UndergraduateData();
    private final BatchData batchData = new BatchData();
    private final AttendanceData attendanceData = new AttendanceData();
    private final ResultData resultData = new ResultData();
    private final int lecturerId;
    private List<CourseOffering> offerings = new ArrayList<>();

    public Undergraduates(User user) {
        super("Undergraduates", "Students in your modules", null);
        this.lecturerId = user == null ? 0 : user.getUser_id();

        //  1. filters   2. student list + selected student details
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(buildToolbar(), BorderLayout.NORTH);
        content.add(buildBody(), BorderLayout.CENTER);
        setScrollBody(content);

        // ===== load data =====
        loadCourses();
        loadStudents((String) courseBox.getSelectedItem());

        courseBox.addActionListener(e -> loadStudents((String) courseBox.getSelectedItem()));
        batchBox.addActionListener(e -> applyFilters());
    }

    // =====================================================================
    //  DATABASE  (loaded through the data layers)
    // =====================================================================

    /** Fills the "All my courses" drop-down. */
    private void loadCourses() {
        courseBox.removeAllItems();
        courseBox.addItem(ALL_COURSES);

        offerings = offeringData.findByLecturer(lecturerId);
        for (CourseOffering o : offerings) {
            Course c = courseData.findById(o.getCourse_id());
            courseBox.addItem(c == null ? "Offering " + o.getOffering_id()
                    : c.getCourse_code() + " - " + c.getCourse_name());
        }
    }

    /**
     * Loads the students enrolled in the lecturer's courses.
     * If "All my courses" is selected, load students from every course the lecturer teaches.
     */
    private void loadStudents(String courseLabel) {
        List<Student> list = new ArrayList<>();

        // index 0 is "All my courses", the others follow the offerings list
        int index = courseBox.getSelectedIndex();
        List<CourseOffering> selected = (index <= 0 || index > offerings.size())
                ? offerings : List.of(offerings.get(index - 1));

        // every student enrolled in the selected course(s), each one only once
        Set<Integer> studentIds = new LinkedHashSet<>();
        for (CourseOffering o : selected) {
            for (Enrollment en : enrollmentData.findByOffering(o.getOffering_id())) {
                studentIds.add(en.getStudent_id());
            }
        }

        for (int id : studentIds) {
            User u = userData.findById(id);
            if (u == null) continue;
            list.add(new Student(String.valueOf(id), u.getName(), batchName(id), u.getEmail(),
                    attendancePercent(id, selected),
                    u.getContact_number() == null ? "" : u.getContact_number(), latestGpa(id),
                    u.getPhoto()));
        }

        model.setRows(list);
        reloadBatchOptions();
        applyFilters();
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
        else showStudent(null);
    }

    /** Batch name of a student ("" if the student has no batch). */
    private String batchName(int studentId) {
        Undergraduate ug = undergraduateData.findById(studentId);
        if (ug == null) return "";
        Batch b = batchData.findById(ug.getBatch_id());
        return b == null ? "" : b.getBatch_name();
    }

    /** Percentage of sessions the student attended in the given courses. */
    private int attendancePercent(int studentId, List<CourseOffering> courses) {
        int total = 0, present = 0;
        for (CourseOffering o : courses) {
            for (Attendance a : attendanceData.findByStudentAndOffering(studentId, o.getOffering_id())) {
                total++;
                if ("PRESENT".equalsIgnoreCase(a.getAttendStatus())) present++;
            }
        }
        return total == 0 ? 0 : Math.round(present * 100f / total);
    }

    /** Latest CGPA from the student's results (null if there is none yet). */
    private Double latestGpa(int studentId) {
        Result latest = null;
        for (Result r : resultData.findByStudent(studentId)) {
            if (r.getCgpa() == null) continue;
            if (latest == null || (r.getPublishedDate() != null && latest.getPublishedDate() != null
                    && r.getPublishedDate().after(latest.getPublishedDate()))) {
                latest = r;
            }
        }
        return latest == null ? null : latest.getCgpa().doubleValue();
    }

    /** Builds the batch drop-down from the loaded students. */
    private void reloadBatchOptions() {
        ActionListener[] ls = batchBox.getActionListeners();
        for (ActionListener l : ls) batchBox.removeActionListener(l);   // avoid refiltering while rebuilding
        batchBox.removeAllItems();
        batchBox.addItem(ALL_BATCHES);
        List<String> seen = new ArrayList<>();
        for (Student s : model.rows) if (!seen.contains(s.batch)) seen.add(s.batch);
        java.util.Collections.sort(seen);
        for (String b : seen) batchBox.addItem(b);
        for (ActionListener l : ls) batchBox.addActionListener(l);
    }

    // =====================================================================
    //  UI BUILDERS
    // =====================================================================

    /** Card with Search / Batch / Course, each with a small caption above it. */
    private JComponent buildToolbar() {
        searchField.setFont(font(Font.PLAIN, 13));
        searchField.setForeground(INK);
        searchField.setPreferredSize(new Dimension(260, 38));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_2, 1, true), new EmptyBorder(0, 12, 0, 12)));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name or registration no");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyFilters(); }
            public void removeUpdate(DocumentEvent e)  { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        for (JComboBox<String> b : java.util.List.of(batchBox, courseBox)) {
            b.setFont(font(Font.PLAIN, 13));
            b.setBackground(SURFACE);
            b.setForeground(INK);
            b.setPreferredSize(new Dimension(b == courseBox ? 300 : 150, 38));
        }

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.anchor = GridBagConstraints.SOUTHWEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.weightx = 1; g.insets = new Insets(0, 0, 0, 12); row.add(labelled("Search", searchField), g);
        g.gridx = 1; g.weightx = 0;                                      row.add(labelled("Batch", batchBox), g);
        g.gridx = 2; g.insets = new Insets(0, 0, 0, 0);                  row.add(labelled("Course", courseBox), g);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    /** Student list on the left, details of the selected student on the right (same height). */
    private JComponent buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.NORTH;
        g.gridy = 0;

        g.gridx = 0; g.weightx = 1; g.insets = new Insets(0, 0, 0, 16);
        body.add(buildTableCard(), g);
        g.gridx = 1; g.weightx = 0; g.insets = new Insets(0, 0, 0, 0);
        body.add(buildDetailCard(), g);
        return body;
    }

    private JComponent buildTableCard() {
        table.setRowHeight(44);
        table.setFont(font(Font.PLAIN, 13));
        table.setForeground(INK);
        table.setBackground(SURFACE);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(LINE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(BRAND_SOFT);
        table.setSelectionForeground(INK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);
        table.setDefaultEditor(Object.class, null);       // read-only
        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 40));
        h.setDefaultRenderer(new HeaderRenderer());

        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getColumnModel().getColumn(ATT_COL).setCellRenderer(new AttendanceRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(
                new AvatarCellRenderer(r -> r < model.rows.size() ? model.rows.get(r).photo : null, SURFACE));

        int[] widths    = {110, 230, 70, 200, 110};
        int[] minWidths = { 90, 160, 64, 120, 104};   // keeps headers readable on narrow windows
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int vr = table.getSelectedRow();
            showStudent(vr < 0 ? null : model.rows.get(table.convertRowIndexToModel(vr)));
        });

        // title bar
        JLabel title = new JLabel("Students");
        title.setFont(font(Font.BOLD, 15));
        title.setForeground(INK);
        countLbl.setFont(font(Font.PLAIN, 12));
        countLbl.setForeground(INK_2);
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(new EmptyBorder(4, 12, 12, 12));
        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(countLbl, BorderLayout.EAST);

        // table sits directly in the card; the page itself scrolls
        JPanel grid = new JPanel(new BorderLayout());
        grid.setOpaque(false);
        grid.add(h, BorderLayout.NORTH);
        grid.add(table, BorderLayout.CENTER);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.add(titleBar, BorderLayout.NORTH);
        inner.add(grid, BorderLayout.CENTER);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 8, 12, 8));   // keeps the header inside the rounded corners
        card.add(inner, BorderLayout.NORTH);
        return card;
    }

    private JComponent buildDetailCard() {
        Card card = new Card(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel heading = new JLabel("Student details");
        heading.setFont(font(Font.BOLD, 15));
        heading.setForeground(INK);

        nameLbl.setFont(font(Font.BOLD, 16));
        nameLbl.setForeground(INK);
        subLbl.setFont(font(Font.PLAIN, 12));
        subLbl.setForeground(INK_2);

        JPanel hero = new JPanel();
        hero.setOpaque(false);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{avatar, nameLbl, subLbl}) c.setAlignmentX(CENTER_ALIGNMENT);
        hero.add(avatar);
        hero.add(Box.createVerticalStrut(12));
        hero.add(nameLbl);
        hero.add(Box.createVerticalStrut(2));
        hero.add(subLbl);

        for (JComponent c : new JComponent[]{heading, hero}) c.setAlignmentX(LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, hero.getPreferredSize().height));

        card.add(heading);
        card.add(Box.createVerticalStrut(18));
        card.add(hero);
        card.add(Box.createVerticalStrut(18));
        card.add(detailRow("Reg. no", regLbl, false));
        card.add(detailRow("Batch", batchLbl, false));
        card.add(detailRow("Email", emailLbl, false));
        card.add(detailRow("Phone", phoneLbl, false));
        card.add(detailRow("Current GPA", gpaLbl, false));
        card.add(detailRow("Attendance", attLbl, true));
        card.add(Box.createVerticalGlue());
        card.setPreferredSize(new Dimension(300, card.getPreferredSize().height));   // fixed width
        card.setMinimumSize(card.getPreferredSize());
        return card;
    }

    /** One "label ........ value" line in the details card. */
    private JComponent detailRow(String title, JLabel value, boolean last) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        EmptyBorder pad = new EmptyBorder(11, 0, 11, 0);
        row.setBorder(last ? pad : new CompoundBorder(new MatteBorder(0, 0, 1, 0, LINE), pad));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(font(Font.PLAIN, 13));
        t.setForeground(INK_2);
        value.setFont(font(Font.BOLD, 13));
        value.setForeground(INK);
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(t, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Small caption above an input. */
    private static JComponent labelled(String caption, JComponent input) {
        JLabel l = new JLabel(caption);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(INK_2);

        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(l, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    /** Green from 80%, amber from 60%, red below. */
    private static Color attendanceColour(int pct) {
        return pct >= 80 ? OK : pct >= 60 ? WARN : BAD;
    }

    // =====================================================================
    //  BEHAVIOUR
    // =====================================================================

    /** Search box + batch drop-down (the course drop-down re-queries the database instead). */
    private void applyFilters() {
        List<RowFilter<StudentModel, Integer>> fs = new ArrayList<>();

        String q = searchField.getText().trim();
        if (!q.isEmpty()) {
            fs.add(RowFilter.regexFilter("(?i)" + Pattern.quote(q), 0, 1));     // reg. no or name
        }
        String batch = (String) batchBox.getSelectedItem();
        if (batch != null && !batch.equals(ALL_BATCHES)) {
            fs.add(RowFilter.regexFilter("^" + Pattern.quote(batch) + "$", 2));
        }
        sorter.setRowFilter(fs.isEmpty() ? null : RowFilter.andFilter(fs));
        countLbl.setText("Showing " + table.getRowCount() + " of " + model.getRowCount() + " students");

        if (table.getRowCount() > 0 && table.getSelectedRow() < 0) table.setRowSelectionInterval(0, 0);
        else if (table.getRowCount() == 0) showStudent(null);
    }

    private void showStudent(Student s) {
        if (s == null) {
            avatar.set("", null);
            nameLbl.setText("No student selected");
            subLbl.setText(" ");
            for (JLabel l : new JLabel[]{regLbl, batchLbl, emailLbl, phoneLbl, gpaLbl, attLbl}) l.setText("–");
            return;
        }
        avatar.set(s.name, s.photo);
        nameLbl.setText(s.name);
        subLbl.setText("Batch " + s.batch);
        regLbl.setText(s.reg);
        batchLbl.setText(s.batch);
        emailLbl.setText(s.email);
        phoneLbl.setText(s.phone);
        gpaLbl.setText(s.gpa == null ? "–" : String.format("%.2f", s.gpa));
        attLbl.setText(s.attendance + "%");
        attLbl.setForeground(attendanceColour(s.attendance));
    }

    // =====================================================================
    //  DATA
    // =====================================================================

    static class Student {
        final String reg, name, batch, email, phone, photo;
        final int attendance;
        final Double gpa;
        Student(String reg, String name, String batch, String email, int attendance, String phone, Double gpa,
                String photo) {
            this.reg = reg; this.name = name; this.batch = batch; this.email = email;
            this.attendance = attendance; this.phone = phone; this.gpa = gpa; this.photo = photo;
        }
    }

    static class StudentModel extends AbstractTableModel {
        List<Student> rows = new ArrayList<>();
        void setRows(List<Student> r) { rows = r; fireTableDataChanged(); }
        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Object getValueAt(int r, int c) {
            Student s = rows.get(r);
            switch (c) {
                case 0:  return s.reg;
                case 1:  return s.name;
                case 2:  return s.batch;
                case 3:  return s.email;
                default: return s.attendance + "%";
            }
        }
    }

    // =====================================================================
    //  CUSTOM COMPONENTS
    // =====================================================================

    static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setOpaque(true);
            setBackground(SURFACE_2);
            setForeground(INK_2);
            setFont(font(Font.BOLD, 11));
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(0, 14, 0, 6)));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(v == null ? "" : v.toString().toUpperCase());
            return this;
        }
    }

    /** Plain cell: Name in bold, other columns muted. */
    static class CellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, false, r, c);
            boolean name = t.convertColumnIndexToModel(c) == 1;
            setFont(font(name ? Font.BOLD : Font.PLAIN, 13));
            setForeground(name ? INK : INK_2);
            setBackground(s ? BRAND_SOFT : SURFACE);
            setBorder(new EmptyBorder(0, 14, 0, 6));
            return this;
        }
    }

    /** Attendance % as a coloured chip. */
    static class AttendanceRenderer extends JLabel implements TableCellRenderer {
        private Color colour = INK_2, background = SURFACE;
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            String text = String.valueOf(v);
            setText(text);
            int pct;
            try { pct = Integer.parseInt(text.replace("%", "").trim()); } catch (NumberFormatException e) { pct = 0; }
            colour = attendanceColour(pct);
            background = s ? BRAND_SOFT : SURFACE;
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(background);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(font(Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int w = fm.stringWidth(text) + 20, h = 24, x = 14, y = (getHeight() - h) / 2;
            g2.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 30));
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, h, h));
            g2.setColor(colour);
            g2.drawString(text, x + 10, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
