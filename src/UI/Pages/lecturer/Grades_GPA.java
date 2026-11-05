package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.SimpleDocListener;
import UI.Pages.Components.Ui;
import data.BatchData;
import data.CourseData;
import data.CourseOfferingData;
import data.EnrollmentData;
import data.Grades_Gpa_Data;
import data.LecturerData;
import data.MarksData;
import data.UndergraduateData;
import data.UserData;
import model.Batch;
import model.Course;
import model.CourseOffering;
import model.Enrollment;
import model.Lecturer;
import model.Undergraduate;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.field;
import static UI.Pages.Components.Ui.flatButton;
import static UI.Pages.Components.Ui.label;
import static UI.Pages.Components.Ui.table;

/**
 * Lecturer - Grades & GPA page
 * - Lecturer selects a batch and a course and sees the grade, grade point, SGPA and CGPA of each student.
 * - "Calculate Grades" calculates and saves them (Grades_Gpa_Data). It uses the CA marks and the end exam
 *   marks that were saved in the Marks page.
 * - Lecturer can search students by name or student ID.
 */
public class Grades_GPA extends LecturerUI.Page {

    JComboBox<String> batchBox = new JComboBox<>();
    JComboBox<String> courseBox = new JComboBox<>();
    JTextField searchField = field(20);
    JLabel footer = new JLabel(" ");

    // read-only table, filled by loadTable()
    DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Student ID", "Student Name", "CA Marks", "End Exam", "Total",
                    "Grade", "Grade Point", "SGPA", "CGPA", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    JTable table = table(model, null, -1);

    // The sorter is used to hide rows that do not match the search text
    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    // ---------- Data layers ----------
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final UserData userData = new UserData();
    private final LecturerData lecturerData = new LecturerData();
    private final BatchData batchData = new BatchData();
    private final UndergraduateData undergraduateData = new UndergraduateData();
    private final MarksData marksData = new MarksData();
    private final Grades_Gpa_Data gpaData = new Grades_Gpa_Data();
    private final int lecturerId;
    private List<CourseOffering> offerings = new ArrayList<>();
    private List<Batch> batches = new ArrayList<>();   // batches of the lecturer's department (same order as batchBox)

    public Grades_GPA(User user) {
        super("Grades & GPA", "Grades and GPA of your students", null);
        this.lecturerId = user == null ? 0 : user.getUser_id();

        table.setRowSorter(sorter);

        // ----- Main body: toolbar on top, table card below -----
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildToolbar(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        setScrollBody(body);

        // When the batch or the course changes, load the table again
        courseBox.addActionListener(e -> loadTable());
        batchBox.addActionListener(e -> loadTable());

        // When the lecturer types in the search box, filter the table
        searchField.getDocument().addDocumentListener(new SimpleDocListener(this::search));

        loadBatches();
        loadCourses();
    }

    // =====================================================================
    // UI
    // =====================================================================

    // Batch and course selectors on the left, search box filling the rest
    private JComponent buildToolbar() {
        courseBox.setFont(font(Font.PLAIN, 13));
        courseBox.setForeground(INK);
        courseBox.setBackground(SURFACE);
        courseBox.setPreferredSize(new Dimension(300, 38));

        batchBox.setFont(font(Font.PLAIN, 13));
        batchBox.setForeground(INK);
        batchBox.setBackground(SURFACE);
        batchBox.setPreferredSize(new Dimension(200, 38));

        searchField.setFont(font(Font.PLAIN, 13));
        searchField.setToolTipText("Search by student name or ID");

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(label("BATCH"));
        left.add(batchBox);
        left.add(Box.createHorizontalStrut(8));
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

    // Table inside a white rounded card, with a title + "Calculate Grades" button on top
    // and the student count at the bottom
    private JComponent buildTableCard() {
        footer.setFont(font(Font.PLAIN, 12));
        footer.setForeground(INK_2);
        footer.setBorder(new EmptyBorder(10, 16, 6, 16));

        JLabel title = new JLabel("Grades & GPA");
        title.setFont(font(Font.BOLD, 15));
        title.setForeground(INK);
        title.setBorder(new EmptyBorder(8, 12, 10, 12));

        JButton calcButton = flatButton("Calculate Grades", 8, 16);
        calcButton.addActionListener(e -> calculateGrades());

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(calcButton, BorderLayout.EAST);

        // header + rows without a scroll pane, so the card grows with the rows
        JPanel grid = new JPanel(new BorderLayout());
        grid.setOpaque(false);
        grid.add(table.getTableHeader(), BorderLayout.NORTH);
        grid.add(table, BorderLayout.CENTER);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(8, 8, 4, 8));   // keeps header/rows inside the rounded corners
        card.add(titleBar, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());   // keeps the card at its natural height
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        return top;
    }

    // =====================================================================
    // LOAD BATCHES (only the batches of the lecturer's department)
    // =====================================================================
    private void loadBatches() {
        Lecturer lecturer = lecturerData.findById(lecturerId);
        if (lecturer == null) return;

        batches = batchData.findByDepartment(lecturer.getDep_id());
        for (Batch b : batches) {
            batchBox.addItem(b.getBatch_name() + " (" + b.getIntake_year() + ")");
        }
    }

    // =====================================================================
    // LOAD COURSES
    // =====================================================================
    private void loadCourses() {
        offerings = new ArrayList<>();
        courseBox.removeAllItems();
        
        java.util.Set<Integer> seenCourses = new LinkedHashSet<>();
        List<CourseOffering> allOfferings = offeringData.findByLecturer(lecturerId);
        
        for (CourseOffering o : allOfferings) {
            Course c = courseData.findById(o.getCourse_id());
            if (c != null && !seenCourses.contains(c.getCourse_id())) {
                offerings.add(o);
                seenCourses.add(c.getCourse_id());
                courseBox.addItem(c.getCourse_code() + " - " + c.getCourse_name());
            } else if (c == null && !seenCourses.contains(o.getCourse_id())) {
                offerings.add(o);
                seenCourses.add(o.getCourse_id());
                courseBox.addItem("Offering " + o.getOffering_id());
            }
        }
    }

    // =====================================================================
    // LOAD THE TABLE FOR THE SELECTED BATCH AND COURSE
    // =====================================================================
    private void loadTable() {
        int index = courseBox.getSelectedIndex();
        if (index < 0) return;
        int offeringId = offerings.get(index).getOffering_id();
        Course course = courseData.findById(offerings.get(index).getCourse_id());

        searchField.setText("");
        model.setRowCount(0);

        Map<Integer, Map<String, BigDecimal>> saved = marksData.findByOffering(offeringId);
        int batchIndex = batchBox.getSelectedIndex();

        for (Enrollment en : enrollmentData.findByOffering(offeringId)) {
            // only the students of the selected batch
            Undergraduate undergraduate = undergraduateData.findById(en.getStudent_id());
            if (undergraduate == null || !inBatch(undergraduate.getBatch_id(), batchIndex)) continue;

            User student = userData.findById(en.getStudent_id());
            Map<String, BigDecimal> marks = saved.getOrDefault(en.getStudent_id(), new HashMap<>());
            model.addRow(buildRow(course, String.valueOf(en.getStudent_id()),
                    student == null ? "" : student.getName(), marks));
        }
        updateFooter();
    }

    // One row. The total, grade, SGPA and CGPA stay empty until "Calculate Grades" saved them.
    private Object[] buildRow(Course course, String id, String name, Map<String, BigDecimal> marks) {
        BigDecimal ca = marks.get("CA");
        BigDecimal end = marks.get("End Exam");
        BigDecimal total = marks.get("Total");
        BigDecimal sgpa = marks.get("SGPA");
        BigDecimal cgpa = marks.get("CGPA");

        String endPart = "";   // end exam mark out of the course's end exam weight
        if (course != null && end != null) {
            endPart = String.format("%.2f", Grades_Gpa_Data.endExamPart(course, end.doubleValue()));
        }

        String totalText = "", grade = "", point = "", status = "";
        if (total != null) {
            totalText = total.toPlainString();
            grade = Grades_Gpa_Data.gradeOf(total.doubleValue());
            point = String.format("%.1f", Grades_Gpa_Data.pointOf(total.doubleValue()));
            status = Grades_Gpa_Data.statusOf(total.doubleValue());
        }

        return new Object[]{id, name, ca == null ? "" : ca.toPlainString(), endPart, totalText, grade, point,
                sgpa == null ? "" : sgpa.toPlainString(), cgpa == null ? "" : cgpa.toPlainString(), status};
    }

    // true if the student's batch is the batch selected in the drop-down
    private boolean inBatch(int batchId, int batchIndex) {
        return batchIndex >= 0 && batches.get(batchIndex).getBatch_id() == batchId;
    }

    // =====================================================================
    // CALCULATE GRADES (button on the table card)
    // =====================================================================
    // total mark, grade, SGPA and CGPA of the students shown in the table
    private void calculateGrades() {
        int index = courseBox.getSelectedIndex();
        if (index < 0) return;

        CourseOffering offering = offerings.get(index);
        Course course = courseData.findById(offering.getCourse_id());
        if (course == null) return;

        List<Integer> studentIds = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            studentIds.add(Integer.parseInt((String) model.getValueAt(i, 0)));
        }

        int done;
        try {
            done = gpaData.calculateAndSave(offering.getOffering_id(), offering.getSemester_id(), course, studentIds);
        } catch (Exception ex) {
            Ui.error(this, "Grades", "Could not calculate grades: " + ex.getMessage());
            return;
        }

        loadTable();   // shows the new grades
        Ui.info(this, "Grades calculated for " + done + " student(s).\n"
                + "A student needs saved CA marks and a saved end exam mark, otherwise the student is skipped.");
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
