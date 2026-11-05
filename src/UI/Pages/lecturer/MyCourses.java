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
import java.util.LinkedHashSet;
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
    private static final String[] TABS = {"Lecture Materials", "Schedule Quiz", "Assignments", "Student Marks"};

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
    JTable matTable;
    private List<CourseMaterial> shownMaterials = new ArrayList<>();   // same order as matModel rows

    // ---------- Tab 2: Quiz ----------
    JTextField quizTitleField = field(15);
    JTextField quizDateField = field(12);
    DefaultTableModel quizModel =
            new ReadOnlyTableModel(new String[]{"Quiz Title", "Date", "File"});
    JTable quizTable;
    JLabel quizFileLabel = new JLabel("No quiz file uploaded");
    private String uploadedQuizPath = null;   // .md file chosen with "Upload Quiz"; needed to schedule
    private List<Assessment> quizzes = new ArrayList<>();   // same order as the rows of quizModel

    // ---------- Tab 3: Assignments ----------
    JTextField assignTitleField    = field(15);
    JTextField assignStartField    = field(12);   // assessment_date = release/start date
    JTextField assignDueField      = field(12);   // submitted_date  = due date
    DefaultTableModel assignModel  =
            new ReadOnlyTableModel(new String[]{"Assignment", "Start Date", "Due Date"});

    // ---------- Tab 3: Marks ----------
    JLabel summaryLabel = new JLabel(" ");
    DefaultTableModel marksModel =
            new ReadOnlyTableModel(new String[]{"Student ID", "Student Name", "CA /50", "Final /50", "Total /100", "Result"});

    public MyCourses(User user) {
        super("My Courses", "Upload materials, schedule quizzes, and manage marks", null);
        this.lecturerId = user == null ? 0 : user.getUser_id();

        quizDateField.setText(java.time.LocalDate.now().plusDays(7).toString());
        assignStartField.setText(java.time.LocalDate.now().toString());
        assignDueField.setText(java.time.LocalDate.now().plusDays(14).toString());

        // ----- Tab contents (only one is shown at a time) -----
        cards.setOpaque(false);
        cards.add(buildMaterialsTab(), TABS[0]);
        cards.add(buildQuizTab(),      TABS[1]);
        cards.add(buildAssignmentTab(), TABS[2]);
        cards.add(buildMarksTab(),     TABS[3]);

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

        // actions for the selected row
        RoundedButton openBtn = new RoundedButton("Open", false);
        RoundedButton editBtn = new RoundedButton("Edit Title", false);
        RoundedButton replaceBtn = new RoundedButton("Replace File", false);
        RoundedButton deleteBtn = new RoundedButton("Delete", false);
        openBtn.addActionListener(e -> openMaterial());
        editBtn.addActionListener(e -> renameMaterial());
        replaceBtn.addActionListener(e -> replaceMaterialFile());
        deleteBtn.addActionListener(e -> deleteMaterial());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(openBtn);
        actions.add(editBtn);
        actions.add(replaceBtn);
        actions.add(deleteBtn);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.add(form, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);

        matTable = table(matModel, null, -1);
        matTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openMaterial();
            }
        });
        return tabPanel(formCard("Upload a new material", content), matTable);
    }

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    // Copies a chosen file into the uploads folder (timestamp prefix avoids name clashes)
    private Path copyToUploads(File chosen) throws java.io.IOException {
        Files.createDirectories(UPLOAD_DIR);
        Path saved = UPLOAD_DIR.resolve(System.currentTimeMillis() + "_" + chosen.getName());
        Files.copy(chosen.toPath(), saved, StandardCopyOption.REPLACE_EXISTING);
        return saved;
    }

    // Only files the app uploaded itself are ever deleted from disk (never the sample PDFs)
    private void deleteIfUploaded(String filePath) {
        if (filePath == null) return;
        try {
            Path p = Paths.get(filePath).toAbsolutePath().normalize();
            if (p.startsWith(UPLOAD_DIR.toAbsolutePath().normalize())) Files.deleteIfExists(p);
        } catch (Exception ignored) {
            // the database row is the source of truth; a leftover file is harmless
        }
    }

    // The material highlighted in the table (null + a message if none)
    private CourseMaterial selectedMaterial() {
        int row = matTable.getSelectedRow();
        if (row < 0) {
            UI.Pages.Components.Ui.info(this, "Please select a material in the table first.");
            return null;
        }
        return shownMaterials.get(matTable.convertRowIndexToModel(row));
    }

    private void uploadMaterial() {
        int index = courseBox.getSelectedIndex();
        String title = matTitleField.getText().trim();

        if (index < 0 || title.isEmpty()) {
            UI.Pages.Components.Ui.info(this, "Please select a course and enter a title.");
            return;
        }

        // Let the lecturer pick a file
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File chosen = chooser.getSelectedFile();

        try {
            Path saved = copyToUploads(chosen);

            // Save the material in the database
            CourseMaterial m = new CourseMaterial();
            m.setOffering_id(offerings.get(index).getOffering_id());
            m.setLecturer_id(lecturerId);
            m.setTitle(title);
            m.setMaterial_type("LECTURE_NOTES");
            m.setFile_path(saved.toString().replace('\\', '/'));
            m.setFile_size(chosen.length());
            materialData.insert(m);

            matTitleField.setText("");
            loadMaterials();
            UI.Pages.Components.Ui.info(this, "Material uploaded.");

        } catch (Exception ex) {
            UI.Pages.Components.Ui.info(this, "Upload failed: " + ex.getMessage());
        }
    }

    // Open the selected file with the system's default viewer
    private void openMaterial() {
        CourseMaterial m = selectedMaterial();
        if (m == null) return;
        try {
            if (m.getFile_path() == null) {
                UI.Pages.Components.Ui.info(this, "This material has no file.");
                return;
            }
            File f = new File(m.getFile_path());
            if (!f.exists()) {
                UI.Pages.Components.Ui.info(this, "File not found: " + m.getFile_path());
                return;
            }
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            UI.Pages.Components.Ui.info(this, "Could not open the file: " + ex.getMessage());
        }
    }

    // Update: rename the selected material
    private void renameMaterial() {
        CourseMaterial m = selectedMaterial();
        if (m == null) return;
        String[] v = UI.Pages.Components.Ui.form(this, "Edit material",
                new String[]{"TITLE"}, new String[]{m.getTitle()});
        if (v == null) return;
        if (v[0].isEmpty()) {
            UI.Pages.Components.Ui.info(this, "The title cannot be empty.");
            return;
        }
        try {
            m.setTitle(v[0]);
            materialData.update(m);
            loadMaterials();
        } catch (Exception ex) {
            UI.Pages.Components.Ui.info(this, "Could not update: " + ex.getMessage());
        }
    }

    // Update: swap the file of the selected material for a new one
    private void replaceMaterialFile() {
        CourseMaterial m = selectedMaterial();
        if (m == null) return;
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File chosen = chooser.getSelectedFile();
        try {
            String oldPath = m.getFile_path();
            Path saved = copyToUploads(chosen);
            m.setFile_path(saved.toString().replace('\\', '/'));
            m.setFile_size(chosen.length());
            materialData.update(m);
            deleteIfUploaded(oldPath);
            loadMaterials();
            UI.Pages.Components.Ui.info(this, "File replaced.");
        } catch (Exception ex) {
            UI.Pages.Components.Ui.info(this, "Could not replace the file: " + ex.getMessage());
        }
    }

    // Delete the selected material (database row + uploaded file)
    private void deleteMaterial() {
        CourseMaterial m = selectedMaterial();
        if (m == null) return;
        if (!UI.Pages.Components.Ui.confirm(this, "Delete \"" + m.getTitle() + "\"?")) return;
        try {
            if (materialData.delete(m.getMaterial_id())) deleteIfUploaded(m.getFile_path());
            loadMaterials();
        } catch (Exception ex) {
            UI.Pages.Components.Ui.info(this, "Could not delete: " + ex.getMessage());
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
            UI.Pages.Components.Ui.info(this, "Please select a course and enter a quiz title.");
            return;
        }

        try {
            java.sql.Date when = java.sql.Date.valueOf(quizDateField.getText().trim());
            if (when.toLocalDate().isBefore(java.time.LocalDate.now())) {
                UI.Pages.Components.Ui.info(this, "The quiz date must be in the future.");
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
            UI.Pages.Components.Ui.info(this, "Quiz scheduled.");

        } catch (IllegalArgumentException ex) {
            UI.Pages.Components.Ui.info(this, "The date must look like 2026-11-10.");
        } catch (Exception ex) {
            UI.Pages.Components.Ui.info(this, "Could not schedule the quiz: " + ex.getMessage());
        }
    }

    // =====================================================================
    // TAB 3 : ASSIGNMENTS
    // =====================================================================
    private JPanel buildAssignmentTab() {
        RoundedButton addBtn = new RoundedButton("Add Assignment", true);
        addBtn.addActionListener(e -> addAssignment());

        JPanel form = new JPanel(new GridLayout(2, 4, 12, 6));
        form.setOpaque(false);
        form.add(label("ASSIGNMENT TITLE"));
        form.add(label("START DATE (yyyy-MM-dd)"));
        form.add(label("DUE DATE (yyyy-MM-dd)"));
        form.add(new JLabel());
        form.add(assignTitleField);
        form.add(assignStartField);
        form.add(assignDueField);
        form.add(addBtn);

        return tabPanel(formCard("Add a new assignment", form),
                table(assignModel, null, -1));
    }

    private void addAssignment() {
        int index = courseBox.getSelectedIndex();
        String title = assignTitleField.getText().trim();

        if (index < 0 || title.isEmpty()) {
            UI.Pages.Components.Ui.info(this, "Please select a course and enter an assignment title.");
            return;
        }

        try {
            java.sql.Date startDate = java.sql.Date.valueOf(assignStartField.getText().trim());
            java.sql.Date dueDate   = java.sql.Date.valueOf(assignDueField.getText().trim());

            if (dueDate.before(startDate)) {
                UI.Pages.Components.Ui.info(this, "Due date must be on or after the start date.");
                return;
            }

            Assessment a = new Assessment();
            a.setOfferingId(offerings.get(index).getOffering_id());
            a.setAssessmentName(title);
            a.setAssessmentType("ASSIGNMENT");
            a.setAssessmentDate(startDate);   // start / release date
            a.setSubmittedDate(dueDate);       // due date
            assessmentData.insert(a);

            assignTitleField.setText("");
            loadAssignments();
            UI.Pages.Components.Ui.info(this, "Assignment added.");

        } catch (IllegalArgumentException ex) {
            UI.Pages.Components.Ui.info(this, "Dates must look like 2026-11-10.");
        } catch (Exception ex) {
            UI.Pages.Components.Ui.info(this, "Could not add assignment: " + ex.getMessage());
        }
    }

    private void loadAssignments() {
        assignModel.setRowCount(0);
        int index = courseBox.getSelectedIndex();
        if (index < 0) return;

        for (Assessment a : assessmentData.findByOffering(offerings.get(index).getOffering_id())) {
            if (!"ASSIGNMENT".equalsIgnoreCase(a.getAssessmentType())) continue;
            assignModel.addRow(new Object[]{
                    a.getAssessmentName(),
                    a.getAssessmentDate()  == null ? "" : a.getAssessmentDate().toString(),
                    a.getSubmittedDate()   == null ? "" : a.getSubmittedDate().toString()
            });
        }
    }

    // =====================================================================
    // TAB 4 : STUDENT MARKS
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
        java.util.Set<Integer> seenCourses = new LinkedHashSet<>();
        List<CourseOffering> allOfferings = offeringData.findByLecturer(lecturerId);
        offerings = new ArrayList<>();
        courseBox.removeAllItems();
        
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

    private void loadCourseData() {
        loadMaterials();
        loadQuizzes();
        loadAssignments();
        loadMarks();
    }

    private void loadMaterials() {
        matModel.setRowCount(0);
        shownMaterials = new ArrayList<>();
        int index = courseBox.getSelectedIndex();
        if (index < 0) return;

        shownMaterials = materialData.findByOffering(offerings.get(index).getOffering_id());
        for (CourseMaterial m : shownMaterials) {
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
