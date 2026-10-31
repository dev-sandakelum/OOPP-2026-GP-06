package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.field;
import static UI.Pages.Components.Ui.label;
import static UI.Pages.Components.Ui.table;
import static UI.Pages.Components.Ui.tableCard;

/**
 * Lecturer Course Page
 * Tab 1 - Upload lecture materials
 * Tab 2 - Schedule a quiz for a module
 * Tab 3 - View student quiz marks
 *
 * Every place marked "NEED TO ADD DATA FROM DATABASE" must be connected to MySQL.
 * The sample rows in the code are only dummy data so the screen is not empty.
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

    // ---------- Tab 1: Materials ----------
    JComboBox<String> matModuleBox = new JComboBox<>();
    JTextField matTitleField = field(15);
    DefaultTableModel matModel =
            new ReadOnlyTableModel(new String[]{"Module", "Title", "File Name", "Uploaded On"});

    // ---------- Tab 2: Quiz ----------
    JComboBox<String> quizModuleBox = new JComboBox<>();
    JTextField quizTitleField = field(15);
    JTextField quizDateField = field(12);
    JTextField quizDurationField = field(5);
    JTextField quizTotalField = field(5);
    DefaultTableModel quizModel =
            new ReadOnlyTableModel(new String[]{"Module", "Quiz Title", "Date & Time", "Duration (min)", "Total Marks"});

    // ---------- Tab 3: Marks ----------
    JComboBox<String> marksQuizBox = new JComboBox<>();
    JLabel summaryLabel = new JLabel(" ");
    DefaultTableModel marksModel =
            new ReadOnlyTableModel(new String[]{"Student ID", "Student Name", "Marks", "Total", "Percentage", "Result"});

    public MyCourses() {
        super("My Courses", "Upload materials, schedule quizzes, and manage marks", null);

        quizDateField.setText("2026-11-10 09:00");
        quizDurationField.setText("30");
        quizTotalField.setText("20");

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
        JPanel form = new JPanel(new GridLayout(2, 3, 12, 6));
        form.setOpaque(false);
        form.add(label("MODULE"));
        form.add(label("TITLE"));
        form.add(new JLabel());
        form.add(styleCombo(matModuleBox, 0));
        form.add(matTitleField);
        form.add(uploadBtn);

        return tabPanel(formCard("Upload a new material", form), table(matModel, null, -1));
    }

    private void uploadMaterial() {
        String module = (String) matModuleBox.getSelectedItem();
        String title = matTitleField.getText().trim();

        if (module == null || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a module and enter a title.");
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

            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // NEED TO ADD DATA TO DATABASE:
            // INSERT INTO lecture_materials (module_id, title, file_name, file_path, uploaded_on)
            // VALUES (?, ?, ?, ?, ?)
            // (save the file name and 'saved.toString()' path in the table)

            // Show the new row in the table
            matModel.addRow(new Object[]{module, title, chosen.getName(), today});
            matTitleField.setText("");
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

        // form: labels on the first row, inputs on the second row
        JPanel form = new JPanel(new GridLayout(2, 6, 12, 6));
        form.setOpaque(false);
        form.add(label("MODULE"));
        form.add(label("QUIZ TITLE"));
        form.add(label("DATE & TIME (yyyy-MM-dd HH:mm)"));
        form.add(label("DURATION (MIN)"));
        form.add(label("TOTAL MARKS"));
        form.add(new JLabel());
        form.add(styleCombo(quizModuleBox, 0));
        form.add(quizTitleField);
        form.add(quizDateField);
        form.add(quizDurationField);
        form.add(quizTotalField);
        form.add(scheduleBtn);

        return tabPanel(formCard("Schedule a new quiz", form), table(quizModel, null, -1));
    }

    private void scheduleQuiz() {
        String module = (String) quizModuleBox.getSelectedItem();
        String title = quizTitleField.getText().trim();

        if (module == null || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a module and enter a quiz title.");
            return;
        }

        try {
            LocalDateTime when = LocalDateTime.parse(quizDateField.getText().trim(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            int duration = Integer.parseInt(quizDurationField.getText().trim());
            int total = Integer.parseInt(quizTotalField.getText().trim());

            if (when.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "The quiz date must be in the future.");
                return;
            }

            // NEED TO ADD DATA TO DATABASE:
            // INSERT INTO quizzes (module_id, title, quiz_date, duration_minutes, total_marks)
            // VALUES (?, ?, ?, ?, ?)

            quizModel.addRow(new Object[]{module, title, quizDateField.getText().trim(), duration, total});
            quizTitleField.setText("");
            JOptionPane.showMessageDialog(this, "Quiz scheduled.");

            // The marks tab should also list the new quiz
            loadQuizzesForMarksTab();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please check the date format and the numbers.");
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
        JPanel quizRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quizRow.setOpaque(false);
        quizRow.add(label("QUIZ"));
        quizRow.add(styleCombo(marksQuizBox, 250));
        form.add(quizRow, BorderLayout.WEST);
        form.add(summaryLabel, BorderLayout.EAST);

        // "Result" column is shown as a green / red chip
        JTable marksTable = table(marksModel, RESULT_COLORS, RESULT_COL);

        marksQuizBox.addActionListener(e -> loadMarks());
        return tabPanel(formCard("Quiz results", form), marksTable);
    }

    private void loadMarks() {
        marksModel.setRowCount(0);
        String quiz = (String) marksQuizBox.getSelectedItem();
        if (quiz == null) {
            summaryLabel.setText(" ");
            return;
        }

        // NEED TO ADD DATA FROM DATABASE:
        // SELECT s.student_id, s.name, r.marks, q.total_marks
        // FROM quiz_results r
        // JOIN students s ON s.student_id = r.student_id
        // JOIN quizzes q  ON q.quiz_id = r.quiz_id
        // WHERE q.title = ?
        // Loop through the result set and call addMarkRow(id, name, marks, total) for each row.

        // ---- dummy data (delete when database is connected) ----
        addMarkRow("S001", "Nimal Perera", 18, 20);
        addMarkRow("S002", "Kamal Silva", 9, 20);
        addMarkRow("S003", "Sunil Fernando", 14, 20);
        addMarkRow("S004", "Amali Jayasinghe", 20, 20);

        // Show the class average
        int sum = 0;
        for (int i = 0; i < marksModel.getRowCount(); i++) {
            sum += (int) marksModel.getValueAt(i, 2);
        }
        double avg = marksModel.getRowCount() == 0 ? 0 : (double) sum / marksModel.getRowCount();
        summaryLabel.setText("Students: " + marksModel.getRowCount()
                + "   |   Average marks: " + String.format("%.1f", avg));
    }

    private void addMarkRow(String id, String name, int marks, int total) {
        double percent = marks * 100.0 / total;
        String result = percent >= 50 ? "Pass" : "Fail";
        marksModel.addRow(new Object[]{id, name, marks, total, String.format("%.0f%%", percent), result});
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
        // NEED TO ADD DATA FROM DATABASE:
        // SELECT course_id, course_name FROM courses WHERE lecturer_id = ?
        // (use the logged-in lecturer's id)

        // ---- dummy data ----
        courseBox.addItem("IT1010 - Introduction to Programming");
        courseBox.addItem("IT2020 - Database Systems");
    }

    private void loadCourseData() {
        String course = (String) courseBox.getSelectedItem();
        if (course == null) return;

        // ----- Modules -----
        // NEED TO ADD DATA FROM DATABASE:
        // SELECT module_id, module_name FROM modules WHERE course_id = ?
        matModuleBox.removeAllItems();
        quizModuleBox.removeAllItems();
        String[] modules = {"Module 1 - Basics", "Module 2 - Loops", "Module 3 - Methods"}; // dummy
        for (String m : modules) {
            matModuleBox.addItem(m);
            quizModuleBox.addItem(m);
        }

        // ----- Lecture materials -----
        // NEED TO ADD DATA FROM DATABASE:
        // SELECT m.module_name, l.title, l.file_name, l.uploaded_on
        // FROM lecture_materials l JOIN modules m ON m.module_id = l.module_id
        // WHERE m.course_id = ?
        matModel.setRowCount(0);
        matModel.addRow(new Object[]{"Module 1 - Basics", "Intro Slides", "intro.pdf", "2026-10-01"}); // dummy

        // ----- Scheduled quizzes -----
        // NEED TO ADD DATA FROM DATABASE:
        // SELECT m.module_name, q.title, q.quiz_date, q.duration_minutes, q.total_marks
        // FROM quizzes q JOIN modules m ON m.module_id = q.module_id
        // WHERE m.course_id = ?
        quizModel.setRowCount(0);
        quizModel.addRow(new Object[]{"Module 1 - Basics", "Quiz 1", "2026-11-05 10:00", 30, 20}); // dummy

        loadQuizzesForMarksTab();
    }

    private void loadQuizzesForMarksTab() {
        // Fill the quiz list of the marks tab from the quiz table above.
        // NEED TO ADD DATA FROM DATABASE:
        // SELECT title FROM quizzes WHERE course_id = ?
        marksQuizBox.removeAllItems();
        for (int i = 0; i < quizModel.getRowCount(); i++) {
            marksQuizBox.addItem((String) quizModel.getValueAt(i, 1));
        }
    }
}
