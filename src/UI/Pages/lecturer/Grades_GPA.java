package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.SimpleDocListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.field;
import static UI.Pages.Components.Ui.label;
import static UI.Pages.Components.Ui.table;

/**
 * Lecturer - Grades & GPA page
 * - Lecturer selects a course and sees every student's marks, grade and grade point for that course.
 * - Lecturer can search students by name or student ID.
 *
 * Every place marked "NEED TO ADD DATA FROM DATABASE" must be connected to MySQL.
 * The sample rows are only dummy data so the screen is not empty.
 */
public class Grades_GPA extends LecturerUI.Page {

    // grade scale (same grades as the Marks page) and the grade point of each grade
    private static final String[] GRADES = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "E"};
    private static final double[] POINTS = {4.0, 4.0, 3.7, 3.3, 3.0, 2.7, 2.3, 2.0, 1.7, 1.3, 1.0, 0.0};

    // chip color of every grade: A = green, B = light green, C = amber, D/E = red
    private static final Map<String, Color> GRADE_COLORS = new HashMap<>();
    static {
        for (String g : GRADES) {
            if (g.startsWith("A"))      GRADE_COLORS.put(g, OK);
            else if (g.startsWith("B")) GRADE_COLORS.put(g, BRAND_LT);
            else if (g.startsWith("C")) GRADE_COLORS.put(g, WARN);
            else                        GRADE_COLORS.put(g, BAD);
        }
    }
    private static final int GRADE_COL = 5;

    JComboBox<String> courseBox = new JComboBox<>();
    JTextField searchField = field(20);
    JLabel footer = new JLabel(" ");

    DefaultTableModel model = new ReadOnlyTableModel(
            new String[]{"Student ID", "Student Name", "Marks Obtained", "Total Marks", "Percentage", "Grade", "Grade Point"});
    JTable table = table(model, GRADE_COLORS, GRADE_COL);   // shared flat table, grade shown as a chip

    // The sorter is used to hide rows that do not match the search text
    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    public Grades_GPA() {
        super("Grades & GPA", "Grades and GPA for your modules", null);

        table.setRowSorter(sorter);
        table.setFocusable(false);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, LINE));

        // ----- Main body: toolbar on top, table card below -----
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildToolbar(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        setScrollBody(body);

        // When the course changes, load the marks of that course
        courseBox.addActionListener(e -> loadMarks());

        // When the lecturer types in the search box, filter the table
        searchField.getDocument().addDocumentListener(new SimpleDocListener(this::search));

        loadCourses();
    }

    // =====================================================================
    // UI
    // =====================================================================

    // Course selector on the left, search box filling the rest
    private JComponent buildToolbar() {
        courseBox.setFont(font(Font.PLAIN, 13));
        courseBox.setForeground(INK);
        courseBox.setBackground(SURFACE);
        courseBox.setPreferredSize(new Dimension(300, 38));

        searchField.setFont(font(Font.PLAIN, 13));
        searchField.setToolTipText("Search by student name or ID");

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(label("COURSE"));
        left.add(courseBox);
        left.add(Box.createHorizontalStrut(8));
        left.add(label("SEARCH"));

        JPanel bar = new JPanel(new BorderLayout(0, 0));
        bar.setOpaque(false);
        bar.add(left, BorderLayout.WEST);
        bar.add(searchField, BorderLayout.CENTER);
        return bar;
    }

    // Table inside a white rounded card, with the student count at the bottom
    private JComponent buildTableCard() {
        footer.setFont(font(Font.PLAIN, 12));
        footer.setForeground(INK_2);
        footer.setBorder(new EmptyBorder(10, 16, 6, 16));

        // header + rows without a scroll pane, so the card grows with the rows
        JPanel grid = new JPanel(new BorderLayout());
        grid.setOpaque(false);
        grid.add(table.getTableHeader(), BorderLayout.NORTH);
        grid.add(table, BorderLayout.CENTER);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(8, 8, 4, 8));   // keeps header/rows inside the rounded corners
        card.add(grid, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());   // keeps the card at its natural height
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        return top;
    }

    // =====================================================================
    // LOAD COURSES
    // =====================================================================
    private void loadCourses() {
        // NEED TO ADD DATA FROM DATABASE:
        // SELECT course_id, course_name FROM courses WHERE lecturer_id = ?
        // (use the logged-in lecturer's id)

        // ---- dummy data (delete when database is connected) ----
        courseBox.addItem("IT1010 - Introduction to Programming");
        courseBox.addItem("IT2020 - Database Systems");
    }

    // =====================================================================
    // LOAD STUDENT MARKS FOR THE SELECTED COURSE
    // =====================================================================
    private void loadMarks() {
        model.setRowCount(0);
        searchField.setText("");

        String course = (String) courseBox.getSelectedItem();
        if (course == null) return;

        // NEED TO ADD DATA FROM DATABASE:
        // SELECT s.student_id, s.name, SUM(r.marks), SUM(q.total_marks)
        // FROM students s
        // JOIN quiz_results r ON r.student_id = s.student_id
        // JOIN quizzes q      ON q.quiz_id = r.quiz_id
        // JOIN modules m      ON m.module_id = q.module_id
        // WHERE m.course_id = ?
        // GROUP BY s.student_id, s.name
        // Loop through the result set and call addRow(id, name, marks, total) for each row.

        // ---- dummy data (delete when database is connected) ----
        addRow("S001", "Nimal Perera", 78, 100);
        addRow("S002", "Kamal Silva", 45, 100);
        addRow("S003", "Sunil Fernando", 62, 100);
        addRow("S004", "Amali Jayasinghe", 91, 100);
        addRow("S005", "Nadeesha Kumari", 55, 100);

        updateFooter();
    }

    private void addRow(String id, String name, int marks, int total) {
        double percent = marks * 100.0 / total;
        String grade = Marks.gradeOf(percent)[0];
        model.addRow(new Object[]{id, name, marks, total,
                String.format("%.1f%%", percent), grade, String.format("%.2f", pointOf(grade))});
    }

    // Grade point of a grade, e.g. "B+" -> 3.3
    private double pointOf(String grade) {
        for (int i = 0; i < GRADES.length; i++) {
            if (GRADES[i].equals(grade)) return POINTS[i];
        }
        return 0;
    }

    // =====================================================================
    // SEARCH
    // =====================================================================
    private void search() {
        String text = searchField.getText().trim();

        if (text.isEmpty()) {
            sorter.setRowFilter(null);   // show all students
        } else {
            // (?i) = ignore upper/lower case, columns 0 and 1 = Student ID and Student Name
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0, 1));
        }
        updateFooter();
    }

    // "Showing 5 of 5 students  ·  Class average GPA: 3.00"
    private void updateFooter() {
        double sum = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            sum += Double.parseDouble((String) model.getValueAt(i, 6));
        }
        double avg = model.getRowCount() == 0 ? 0 : sum / model.getRowCount();

        footer.setText("Showing " + table.getRowCount() + " of " + model.getRowCount() + " students"
                + "   ·   Class average GPA: " + String.format("%.2f", avg));
    }
}
