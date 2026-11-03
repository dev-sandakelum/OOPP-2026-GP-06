package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.SimpleDocListener;
import UI.Pages.Components.Ui;
import data.AssessmentData;
import data.CourseData;
import data.CourseOfferingData;
import data.EnrollmentData;
import data.MarksData;
import data.UserData;
import model.Assessment;
import model.Course;
import model.CourseOffering;
import model.Enrollment;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.field;
import static UI.Pages.Components.Ui.label;
import static UI.Pages.Components.Ui.table;

/**
 * Lecturer - Grades & GPA page
 * - Lecturer selects a course and enters the marks of each student:
 *   Quiz 1, Quiz 2, Quiz 3, Mid Exam and, if the course has them, its scheduled Assignments and Project.
 * - Every row has its own "Save" button.
 * - Lecturer can search students by name or student ID.
 *
 * Courses, students and marks come from the database (see MarksData for how marks are stored).
 */
public class Grades_GPA extends LecturerUI.Page {

    // marks every course has; assignments and project are added from the course's scheduled assessments
    private static final String[] FIXED_MARKS = {"Quiz 1", "Quiz 2", "Quiz 3", "Mid Exam"};

    // first two columns are the student, then the marks, the last column is the Save button
    private static final int FIRST_MARK_COL = 2;

    // ---------- Data layers ----------
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final AssessmentData assessmentData = new AssessmentData();
    private final UserData userData = new UserData();
    private final MarksData marksData = new MarksData();
    private final int lecturerId;
    private List<CourseOffering> offerings = new ArrayList<>();

    JComboBox<String> courseBox = new JComboBox<>();
    JTextField searchField = field(20);
    JLabel footer = new JLabel(" ");

    // only the mark columns can be edited (not the student ID, name or the Save button)
    DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int col) {
            return col >= FIRST_MARK_COL && col < getColumnCount() - 1;
        }
    };
    JTable table = table(model, null, -1);

    // The sorter is used to hide rows that do not match the search text
    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    public Grades_GPA(User user) {
        super("Grades & GPA", "Enter the marks of your students", null);
        this.lecturerId = user == null ? 0 : user.getUser_id();

        table.setRowSorter(sorter);

        // ----- Main body: toolbar on top, table card below -----
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildToolbar(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        setScrollBody(body);

        // When the course changes, build the table again for that course
        courseBox.addActionListener(e -> loadTable());

        // When the lecturer types in the search box, filter the table
        searchField.getDocument().addDocumentListener(new SimpleDocListener(this::search));

        // When the lecturer clicks the Save button of a row, save that student's marks
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == table.getColumnCount() - 1) saveRow(row);
            }
        });

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
        offerings = offeringData.findByLecturer(lecturerId);
        for (CourseOffering o : offerings) {
            Course c = courseData.findById(o.getCourse_id());
            courseBox.addItem(c == null ? "Offering " + o.getOffering_id()
                    : c.getCourse_code() + " - " + c.getCourse_name());
        }
    }

    // =====================================================================
    // LOAD THE TABLE FOR THE SELECTED COURSE
    // =====================================================================
    private void loadTable() {
        searchField.setText("");

        int index = courseBox.getSelectedIndex();
        if (index < 0) return;
        int offeringId = offerings.get(index).getOffering_id();

        // columns: ID, Name, Quiz 1-3, Mid Exam, then the course's assignments / project, then Save
        List<String> columns = new ArrayList<>();
        columns.add("Student ID");
        columns.add("Student Name");
        for (String name : FIXED_MARKS) columns.add(name);
        boolean hasAssignment = false, hasProject = false;
        for (Assessment a : assessmentData.findByOffering(offeringId)) {
            if ("ASSIGNMENT".equalsIgnoreCase(a.getAssessmentType())) hasAssignment = true;
            if ("PROJECT".equalsIgnoreCase(a.getAssessmentType())) hasProject = true;
        }
        if (hasAssignment) {
            columns.add("Assignment 1");   // two separate fields for the assignments
            columns.add("Assignment 2");
        }
        if (hasProject) columns.add("Project");
        columns.add("Save");

        model.setRowCount(0);
        model.setColumnIdentifiers(columns.toArray());

        // one row per enrolled student, with the marks already saved in the database
        Map<Integer, Map<String, BigDecimal>> saved = marksData.findByOffering(offeringId);
        for (Enrollment en : enrollmentData.findByOffering(offeringId)) {
            User student = userData.findById(en.getStudent_id());
            Map<String, BigDecimal> marks = saved.getOrDefault(en.getStudent_id(), new HashMap<>());

            Object[] row = new Object[columns.size()];
            row[0] = String.valueOf(en.getStudent_id());
            row[1] = student == null ? "" : student.getName();
            for (int i = FIRST_MARK_COL; i < row.length - 1; i++) {
                BigDecimal mark = marks.get(columns.get(i));
                row[i] = mark == null ? "" : mark.stripTrailingZeros().toPlainString();
            }
            row[row.length - 1] = "Save";
            model.addRow(row);
        }

        // show the last column as a button (setting the columns again removes old renderers)
        table.getColumnModel().getColumn(columns.size() - 1).setCellRenderer(saveButtonRenderer());
        updateFooter();
    }

    // Draws a "Save" button inside the cell
    private TableCellRenderer saveButtonRenderer() {
        // flat rounded blue button (no shadow), small enough to fit the 44px row
        JButton button = new JButton("Action") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BRAND);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);   // draws the "Save" text
            }
        };
        button.setFont(font(Font.BOLD, 12));
        button.setForeground(ON_BRAND);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(6, 14, 6, 14));

        // transparent panel around it gives some space inside the cell
        JPanel cell = new JPanel(new GridBagLayout());
        cell.setOpaque(false);
        cell.add(button);
        return (t, value, selected, focused, row, col) -> cell;
    }

    // =====================================================================
    // SAVE ONE STUDENT'S MARKS
    // =====================================================================
    private void saveRow(int viewRow) {
        // the lecturer may still be typing in a cell, so finish that first
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        int studentId = Integer.parseInt((String) table.getValueAt(viewRow, 0));
        String name = (String) table.getValueAt(viewRow, 1);

        // read every mark: empty = not entered, otherwise a number from 0 to 100
        Map<String, BigDecimal> marks = new HashMap<>();
        for (int col = FIRST_MARK_COL; col < table.getColumnCount() - 1; col++) {
            String text = String.valueOf(table.getValueAt(viewRow, col)).trim();
            if (text.isEmpty()) {
                marks.put(table.getColumnName(col), null);
                continue;
            }
            try {
                BigDecimal mark = new BigDecimal(text);
                if (mark.signum() < 0 || mark.compareTo(BigDecimal.valueOf(100)) > 0) throw new NumberFormatException();
                marks.put(table.getColumnName(col), mark);
            } catch (NumberFormatException ex) {
                Ui.error(this, "Invalid mark", table.getColumnName(col) + " of " + name
                        + " must be a number from 0 to 100.");
                return;
            }
        }

        try {
            int offeringId = offerings.get(courseBox.getSelectedIndex()).getOffering_id();
            marksData.save(studentId, offeringId, marks);
        } catch (Exception ex) {
            Ui.error(this, "Marks", "Could not save marks: " + ex.getMessage());
            return;
        }

        Ui.info(this, "Marks of " + name + " (" + studentId + ") saved.");
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

    // "Showing 5 of 5 students"
    private void updateFooter() {
        footer.setText("Showing " + table.getRowCount() + " of " + model.getRowCount() + " students");
    }
}
