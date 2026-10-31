package UI.Pages.lecturer;

import UI.Pages.Components.Card;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
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

    private final AvatarPanel avatar = new AvatarPanel();
    private final JLabel nameLbl  = new JLabel(" ");
    private final JLabel subLbl   = new JLabel(" ");
    private final JLabel regLbl   = new JLabel(" ");
    private final JLabel batchLbl = new JLabel(" ");
    private final JLabel emailLbl = new JLabel(" ");
    private final JLabel phoneLbl = new JLabel(" ");
    private final JLabel gpaLbl   = new JLabel(" ");
    private final JLabel attLbl   = new JLabel(" ");

    public Undergraduates() {
        super("Undergraduates", "Students in your modules", null);

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
    //  DATABASE  (replace the sample data with real queries)
    // =====================================================================

    /** Fills the "All my courses" drop-down. */
    private void loadCourses() {
        courseBox.removeAllItems();
        courseBox.addItem(ALL_COURSES);

        // TODO: NEED TO GET DATA FROM DATABASE HERE
        // Get the courses/modules taught by the logged-in lecturer, e.g.
        //   SELECT course_code, course_name FROM courses WHERE lecturer_id = ?
        // then add one item per row:
        //   courseBox.addItem(code + " - " + name);

        // ---- sample data (remove once the database is connected) ----
        courseBox.addItem("CS3012 - Database Management Systems");
        courseBox.addItem("CS3022 - Software Engineering Principles");
        courseBox.addItem("CS2042 - Data Structures & Algorithms");
    }

    /**
     * Loads the students enrolled in the lecturer's courses.
     * If "All my courses" is selected, load students from every course the lecturer teaches.
     */
    private void loadStudents(String courseLabel) {
        List<Student> list = new ArrayList<>();

        // TODO: NEED TO GET DATA FROM DATABASE HERE
        // Get the undergraduates enrolled in the selected course (or in all of the lecturer's
        // courses when ALL_COURSES is selected), e.g.
        //   SELECT DISTINCT s.reg_no, s.full_name, s.batch, s.email,
        //                   s.phone, s.current_gpa,
        //                   a.attendance_percentage
        //   FROM   enrollments e
        //   JOIN   students s ON s.reg_no = e.reg_no
        //   JOIN   courses  c ON c.course_code = e.course_code
        //   LEFT JOIN attendance_summary a ON a.reg_no = s.reg_no AND a.course_code = e.course_code
        //   WHERE  c.lecturer_id = ?
        //     AND  (? = 'ALL' OR e.course_code = ?)
        //   ORDER  BY s.reg_no
        // For each row: list.add(new Student(reg, name, batch, email, attendance, phone, gpa));

        // ---- sample data (remove once the database is connected) ----
        list.add(new Student("S/21/001", "Nimal Fernando",     "2021", "nimal@uni.lk",   94, "077 123 4567", 3.42));
        list.add(new Student("S/21/002", "Kavindi Silva",      "2021", "kavindi@uni.lk", 88, "071 234 5678", 3.10));
        list.add(new Student("S/22/014", "Ruwan Jayasinghe",   "2022", "ruwan@uni.lk",   71, "076 345 6789", 2.85));

        model.setRows(list);
        reloadBatchOptions();
        applyFilters();
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
        else showStudent(null);
    }

    /** Builds the batch drop-down from the loaded students (or query the batches table instead). */
    private void reloadBatchOptions() {
        // TODO: NEED TO GET DATA FROM DATABASE HERE (optional)
        // Batches can also come straight from the database, e.g.  SELECT DISTINCT batch FROM students ORDER BY batch

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

        int[] widths    = {110, 190, 70, 200, 110};
        int[] minWidths = { 90, 120, 64, 120, 104};   // keeps headers readable on narrow windows
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
            avatar.setInitials("");
            nameLbl.setText("No student selected");
            subLbl.setText(" ");
            for (JLabel l : new JLabel[]{regLbl, batchLbl, emailLbl, phoneLbl, gpaLbl, attLbl}) l.setText("–");
            return;
        }
        avatar.setInitials(initials(s.name));
        nameLbl.setText(s.name);
        subLbl.setText("Batch " + s.batch);
        regLbl.setText(s.reg);
        batchLbl.setText(s.batch);
        emailLbl.setText(s.email);
        phoneLbl.setText(s.phone);
        gpaLbl.setText(String.format("%.2f", s.gpa));
        attLbl.setText(s.attendance + "%");
        attLbl.setForeground(attendanceColour(s.attendance));
    }

    private static String initials(String name) {
        String[] p = name.trim().split("\\s+");
        String a = p[0].substring(0, 1);
        String b = p.length > 1 ? p[p.length - 1].substring(0, 1) : "";
        return (a + b).toUpperCase();
    }

    // =====================================================================
    //  DATA
    // =====================================================================

    static class Student {
        final String reg, name, batch, email, phone;
        final int attendance;
        final double gpa;
        Student(String reg, String name, String batch, String email, int attendance, String phone, double gpa) {
            this.reg = reg; this.name = name; this.batch = batch; this.email = email;
            this.attendance = attendance; this.phone = phone; this.gpa = gpa;
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

    /** Round avatar with the student's initials. */
    static class AvatarPanel extends JPanel {
        private String initials = "";
        AvatarPanel() {
            setOpaque(false);
            Dimension d = new Dimension(84, 84);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }
        void setInitials(String s) { initials = s; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, BRAND_LT, getWidth(), getHeight(), BRAND));
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Color.WHITE);
            g2.setFont(font(Font.BOLD, 28));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(initials)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initials, x, y);
            g2.dispose();
        }
    }
}
