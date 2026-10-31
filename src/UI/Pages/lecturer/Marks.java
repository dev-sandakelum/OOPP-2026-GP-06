package UI.Pages.lecturer;

import UI.Pages.Components.Card;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static UI.Kit.Palette.*;

/**
 * Lecturer Portal - Marks page (Java Swing).
 *
 * Columns: Reg. No | Student | Quiz 1 | Quiz 2 | Quiz 3 | Assignment 1 | Assignment 2
 *          | Mid-Term | Final | Total | Grade
 *
 * Scoring (total = 100):
 *   Quizzes      : 3 quizzes, each marked out of 10  -> scaled to 10 in the total
 *   Assignments  : 2 assignments, each out of 20     -> scaled to 20 in the total
 *   Mid-Term     : /20
 *   Final        : /50
 * Change the constants below if your weighting is different.
 */
public class Marks extends LecturerUI.Page {

    // ---------- assessment structure ----------
    static final int QUIZ_COUNT = 3;
    static final int ASG_COUNT  = 2;
    static final int QUIZ_MAX = 10, ASG_MAX = 20, MID_MAX = 20, FINAL_MAX = 50;
    static final int QUIZ_WEIGHT = 10, ASG_WEIGHT = 20;     // share of the 100 total

    // marks[] index -> 0..2 quizzes, 3..4 assignments, 5 mid, 6 final
    static final int[] MAX = {QUIZ_MAX, QUIZ_MAX, QUIZ_MAX, ASG_MAX, ASG_MAX, MID_MAX, FINAL_MAX};

    // table column index
    static final int C_REG = 0, C_NAME = 1, C_Q1 = 2, C_Q3 = 4, C_A1 = 5, C_A2 = 6,
            C_MID = 7, C_FINAL = 8, C_TOTAL = 9, C_GRADE = 10;
    static final String[] COLS = {"Reg. No", "Student", "Quiz 1 /10", "Quiz 2 /10", "Quiz 3 /10",
            "Assign. 1 /20", "Assign. 2 /20", "Mid-Term /20", "Final /50", "Total /100", "Grade"};

    // ---------- UI ----------
    private final JComboBox<String> courseBox = new JComboBox<>();
    private final JComboBox<String> assessBox = new JComboBox<>(
            new String[]{"All assessments", "Quiz", "Assignment", "Mid-Term", "Final"});
    private final JTextField searchField = new JTextField();
    private final JLabel avgLbl = new JLabel("0"), highLbl = new JLabel("0"),
            lowLbl = new JLabel("0"), passLbl = new JLabel("0%");
    private final JLabel footerCount = new JLabel(), footerUnsaved = new JLabel();

    private final MarksModel model = new MarksModel();
    private final JTable table = new JTable(model);
    private TableRowSorter<MarksModel> sorter;
    private List<TableColumn> allColumns;

    public Marks() {
        super("Marks", "Enter and review assessment marks", null);

        //  1. filters   2. summary cards   3. marks table
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(stretch(buildToolbar()));
        body.add(Box.createVerticalStrut(16));
        body.add(stretch(buildSummaryCards()));
        body.add(Box.createVerticalStrut(16));
        body.add(stretch(buildTable()));
        setScrollBody(body);

        // ===== load initial data =====
        loadCourses();
        if (courseBox.getItemCount() > 0) loadMarksForCourse((String) courseBox.getSelectedItem());

        courseBox.addActionListener(e -> {
            loadMarksForCourse((String) courseBox.getSelectedItem());
        });
        assessBox.addActionListener(e -> applyAssessmentFilter((String) assessBox.getSelectedItem()));
    }

    // =====================================================================
    //  DATABASE  (replace the sample data with real queries)
    // =====================================================================

    /** Fills the course drop-down. */
    private void loadCourses() {
        courseBox.removeAllItems();

        // TODO: NEED TO GET DATA FROM DATABASE HERE
        // Get the courses taught by the logged-in lecturer, e.g.
        //   SELECT course_code, course_name FROM courses WHERE lecturer_id = ?
        // then add one item per row:
        //   courseBox.addItem(code + " - " + name);

        // ---- sample data (remove once the database is connected) ----
        courseBox.addItem("CS3012 - Database Management Systems");
        courseBox.addItem("CS3022 - Software Engineering Principles");
        courseBox.addItem("CS2042 - Data Structures & Algorithms");
        courseBox.addItem("CS4013 - Machine Learning");
        courseBox.addItem("CS4091 - Final Year Research Project");
    }

    /** Loads every enrolled student with their 3 quiz + 2 assignment + mid + final marks. */
    private void loadMarksForCourse(String courseLabel) {
        List<StudentMarks> rows = new ArrayList<>();

        // TODO: NEED TO GET DATA FROM DATABASE HERE
        // Get all students enrolled in the selected course together with their marks, e.g.
        //   SELECT s.reg_no, s.full_name,
        //          m.quiz1, m.quiz2, m.quiz3,
        //          m.assignment1, m.assignment2,
        //          m.midterm, m.final_exam
        //   FROM   enrollments e
        //   JOIN   students s ON s.reg_no = e.reg_no
        //   LEFT JOIN marks m ON m.reg_no = e.reg_no AND m.course_code = e.course_code
        //   WHERE  e.course_code = ?
        //   ORDER  BY s.reg_no
        // For each row create a StudentMarks and fill marks[0..6] in this order:
        //   quiz1, quiz2, quiz3, assignment1, assignment2, midterm, final   (use null if not entered yet)

        // ---- sample data (remove once the database is connected) ----
        String[] names = {"Kavindu Perera", "Nethmi Fernando", "Sahan Jayawardena", "Ishara Wickramasinghe",
                "Tharushi Silva", "Dilshan Rajapaksha", "Chamodi Gunasekara", "Ravindu Senanayake",
                "Hansika Dissanayake", "Lakshan Bandara", "Piumi Herath", "Nadeesha Amarasinghe"};
        for (int i = 0; i < names.length; i++) {
            StudentMarks s = new StudentMarks("CS/2021/" + String.format("%03d", i + 11), names[i]);
            s.marks[0] = 4 + (i * 3) % 7;
            s.marks[1] = 5 + (i * 5) % 6;
            s.marks[2] = 3 + (i * 7) % 8;
            s.marks[3] = 9 + (i * 4) % 12;
            s.marks[4] = 8 + (i * 6) % 13;
            s.marks[5] = 6 + (i * 5) % 15;
            s.marks[6] = 18 + (i * 9) % 33;
            rows.add(s);
        }

        model.setRows(rows);
        refreshSummary();
        updateFooter();
    }

    /** Called by the "Save Marks" button. */
    private void saveMarksToDatabase() {
        // TODO: NEED TO SAVE DATA TO DATABASE HERE
        // Loop through model.getRows() and insert / update one row per student, e.g.
        //   INSERT INTO marks (reg_no, course_code, quiz1, quiz2, quiz3,
        //                      assignment1, assignment2, midterm, final_exam)
        //   VALUES (?,?,?,?,?,?,?,?,?)
        //   ON DUPLICATE KEY UPDATE quiz1=VALUES(quiz1), quiz2=VALUES(quiz2), quiz3=VALUES(quiz3),
        //                           assignment1=VALUES(assignment1), assignment2=VALUES(assignment2),
        //                           midterm=VALUES(midterm), final_exam=VALUES(final_exam)
        // Use PreparedStatement.setNull(index, Types.INTEGER) when a mark is null.
        // Wrap the loop in one transaction (conn.setAutoCommit(false) ... conn.commit()).

        model.clearUnsaved();
        updateFooter();
        JOptionPane.showMessageDialog(this, "Marks saved successfully.", "Marks", JOptionPane.INFORMATION_MESSAGE);
    }

    // =====================================================================
    //  UI BUILDERS
    // =====================================================================

    /** Card with Course / Assessment / Search, each with a small caption above it. */
    private JComponent buildToolbar() {
        styleInput(courseBox, 320);
        styleInput(assessBox, 180);

        searchField.setFont(font(Font.PLAIN, 13));
        searchField.setForeground(INK);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_2, 1, true), new EmptyBorder(0, 12, 0, 12)));
        searchField.setPreferredSize(new Dimension(220, 38));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name or reg. no"); // shown by some look-and-feels
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applySearch(); }
            public void removeUpdate(DocumentEvent e)  { applySearch(); }
            public void changedUpdate(DocumentEvent e) { applySearch(); }
        });

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.anchor = GridBagConstraints.SOUTHWEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.insets = new Insets(0, 0, 0, 12); row.add(labelled("Course", courseBox), g);
        g.gridx = 1;                                      row.add(labelled("Assessment", assessBox), g);
        g.gridx = 2; g.weightx = 1; g.insets = new Insets(0, 0, 0, 0);
        row.add(labelled("Search", searchField), g);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    /** Four summary cards, same style as the Overview KPIs. */
    private JComponent buildSummaryCards() {
        JPanel p = new JPanel(new GridLayout(1, 4, 16, 0));
        p.setOpaque(false);
        p.add(statCard(avgLbl,  "Class average", "Total out of 100", true));
        p.add(statCard(highLbl, "Highest",       "Best total",       false));
        p.add(statCard(lowLbl,  "Lowest",        "Lowest total",     false));
        p.add(statCard(passLbl, "Pass rate",     "Total of 40 or more", false));
        return p;
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

    /** Card with a title bar, the editable marks table and a footer. */
    private JComponent buildTable() {
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSurrendersFocusOnKeystroke(true);

        // header
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 48));
        header.setDefaultRenderer(new HeaderRenderer());

        // renderers
        table.getColumnModel().getColumn(C_REG).setCellRenderer(new CellRenderer(SwingConstants.LEFT, false, INK_2));
        table.getColumnModel().getColumn(C_NAME).setCellRenderer(new CellRenderer(SwingConstants.LEFT, true, INK));
        for (int c = C_Q1; c <= C_FINAL; c++) {
            table.getColumnModel().getColumn(c).setCellRenderer(new CellRenderer(SwingConstants.CENTER, false, INK));
            table.getColumnModel().getColumn(c).setCellEditor(new MarkEditor());
        }
        table.getColumnModel().getColumn(C_TOTAL).setCellRenderer(new TotalRenderer());
        table.getColumnModel().getColumn(C_GRADE).setCellRenderer(new GradeRenderer());

        // marks columns stay compact so Reg. No and Student keep their room
        // (min widths: on narrow windows the Student column shrinks first)
        TableColumnModel cols = table.getColumnModel();
        cols.getColumn(C_REG).setPreferredWidth(120);
        cols.getColumn(C_REG).setMinWidth(105);
        cols.getColumn(C_NAME).setPreferredWidth(220);
        cols.getColumn(C_NAME).setMinWidth(120);
        for (int c = C_Q1; c <= C_GRADE; c++) {
            cols.getColumn(c).setPreferredWidth(64);
            cols.getColumn(c).setMinWidth(60);
        }

        // sorter used for the search box
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // keep a copy of every column so the assessment filter can hide / show them
        table.setAutoCreateColumnsFromModel(false);
        allColumns = Collections.list(table.getColumnModel().getColumns());

        // recalculate the summary cards + footer whenever a mark changes
        model.addTableModelListener(e -> {
            refreshSummary();
            updateFooter();
        });

        // title bar
        JLabel title = new JLabel("Student marks");
        title.setFont(font(Font.BOLD, 15));
        title.setForeground(INK);
        JLabel hint = new JLabel("Click a mark to edit it");
        hint.setFont(font(Font.PLAIN, 12));
        hint.setForeground(INK_2);
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(new EmptyBorder(4, 12, 12, 12));
        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(hint, BorderLayout.EAST);

        // table sits directly in the card; the page itself scrolls
        JPanel grid = new JPanel(new BorderLayout());
        grid.setOpaque(false);
        grid.add(header, BorderLayout.NORTH);
        grid.add(table, BorderLayout.CENTER);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 8, 4, 8));   // keeps the header inside the rounded corners
        card.add(titleBar, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(buildFooter(), BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 12, 6, 12));
        footerCount.setFont(font(Font.PLAIN, 12));
        footerCount.setForeground(INK_2);
        footerUnsaved.setFont(font(Font.PLAIN, 12));
        footerUnsaved.setForeground(INK_2);
        p.add(footerCount, BorderLayout.WEST);
        p.add(footerUnsaved, BorderLayout.EAST);
        return p;
    }

    // =====================================================================
    //  BEHAVIOUR
    // =====================================================================

    private void applySearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q), C_REG, C_NAME));
        updateFooter();
    }

    /** Shows only the columns of the chosen assessment (Reg/Name/Total/Grade always stay). */
    private void applyAssessmentFilter(String sel) {
        TableColumnModel cm = table.getColumnModel();
        while (cm.getColumnCount() > 0) cm.removeColumn(cm.getColumn(0));
        for (TableColumn col : allColumns) {
            if (isVisible(col.getModelIndex(), sel)) cm.addColumn(col);
        }
    }

    private boolean isVisible(int mi, String sel) {
        if (mi == C_REG || mi == C_NAME || mi == C_TOTAL || mi == C_GRADE) return true;
        switch (sel) {
            case "Quiz":       return mi >= C_Q1 && mi <= C_Q3;
            case "Assignment": return mi == C_A1 || mi == C_A2;
            case "Mid-Term":   return mi == C_MID;
            case "Final":      return mi == C_FINAL;
            default:           return true;
        }
    }

    private void refreshSummary() {
        List<StudentMarks> rows = model.getRows();
        if (rows.isEmpty()) {
            avgLbl.setText("0"); highLbl.setText("0"); lowLbl.setText("0"); passLbl.setText("0%");
            return;
        }
        double sum = 0, hi = Double.NEGATIVE_INFINITY, lo = Double.MAX_VALUE;
        int pass = 0;
        for (StudentMarks s : rows) {
            double t = s.total();
            sum += t;
            hi = Math.max(hi, t);
            lo = Math.min(lo, t);
            if (t >= 40) pass++;
        }
        avgLbl.setText(String.format("%.1f", sum / rows.size()));
        highLbl.setText(fmt(hi));
        lowLbl.setText(fmt(lo));
        passLbl.setText(Math.round(pass * 100.0 / rows.size()) + "%");
    }

    private void updateFooter() {
        footerCount.setText("Showing " + table.getRowCount() + " of " + model.getRowCount() + " students");
        footerUnsaved.setText("Unsaved changes: " + model.getUnsaved());
    }

    // =====================================================================
    //  DATA MODEL
    // =====================================================================

    static class StudentMarks {
        final String regNo, name;
        final Integer[] marks = new Integer[7];     // q1,q2,q3,a1,a2,mid,final (null = not entered)

        StudentMarks(String regNo, String name) { this.regNo = regNo; this.name = name; }

        private int v(int i) { return marks[i] == null ? 0 : marks[i]; }

        double quizPart() {
            return (v(0) + v(1) + v(2)) / (double) (QUIZ_COUNT * QUIZ_MAX) * QUIZ_WEIGHT;
        }
        double assignmentPart() {
            return (v(3) + v(4)) / (double) (ASG_COUNT * ASG_MAX) * ASG_WEIGHT;
        }
        double total() {
            double t = quizPart() + assignmentPart() + v(5) + v(6);
            return Math.round(t * 10) / 10.0;
        }
    }

    static class MarksModel extends AbstractTableModel {
        private List<StudentMarks> rows = new ArrayList<>();
        private int unsaved = 0;

        void setRows(List<StudentMarks> r) { rows = r; unsaved = 0; fireTableDataChanged(); }
        List<StudentMarks> getRows()      { return rows; }
        int getUnsaved()                  { return unsaved; }
        void clearUnsaved()               { unsaved = 0; fireTableDataChanged(); }

        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        // only the 7 mark columns are editable; Total and Grade are read-only
        @Override public boolean isCellEditable(int r, int c) { return c >= C_Q1 && c <= C_FINAL; }

        @Override public Object getValueAt(int r, int c) {
            StudentMarks s = rows.get(r);
            if (c == C_REG)   return s.regNo;
            if (c == C_NAME)  return s.name;
            if (c == C_TOTAL) return s.total();
            if (c == C_GRADE) return s.total();            // renderer converts to a grade
            return s.marks[c - C_Q1];
        }

        @Override public void setValueAt(Object value, int r, int c) {
            if (!isCellEditable(r, c)) return;
            Integer newVal = null;
            String txt = value == null ? "" : value.toString().trim();
            if (!txt.isEmpty()) {
                try {
                    int n = (int) Math.round(Double.parseDouble(txt));
                    newVal = Math.max(0, Math.min(MAX[c - C_Q1], n));   // keep inside 0..max
                } catch (NumberFormatException ex) {
                    return;                                              // ignore invalid text
                }
            }
            StudentMarks s = rows.get(r);
            Integer old = s.marks[c - C_Q1];
            if ((old == null && newVal == null) || (old != null && old.equals(newVal))) return;
            s.marks[c - C_Q1] = newVal;
            unsaved++;
            fireTableRowsUpdated(r, r);                    // refreshes Total + Grade + triggers listener
        }
    }

    // =====================================================================
    //  RENDERERS / EDITORS
    // =====================================================================

    /** Text editor for mark cells (value is validated and clamped in MarksModel.setValueAt). */
    static class MarkEditor extends DefaultCellEditor {
        MarkEditor() {
            super(new JTextField());
            JTextField tf = (JTextField) getComponent();
            tf.setHorizontalAlignment(SwingConstants.CENTER);
            tf.setFont(font(Font.BOLD, 13));
            tf.setForeground(INK);
            tf.setBorder(new LineBorder(BRAND_LT, 2));
            setClickCountToStart(1);
        }
    }

    /** Column titles; mark columns show the max on a second line (e.g. "Quiz 1" / "/10"). */
    static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setOpaque(true);
            setBackground(SURFACE_2);
            setForeground(INK_2);
            setFont(font(Font.BOLD, 10));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            String text = v == null ? "" : v.toString().toUpperCase();
            int mi = t.convertColumnIndexToModel(c);
            boolean centred = mi >= C_Q1 && mi <= C_GRADE;
            int slash = text.lastIndexOf(" /");
            if (slash > 0) {
                text = "<html><div style='text-align:center; white-space:nowrap'>" + text.substring(0, slash)
                        + "<br><span style='font-weight:normal'>" + text.substring(slash + 1) + "</span></div></html>";
            }
            setText(text);
            setHorizontalAlignment(centred ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, LINE),
                    new EmptyBorder(0, centred ? 2 : 14, 0, 2)));
            return this;
        }
    }

    /** Plain cell: padding, colour and alignment per column; empty marks show a dash. */
    static class CellRenderer extends DefaultTableCellRenderer {
        private final boolean bold;
        private final Color colour;
        CellRenderer(int align, boolean bold, Color colour) {
            this.bold = bold;
            this.colour = colour;
            setHorizontalAlignment(align);
            setBorder(new EmptyBorder(0, align == SwingConstants.LEFT ? 14 : 4, 0, 4));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, false, r, c);
            setBorder(new EmptyBorder(0, getHorizontalAlignment() == SwingConstants.LEFT ? 14 : 4, 0, 4));
            setFont(font(bold ? Font.BOLD : Font.PLAIN, 13));
            setText(v == null ? "–" : v.toString());
            setForeground(v == null ? LINE_2 : colour);
            setBackground(s ? BRAND_SOFT : SURFACE);
            return this;
        }
    }

    static class TotalRenderer extends DefaultTableCellRenderer {
        TotalRenderer() { setHorizontalAlignment(SwingConstants.CENTER); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, false, r, c);
            setText(fmt((Double) v));
            setFont(font(Font.BOLD, 13));
            setForeground(INK);
            setBackground(s ? BRAND_SOFT : SURFACE);
            return this;
        }
    }

    /** Rounded green / red pill showing the letter grade. */
    static class GradeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel pill = new JLabel("", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        GradeRenderer() {
            super(new GridBagLayout());
            pill.setOpaque(false);
            pill.setFont(font(Font.BOLD, 12));
            pill.setBorder(new EmptyBorder(3, 12, 3, 12));
            add(pill);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            double total = (Double) v;
            Color col = total >= 40 ? OK : BAD;
            pill.setText(gradeOf(total)[0]);
            pill.setForeground(col);
            pill.setBackground(new Color(col.getRed(), col.getGreen(), col.getBlue(), 30));
            setBackground(sel ? BRAND_SOFT : SURFACE);
            return this;
        }
    }

    // =====================================================================
    //  SMALL HELPERS
    // =====================================================================

    static String[] gradeOf(double t) {
        double[] cut  = {85, 75, 70, 65, 60, 55, 50, 45, 40, 35, 30, 0};
        String[] name = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "E"};
        for (int i = 0; i < cut.length; i++) if (t >= cut[i]) return new String[]{name[i]};
        return new String[]{"E"};
    }

    static String fmt(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.format("%.1f", d);
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

    private static void styleInput(JComponent c, int width) {
        c.setFont(font(Font.PLAIN, 13));
        c.setForeground(INK);
        c.setBackground(SURFACE);
        c.setPreferredSize(new Dimension(width, 38));
    }

    /** Lets a section fill the full width inside the vertical BoxLayout. */
    private static JComponent stretch(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return c;
    }

    // =====================================================================
    //  RUN STANDALONE (to preview the page)
    // =====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Lecturer Portal - Marks");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new Marks());
            f.setSize(1250, 780);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
