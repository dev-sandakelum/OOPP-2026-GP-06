package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import data.AssessmentData;
import data.CourseData;
import data.CourseMaterialData;
import data.CourseOfferingData;
import data.EnrollmentData;
import data.ExamData;
import data.ResultData;
import data.UserData;
import model.Assessment;
import model.Course;
import model.CourseMaterial;
import model.CourseOffering;
import model.Enrollment;
import model.Exam;
import model.Result;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.field;
import static UI.Pages.Components.Ui.label;
import static UI.Pages.Components.Ui.table;
import static UI.Pages.Components.Ui.tableCard;

/**
 * Lecturer Course Page
 * Tab 1 - Upload lecture materials
 * Tab 2 - Schedule a quiz (saved in the assessment table)
 * Tab 3 - View student marks (CA / final from the result table)
 *
 * Data comes from CourseOfferingData, CourseData, CourseMaterialData, AssessmentData,
 * EnrollmentData, ExamData, ResultData and UserData.
 */
public class MyCourses extends LecturerUI.Page {

    // tab names (also used as the CardLayout keys)
    private static final String[] TABS = {"Lecture Materials", "Schedule Quiz", "Student Marks"};

    // colors of the "Result" chips in the marks table
    private static final Map<String, Color> RESULT_COLORS = Map.of("Pass", OK, "Fail", BAD);
    private static final int RESULT_COL = 5;

    // ---------- Common ----------
    JComboBox<String> courseBox = new JComboBox<>();
    JPanel cards = new JPanel(new CardLayout());
    JButton[] tabButtons = new JButton[TABS.length];

    // ---------- Data layers ----------
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final ExamData examData = new ExamData();
    private final ResultData resultData = new ResultData();
    private final UserData userData = new UserData();
    private final CourseMaterialData materialData = new CourseMaterialData();
    private final AssessmentData assessmentData = new AssessmentData();
    private final int lecturerId;
    private List<CourseOffering> offerings = new ArrayList<>();

    // ---------- Tab 1: Materials ----------
    JTextField matTitleField = field(15);
    DefaultTableModel matModel =
            new ReadOnlyTableModel(new String[]{"Title", "File Name", "Uploaded On"});

    // ---------- Tab 2: Quiz ----------
    JTextField quizTitleField = field(15);
    JTextField quizDateField = field(12);
    DefaultTableModel quizModel =
            new ReadOnlyTableModel(new String[]{"Quiz Title", "Date", "File"});
    JTable quizTable;
    JLabel quizFileLabel = new JLabel("No quiz file uploaded");
    private String uploadedQuizPath = null;   // .md file chosen with "Upload Quiz"; needed to schedule
    private List<Assessment> quizzes = new ArrayList<>();   // same order as the rows of quizModel

    // ---------- Tab 3: Marks ----------
    JLabel summaryLabel = new JLabel(" ");
    DefaultTableModel marksModel =
            new ReadOnlyTableModel(new String[]{"Student ID", "Student Name", "CA /50", "Final /50", "Total /100", "Result"});

    public MyCourses(User user) {
        super("My Courses", "Upload materials, schedule quizzes, and manage marks", null);
        this.lecturerId = user == null ? 0 : user.getUser_id();

        quizDateField.setText(java.time.LocalDate.now().plusDays(7).toString());

        // ----- Tab contents (only one is shown at a time) -----
        cards.setOpaque(false);
        cards.add(buildMaterialsTab(), TABS[0]);
        cards.add(buildQuizTab(), TABS[1]);
        cards.add(buildMarksTab(), TABS[2]);

        // ----- Main body: top bar + tab contents -----
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildTopBar(), BorderLayout.NORTH);
        body.add(cards, BorderLayout.CENTER);
        setBody(body);

        showTab(0);

        // When the course changes, reload everything for that course
        courseBox.addActionListener(e -> loadCourseData());

        loadCourses();
    }

    // =====================================================================
    // TOP BAR : tab buttons on the left, course selector on the right
    // =====================================================================
    private JPanel buildTopBar() {
        JPanel tabsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabsRow.setOpaque(false);
        for (int i = 0; i < TABS.length; i++) {
            int index = i;
            JButton b = new JButton(TABS[i]);
            b.setFont(font(Font.BOLD, 14));
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> showTab(index));
            tabButtons[i] = b;
            tabsRow.add(b);
        }

        JPanel courseRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        courseRow.setOpaque(false);
        courseRow.add(label("COURSE"));
        courseRow.add(styleCombo(courseBox, 300));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, LINE));   // line under the tabs
        bar.add(tabsRow, BorderLayout.WEST);
        bar.add(courseRow, BorderLayout.EAST);
        return bar;
    }

    // Show one tab and underline its button
    private void showTab(int index) {
        ((CardLayout) cards.getLayout()).show(cards, TABS[index]);
        for (int i = 0; i < tabButtons.length; i++) {
            boolean selected = (i == index);
            tabButtons[i].setForeground(selected ? BRAND : INK_2);
            tabButtons[i].setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 3, 0, selected ? BRAND : new Color(0, 0, 0, 0)),
                    new EmptyBorder(10, 16, 10, 16)));
        }
    }

    // =====================================================================
    // TAB 1 : LECTURE MATERIALS
    // =====================================================================
    private JPanel buildMaterialsTab() {
        RoundedButton uploadBtn = new RoundedButton("Choose File & Upload", true);
        uploadBtn.addActionListener(e -> uploadMaterial());

        // form: labels on the first row, inputs on the second row
        JPanel form = new JPanel(new GridLayout(2, 2, 12, 6));
        form.setOpaque(false);
        form.add(label("TITLE"));
        form.add(new JLabel());
        form.add(matTitleField);
        form.add(uploadBtn);

        return tabPanel(formCard("Upload a new material", form), table(matModel, null, -1));
    }

    private void uploadMaterial() {
        int index = courseBox.getSelectedIndex();
        String title = matTitleField.getText().trim();

        if (index < 0 || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a course and enter a title.");
            return;
        }

        // Let the lecturer pick a file
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File chosen = chooser.getSelectedFile();

        try {
            // Copy the file into an "uploads" folder
            Path folder = Paths.get("uploads");
            Files.createDirectories(folder);
            Path saved = folder.resolve(chosen.getName());
            Files.copy(chosen.toPath(), saved, StandardCopyOption.REPLACE_EXISTING);

            // Save the material in the database
            CourseMaterial m = new CourseMaterial();
            m.setOffering_id(offerings.get(index).getOffering_id());
            m.setLecturer_id(lecturerId);
            m.setTitle(title);
            m.setMaterial_type("FILE");
            m.setFile_path(saved.toString());
            m.setFile_size(chosen.length());
            materialData.insert(m);

            matTitleField.setText("");
            loadMaterials();
            JOptionPane.showMessageDialog(this, "Material uploaded.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Upload failed: " + ex.getMessage());
        }
    }

    // =====================================================================
    // TAB 2 : SCHEDULE QUIZ
    // =====================================================================
    private JPanel buildQuizTab() {
        RoundedButton scheduleBtn = new RoundedButton("Schedule Quiz", true);
        scheduleBtn.addActionListener(e -> scheduleQuiz());
        RoundedButton uploadBtn = new RoundedButton("Upload Quiz (.md)", false);
        uploadBtn.addActionListener(e -> uploadQuiz());
        RoundedButton removeBtn = new RoundedButton("Remove Quiz", false);
        removeBtn.addActionListener(e -> removeQuiz());

        // inputs: labels on the first row, fields on the second row
        JPanel inputs = new JPanel(new GridLayout(2, 2, 12, 6));
        inputs.setOpaque(false);
        inputs.add(label("QUIZ TITLE"));
        inputs.add(label("DATE (yyyy-MM-dd)"));
        inputs.add(quizTitleField);
        inputs.add(quizDateField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(scheduleBtn);
        buttons.add(uploadBtn);
        buttons.add(removeBtn);
        quizFileLabel.setFont(font(Font.PLAIN, 12));
        quizFileLabel.setForeground(INK_2);
        buttons.add(quizFileLabel);

        JPanel form = new JPanel(new BorderLayout(0, 12));
        form.setOpaque(false);
        form.add(inputs, BorderLayout.NORTH);
        form.add(buttons, BorderLayout.SOUTH);

        quizTable = table(quizModel, null, -1);
        return tabPanel(formCard("Schedule a new quiz", form), quizTable);
    }

    // Schedule works only after a .md quiz file has been uploaded
    private void scheduleQuiz() {
        if (uploadedQuizPath == null) {
            JOptionPane.showMessageDialog(this, "Please upload the quiz (.md file) before scheduling it.");
            return;
        }

        // no title typed -> use the file name without ".md"
        String title = quizTitleField.getText().trim();
        if (title.isEmpty()) {
            String name = Paths.get(uploadedQuizPath).getFileName().toString();
            title = name.substring(0, name.length() - 3);
        }
        saveQuiz(title, uploadedQuizPath);
    }

    // Choose a quiz saved as a .md file; it is copied to uploads/quizzes and used by "Schedule Quiz"
    private void uploadQuiz() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Markdown quiz (*.md)", "md"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File chosen = chooser.getSelectedFile();

        if (!chosen.getName().toLowerCase().endsWith(".md")) {
            JOptionPane.showMessageDialog(this, "Only .md files can be uploaded.");
            return;
        }

        try {
            Path folder = Paths.get("uploads", "quizzes");
            Files.createDirectories(folder);
            Path saved = folder.resolve(chosen.getName());
            Files.copy(chosen.toPath(), saved, StandardCopyOption.REPLACE_EXISTING);

            uploadedQuizPath = saved.toString();
            quizFileLabel.setText("Uploaded: " + chosen.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Upload failed: " + ex.getMessage());
        }
    }

    // Delete the quiz selected in the table
    private void removeQuiz() {
        int row = quizTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a quiz in the table first.");
            return;
        }
        Assessment quiz = quizzes.get(quizTable.convertRowIndexToModel(row));

        int answer = JOptionPane.showConfirmDialog(this,
                "Remove the quiz \"" + quiz.getAssessmentName() + "\"?", "Remove Quiz", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;

        try {
            assessmentData.delete(quiz.getAssessmentId());
            loadQuizzes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not remove the quiz: " + ex.getMessage());
        }
    }

    // Saves a quiz in the assessment table (filePath is the uploaded .md file, or null)
    private void saveQuiz(String title, String filePath) {
        int index = courseBox.getSelectedIndex();

        if (index < 0 || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a course and enter a quiz title.");
            return;
        }

        try {
            java.sql.Date when = java.sql.Date.valueOf(quizDateField.getText().trim());
            if (when.toLocalDate().isBefore(java.time.LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "The quiz date must be in the future.");
                return;
            }

            Assessment a = new Assessment();
            a.setOfferingId(offerings.get(index).getOffering_id());
            a.setAssessmentName(title);
            a.setAssessmentType("Quiz");
            a.setAssessmentDate(when);
            a.setReferenceNumber(filePath);   // path of the uploaded .md file
            assessmentData.insert(a);

            quizTitleField.setText("");
            uploadedQuizPath = null;   // the next quiz needs its own file
            quizFileLabel.setText("No quiz file uploaded");
            loadQuizzes();
            JOptionPane.showMessageDialog(this, "Quiz scheduled.");

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "The date must look like 2026-11-10.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save the quiz: " + ex.getMessage());
        }
    }

    // =====================================================================
    // TAB 3 : STUDENT MARKS
    // =====================================================================
    private JPanel buildMarksTab() {
        summaryLabel.setFont(font(Font.BOLD, 13));
        summaryLabel.setForeground(INK_2);

        JPanel form = new JPanel(new BorderLayout(12, 0));
        form.setOpaque(false);
        form.add(summaryLabel, BorderLayout.WEST);

        // "Result" column is shown as a green / red chip
        JTable marksTable = table(marksModel, RESULT_COLORS, RESULT_COL);

        return tabPanel(formCard("Student marks", form), marksTable);
    }

    private void loadMarks() {
        marksModel.setRowCount(0);
        int index = courseBox.getSelectedIndex();
        if (index < 0) {
            summaryLabel.setText(" ");
            return;
        }

        int offeringId = offerings.get(index).getOffering_id();
        List<Exam> exams = examData.findByOffering(offeringId);

        double sum = 0;
        for (Enrollment en : enrollmentData.findByOffering(offeringId)) {
            // the student's first result among this course's exams (null if no marks yet)
            Result r = null;
            for (Exam exam : exams) {
                r = resultData.find(en.getStudent_id(), exam.getExamId());
                if (r != null) break;
            }

            User student = userData.findById(en.getStudent_id());
            String name = student == null ? "" : student.getName();
            if (r == null || r.getTotalMark() == null) {
                marksModel.addRow(new Object[]{String.valueOf(en.getStudent_id()), name, "-", "-", "-", "-"});
                continue;
            }
            double total = r.getTotalMark().doubleValue();
            sum += total;
            marksModel.addRow(new Object[]{String.valueOf(en.getStudent_id()), name,
                    r.getCaResult() == null ? "-" : r.getCaResult(),
                    r.getFinalExamMark() == null ? "-" : r.getFinalExamMark(),
                    r.getTotalMark(), total >= 40 ? "Pass" : "Fail"});
        }

        // Show the class average
        double avg = marksModel.getRowCount() == 0 ? 0 : sum / marksModel.getRowCount();
        summaryLabel.setText("Students: " + marksModel.getRowCount()
                + "   |   Average total: " + String.format("%.1f", avg));
    }

    // =====================================================================
    // SMALL UI HELPERS
    // =====================================================================

    // One tab = form card on top + table card below
    private JPanel tabPanel(JComponent formCard, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(formCard, BorderLayout.NORTH);
        panel.add(tableCard(table), BorderLayout.CENTER);
        return panel;
    }

    // White rounded card with a small heading and the form inside
    private JComponent formCard(String heading, JComponent form) {
        JLabel title = new JLabel(heading);
        title.setFont(font(Font.BOLD, 16));
        title.setForeground(INK);

        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    // Same combo box look as the other lecturer pages (width 0 = let the layout decide)
    private JComboBox<String> styleCombo(JComboBox<String> box, int width) {
        box.setFont(font(Font.PLAIN, 13));
        box.setForeground(INK);
        box.setBackground(SURFACE);
        if (width > 0) box.setPreferredSize(new Dimension(width, 38));
        return box;
    }

    // =====================================================================
    // LOADING DATA
    // =====================================================================
    private void loadCourses() {
        offerings = offeringData.findByLecturer(lecturerId);
        courseBox.removeAllItems();
        for (CourseOffering o : offerings) {
            Course c = courseData.findById(o.getCourse_id());
            courseBox.addItem(c == null ? "Offering " + o.getOffering_id()
                    : c.getCourse_code() + " - " + c.getCourse_name());
        }
    }

    private void loadCourseData() {
        loadMaterials();
        loadQuizzes();
        loadMarks();
    }

    private void loadMaterials() {
        matModel.setRowCount(0);
        int index = courseBox.getSelectedIndex();
        if (index < 0) return;

        for (CourseMaterial m : materialData.findByOffering(offerings.get(index).getOffering_id())) {
            String fileName = m.getFile_path() == null ? "" : Paths.get(m.getFile_path()).getFileName().toString();
            String date = m.getUpload_date() == null ? "" : m.getUpload_date().toString().substring(0, 10);
            matModel.addRow(new Object[]{m.getTitle(), fileName, date});
        }
    }

    private void loadQuizzes() {
        quizModel.setRowCount(0);
        quizzes = new ArrayList<>();
        int index = courseBox.getSelectedIndex();
        if (index < 0) return;

        for (Assessment a : assessmentData.findByOffering(offerings.get(index).getOffering_id())) {
            if (!"Quiz".equals(a.getAssessmentType())) continue;
            quizzes.add(a);
            String file = a.getReferenceNumber() == null ? "" : Paths.get(a.getReferenceNumber()).getFileName().toString();
            quizModel.addRow(new Object[]{a.getAssessmentName(),
                    a.getAssessmentDate() == null ? "" : a.getAssessmentDate().toString(), file});
        }
    }
}
