package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.SimpleDocListener;
import data.CourseData;
import data.CourseOfferingData;
import data.EnrollmentData;
import data.ExamData;
import data.ResultData;
import data.UserData;
import model.Course;
import model.CourseOffering;
import model.Enrollment;
import model.Exam;
import model.Result;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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
 * Courses and marks come from the database (CourseOfferingData, CourseData, EnrollmentData,
 * ExamData, ResultData and UserData).
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

    // ---------- Data layers ----------
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final ExamData examData = new ExamData();
    private final ResultData resultData = new ResultData();
    private final UserData userData = new UserData();
    private final int lecturerId;
    private List<CourseOffering> offerings = new ArrayList<>();

    public Grades_GPA(User user) {
        super("Grades & GPA", "Grades and GPA for your modules", null);
        this.lecturerId = user == null ? 0 : user.getUser_id();

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
        offerings = offeringData.findByLecturer(lecturerId);
        for (CourseOffering o : offerings) {
            Course c = courseData.findById(o.getCourse_id());
            courseBox.addItem(c == null ? "Offering " + o.getOffering_id()
                    : c.getCourse_code() + " - " + c.getCourse_name());
        }
    }

    // =====================================================================
    // LOAD STUDENT MARKS FOR THE SELECTED COURSE
    // =====================================================================
    private void loadMarks() {
        model.setRowCount(0);
        searchField.setText("");

        String course = (String) courseBox.getSelectedItem();
        if (course == null) return;

        // total mark (out of 100) comes from the student's result in this course's exams
        int index = courseBox.getSelectedIndex();
        if (index >= 0) {
            int offeringId = offerings.get(index).getOffering_id();
            List<Exam> exams = examData.findByOffering(offeringId);

            for (Enrollment en : enrollmentData.findByOffering(offeringId)) {
                Result r = null;
                for (Exam exam : exams) {
                    r = resultData.find(en.getStudent_id(), exam.getExamId());
                    if (r != null) break;
                }
                if (r == null || r.getTotalMark() == null) continue;   // no marks entered yet

                User student = userData.findById(en.getStudent_id());
                addRow(String.valueOf(en.getStudent_id()), student == null ? "" : student.getName(),
                        r.getTotalMark().doubleValue(), 100);
            }
        }

        updateFooter();
    }

    private void addRow(String id, String name, double marks, int total) {
        double percent = marks * 100.0 / total;
        String grade = Marks.gradeOf(percent)[0];
        model.addRow(new Object[]{id, name, Marks.fmt(marks), total,
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
