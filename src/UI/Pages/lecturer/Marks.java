package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.SimpleDocListener;
import UI.Pages.Components.Ui;
import data.BatchData;
import data.CourseData;
import data.CourseOfferingData;
import data.EnrollmentData;
import data.LecturerData;
import data.MarksData;
import data.UndergraduateData;
import data.UserData;
import data.calc_CA_Data;
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
 * Lecturer - Marks page
 * - Lecturer selects a batch and a course, and enters the marks of each student:
 *   Quiz 1, 2, 3, Mid Exam, Assignment 1, 2 (if the course has an assignment weight) and Project (if the course has a project weight).
 *   Every row has its own "Save" button.
 * - The Assessment filter shows only the quiz / mid exam / assignment / project columns.
 * - "Calculate CA Marks" calculates the CA marks of the students (calc_CA_Data).
 * - Second table: end exam mark of each student, with the CA / End exam pass or fail status.
 * - Lecturer can search students by name or student ID.
 *
 * Marks are saved in the result table by MarksData.
 */
public class Marks extends LecturerUI.Page {

    // first two columns are the student, then the marks, then the CA marks, the last column is the Save button
    private static final int FIRST_MARK_COL = 2;
    private static final int END_COL = 2;   // end exam mark column of the second table

    JComboBox<String> batchBox = new JComboBox<>();
    JComboBox<String> courseBox = new JComboBox<>();
    JComboBox<String> assessBox = new JComboBox<>(
            new String[]{"All assessments", "Quizzes", "Mid Exam", "Assignments", "Project"});
    JTextField searchField = field(20);
    JLabel footer = new JLabel(" ");

    // only the mark columns can be edited (not the student ID, name, CA marks or the Save button)
    DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int col) {
            return col >= FIRST_MARK_COL && col < getColumnCount() - 2;
        }
    };
    JTable table = table(model, null, -1);

    // Second table: end exam marks (Student ID, Name, End Exam, CA Marks, Status, Action)
    DefaultTableModel endModel = new DefaultTableModel(
            new Object[]{"Student ID", "Student Name", "End Exam Marks", "CA Marks", "Status", "Action"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return col == END_COL;   // only the end exam mark can be edited
        }
    };
    JTable endTable = table(endModel, null, -1);
    TableRowSorter<DefaultTableModel> endSorter = new TableRowSorter<>(endModel);
    JLabel endFooter = new JLabel(" ");

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
    private final calc_CA_Data caData = new calc_CA_Data();
    private final int lecturerId;
    private List<CourseOffering> offerings = new ArrayList<>();
    private int caWeight = 0;         // CA marks are out of this (course.ca_weight)
    private int assignmentCount = 0;   // assignments of the course that is shown in the table
    private List<Batch> batches = new ArrayList<>();   // batches of the lecturer's department (same order as batchBox, after "All batches")

    public Marks(User user) {
        super("Marks", "Enter and review the marks of your students", null);
        this.lecturerId = user == null ? 0 : user.getUser_id();

        table.setRowSorter(sorter);
        endTable.setRowSorter(endSorter);

        // ----- Main body: toolbar on top, table card below -----
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildToolbar(), BorderLayout.NORTH);

        // the two tables, one under the other
        JPanel tables = new JPanel();
        tables.setOpaque(false);
        tables.setLayout(new BoxLayout(tables, BoxLayout.Y_AXIS));
        tables.add(buildTableCard(table, footer, null, null));
        tables.add(Box.createVerticalStrut(16));
        tables.add(buildTableCard(endTable, endFooter, "End exam marks", null));
        JPanel tablesTop = new JPanel(new BorderLayout());   // keeps the cards at their natural height
        tablesTop.setOpaque(false);
        tablesTop.add(tables, BorderLayout.NORTH);
        body.add(tablesTop, BorderLayout.CENTER);
        setScrollBody(body);

        // When the course changes, build the table again for that course
        courseBox.addActionListener(e -> loadTable());
        batchBox.addActionListener(e -> loadTable());
        assessBox.addActionListener(e -> applyAssessmentFilter());

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

        endTable.getColumnModel().getColumn(5).setCellRenderer(saveButtonRenderer());   // last column = Save button

        // Save button of the end exam table
        endTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = endTable.rowAtPoint(e.getPoint());
                int col = endTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == endTable.getColumnCount() - 1) saveEndRow(row);
            }
        });

        loadBatches();
        loadCourses();
    }

    // =====================================================================
    // UI
    // =====================================================================

    // Card with Batch / Course / Assessment / Search (a small caption above each) and the Calculate CA Marks button
    private JComponent buildToolbar() {
        styleInput(batchBox, 180);
        styleInput(courseBox, 280);
        styleInput(assessBox, 160);

        searchField.setFont(font(Font.PLAIN, 13));
        searchField.setToolTipText("Search by student name or ID");

        JButton calcButton = flatButton("Calculate CA Marks", 10, 16);
        calcButton.addActionListener(e -> calculateCa());

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.anchor = GridBagConstraints.SOUTHWEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.insets = new Insets(0, 0, 0, 12); row.add(labelled("Batch", batchBox), g);
        g.gridx = 1;                                      row.add(labelled("Course", courseBox), g);
        g.gridx = 2;                                      row.add(labelled("Assessment", assessBox), g);
        g.gridx = 3; g.weightx = 1;                       row.add(labelled("Search", searchField), g);
        g.gridx = 4; g.weightx = 0; g.insets = new Insets(0, 0, 0, 0);
        g.anchor = GridBagConstraints.SOUTH;              row.add(calcButton, g);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    // Small caption above an input
    private JComponent labelled(String caption, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(label(caption), BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private void styleInput(JComponent c, int width) {
        c.setFont(font(Font.PLAIN, 13));
        c.setForeground(INK);
        c.setBackground(SURFACE);
        c.setPreferredSize(new Dimension(width, 38));
    }

    // Table inside a white rounded card, with the student count at the bottom (and an optional title on top)
    private JComponent buildTableCard(JTable t, JLabel footer, String title, JComponent action) {
        footer.setFont(font(Font.PLAIN, 12));
        footer.setForeground(INK_2);
        footer.setBorder(new EmptyBorder(10, 16, 6, 16));

        // header + rows without a scroll pane, so the card grows with the rows
        JPanel grid = new JPanel(new BorderLayout());
        grid.setOpaque(false);
        grid.add(t.getTableHeader(), BorderLayout.NORTH);
        grid.add(t, BorderLayout.CENTER);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(8, 8, 4, 8));   // keeps header/rows inside the rounded corners
        if (title != null) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(font(Font.BOLD, 15));
            titleLabel.setForeground(INK);
            titleLabel.setBorder(new EmptyBorder(8, 12, 10, 12));
            JPanel titleBar = new JPanel(new BorderLayout());   // title on the left, optional button on the right
            titleBar.setOpaque(false);
            titleBar.add(titleLabel, BorderLayout.WEST);
            if (action != null) titleBar.add(action, BorderLayout.EAST);
            card.add(titleBar, BorderLayout.NORTH);
        }
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
    // LOAD THE TABLE FOR THE SELECTED COURSE
    // =====================================================================
    private void loadTable() {
        int index = courseBox.getSelectedIndex();
        if (index < 0) return;
        int offeringId = offerings.get(index).getOffering_id();

        searchField.setText("");

        Course course = courseData.findById(offerings.get(index).getCourse_id());

        // a course with an assignment weight has two assignments (Assignment 1 and 2)
        assignmentCount = course != null && course.getAssignment_weight() > 0 ? 2 : 0;

        // the Project column is only for courses that have a project weight
        boolean hasProject = course != null && course.getProject_weight() > 0;
        caWeight = course == null ? 0 : course.getCa_weight();

        // columns: ID, Name, 3 quizzes, Mid Exam, then Assignment 1, 2 / Project only if the course has them, CA Marks, then Save
        List<String> marks = new ArrayList<>();
        marks.add("Quiz 1");
        marks.add("Quiz 2");
        marks.add("Quiz 3");
        marks.add("Mid Exam");
        for (int i = 1; i <= assignmentCount; i++) marks.add("Assignment " + i);
        if (hasProject) marks.add("Project");

        List<String> columns = new ArrayList<>();
        columns.add("Student ID");
        columns.add("Student Name");
        columns.addAll(marks);
        columns.add("CA Marks");
        columns.add("Action");

        model.setRowCount(0);
        endModel.setRowCount(0);
        model.setColumnIdentifiers(columns.toArray());

        // one row per enrolled student, with the marks already saved in the database
        Map<Integer, Map<String, BigDecimal>> saved = marksData.findByOffering(offeringId);
        // only the students of the selected batch (the drop-down has the lecturer's department batches only)
        int batchIndex = batchBox.getSelectedIndex();

        for (Enrollment en : enrollmentData.findByOffering(offeringId)) {
            // skip students that are not in the selected batch
            Undergraduate undergraduate = undergraduateData.findById(en.getStudent_id());
            if (undergraduate == null || !inBatch(undergraduate.getBatch_id(), batchIndex)) continue;

            User student = userData.findById(en.getStudent_id());
            Map<String, BigDecimal> studentMarks = saved.getOrDefault(en.getStudent_id(), new HashMap<>());

            Object[] row = new Object[columns.size()];
            row[0] = String.valueOf(en.getStudent_id());
            row[1] = student == null ? "" : student.getName();
            for (int i = 0; i < marks.size(); i++) {
                BigDecimal mark = studentMarks.get(marks.get(i));
                row[FIRST_MARK_COL + i] = mark == null ? "" : mark.stripTrailingZeros().toPlainString();
            }
            BigDecimal ca = studentMarks.get("CA");
            row[row.length - 2] = ca == null ? "" : ca.toPlainString();   // calculated by "Calculate CA Marks"
            row[row.length - 1] = "Action";
            model.addRow(row);

            // same student in the end exam table
            BigDecimal end = studentMarks.get("End Exam");
            String endText = end == null ? "" : end.stripTrailingZeros().toPlainString();
            String caText = (String) row[row.length - 2];
            endModel.addRow(new Object[]{row[0], row[1], endText, caText, statusOf(caText, endText), "Action"});
        }

        applyAssessmentFilter();
        updateFooter();
    }

    // Shows only the mark columns of the selected assessment (ID, Name, CA Marks and Save always stay)
    private void applyAssessmentFilter() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        // build all the columns again, so the hidden ones come back
        table.createDefaultColumnsFromModel();
        TableColumnModel columns = table.getColumnModel();
        if (columns.getColumnCount() == 0) return;

        // the last column is shown as a button (new columns have no renderer)
        columns.getColumn(columns.getColumnCount() - 1).setCellRenderer(saveButtonRenderer());

        String selected = (String) assessBox.getSelectedItem();
        for (int i = columns.getColumnCount() - 1; i >= 0; i--) {
            TableColumn column = columns.getColumn(i);
            if (!isShown(model.getColumnName(column.getModelIndex()), selected)) columns.removeColumn(column);
        }
    }

    private boolean isShown(String column, String selected) {
        switch (selected) {
            case "Quizzes":     return !isMarkColumn(column) || column.startsWith("Quiz");
            case "Mid Exam":    return !isMarkColumn(column) || column.equals("Mid Exam");
            case "Assignments": return !isMarkColumn(column) || column.startsWith("Assignment");
            case "Project":     return !isMarkColumn(column) || column.equals("Project");
            default:            return true;   // All assessments
        }
    }

    private boolean isMarkColumn(String column) {
        return column.startsWith("Quiz") || column.equals("Mid Exam")
                || column.startsWith("Assignment") || column.equals("Project");
    }

    // true if the student's batch is the batch selected in the drop-down
    private boolean inBatch(int batchId, int batchIndex) {
        return batchIndex >= 0 && batches.get(batchIndex).getBatch_id() == batchId;
    }

    // Draws a "Save" button inside the cell
    private TableCellRenderer saveButtonRenderer() {
        JButton button = flatButton("Save", 6, 14);

        // transparent panel around it gives some space inside the cell
        JPanel cell = new JPanel(new GridBagLayout());
        cell.setOpaque(false);
        cell.add(button);
        return (t, value, selected, focused, row, col) -> cell;
    }

    // =====================================================================
    // CALCULATE CA MARKS (button in the toolbar)
    // =====================================================================
    private void calculateCa() {
        int index = courseBox.getSelectedIndex();
        if (index < 0) return;
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        int offeringId = offerings.get(index).getOffering_id();
        Course course = courseData.findById(offerings.get(index).getCourse_id());
        if (course == null) return;

        // every student shown in the table (the selected batch)
        List<Integer> studentIds = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            studentIds.add(Integer.parseInt((String) model.getValueAt(i, 0)));
        }

        int done;
        try {
            done = caData.calculateAndSave(offeringId, course, assignmentCount, studentIds);
        } catch (Exception ex) {
            Ui.error(this, "CA marks", "Could not calculate CA marks: " + ex.getMessage());
            return;
        }

        loadTable();   // shows the new CA marks
        Ui.info(this, "CA marks calculated for " + done + " student(s).\n"
                + "Only saved marks are used, and students with no saved marks are skipped.");
    }

    // =====================================================================
    // SAVE ONE STUDENT'S MARKS
    // =====================================================================
    private void saveRow(int viewRow) {
        // the lecturer may still be typing in a cell, so finish that first
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        int index = courseBox.getSelectedIndex();
        if (index < 0) return;
        int offeringId = offerings.get(index).getOffering_id();
        int studentId = Integer.parseInt((String) table.getValueAt(viewRow, 0));
        String name = (String) table.getValueAt(viewRow, 1);

        // every mark must be empty or a number from 0 to 100; empty = not entered (null)
        Map<String, BigDecimal> marks = new HashMap<>();
        // (model columns, so the columns hidden by the Assessment filter are saved too)
        int modelRow = table.convertRowIndexToModel(viewRow);
        for (int col = FIRST_MARK_COL; col < model.getColumnCount() - 2; col++) {
            String columnName = model.getColumnName(col);
            String text = String.valueOf(model.getValueAt(modelRow, col)).trim();
            if (text.isEmpty()) {
                marks.put(columnName, null);
                continue;
            }
            try {
                BigDecimal mark = new BigDecimal(text);
                if (mark.signum() < 0 || mark.compareTo(BigDecimal.valueOf(100)) > 0) throw new NumberFormatException();
                marks.put(columnName, mark);
            } catch (NumberFormatException ex) {
                Ui.error(this, "Invalid mark", columnName + " of " + name
                        + " must be a number from 0 to 100.");
                return;
            }
        }

        try {
            marksData.save(studentId, offeringId, marks);
        } catch (Exception ex) {
            Ui.error(this, "Marks", "Could not save marks: " + ex.getMessage());
            return;
        }
        Ui.info(this, "Student No: " + studentId + "marks saved.");
    }

    // =====================================================================
    // PASS / FAIL STATUS
    // =====================================================================
    // "CA: Pass  |  End: Fail". A part passes with half or more of its marks, "-" = not available yet.
    private String statusOf(String caText, String endText) {
        String ca = "-";
        if (!caText.isEmpty() && caWeight > 0) {
            ca = passOrFail(Double.parseDouble(caText) * 100.0 / caWeight);   // CA marks are out of ca_weight
        }
        String end = endText.isEmpty() ? "-" : passOrFail(Double.parseDouble(endText));   // end exam is out of 100
        return "CA: " + ca + "  |  End: " + end;
    }

    private String passOrFail(double percentage) {
        return percentage >= 50 ? "Pass" : "Fail";
    }

    // =====================================================================
    // SAVE ONE STUDENT'S END EXAM MARK
    // =====================================================================
    private void saveEndRow(int viewRow) {
        if (endTable.isEditing()) endTable.getCellEditor().stopCellEditing();

        int index = courseBox.getSelectedIndex();
        if (index < 0) return;
        int offeringId = offerings.get(index).getOffering_id();
        int studentId = Integer.parseInt((String) endTable.getValueAt(viewRow, 0));
        String name = (String) endTable.getValueAt(viewRow, 1);

        // the mark must be empty (not entered) or a number from 0 to 100
        String text = String.valueOf(endTable.getValueAt(viewRow, END_COL)).trim();
        BigDecimal mark = null;
        if (!text.isEmpty()) {
            try {
                mark = new BigDecimal(text);
                if (mark.signum() < 0 || mark.compareTo(BigDecimal.valueOf(100)) > 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                Ui.error(this, "Invalid mark", "End exam mark of " + name + " must be a number from 0 to 100.");
                return;
            }
        }

        try {
            marksData.saveEndExam(studentId, offeringId, mark);
        } catch (Exception ex) {
            Ui.error(this, "Marks", "Could not save the end exam mark: " + ex.getMessage());
            return;
        }
        // show the new pass / fail status of this student
        int modelRow = endTable.convertRowIndexToModel(viewRow);
        endModel.setValueAt(statusOf((String) endModel.getValueAt(modelRow, 3), text), modelRow, 4);

        Ui.info(this, "End exam mark of " + name + " (" + studentId + ") saved.");
    }

    // =====================================================================
    // SEARCH
    // =====================================================================
    private void search() {
        String text = searchField.getText().trim();

        if (text.isEmpty()) {
            sorter.setRowFilter(null);   // show all students
            endSorter.setRowFilter(null);
        } else {
            // (?i) = ignore upper/lower case, columns 0 and 1 = Student ID and Student Name
            RowFilter<Object, Object> filter = RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0, 1);
            sorter.setRowFilter(filter);
            endSorter.setRowFilter(filter);
        }
        updateFooter();
    }

    // "Showing 5 of 5 students"
    private void updateFooter() {
        footer.setText("Showing " + table.getRowCount() + " of " + model.getRowCount() + " students");
        endFooter.setText("Showing " + endTable.getRowCount() + " of " + endModel.getRowCount() + " students");
    }
}
