package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.Ui;
import data.AssessmentData;
import data.AssignmentSubmissionData;
import data.CourseData;
import data.CourseOfferingData;
import data.EnrollmentData;
import model.Assessment;
import model.AssignmentSubmission;
import model.Course;
import model.CourseOffering;
import model.Enrollment;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.table;
import static UI.Pages.Components.Ui.tableCard;

/**
 * Assessments card — shown in the student My Courses page.
 *
 * Loads ALL assessments (QUIZ, ASSIGNMENT, PRACTICAL, MID_EXAM, PROJECT …)
 * for every course the student is ENROLLED in, from the assessment table.
 *
 * Columns: COURSE | ASSESSMENT | TYPE | START DATE | DUE DATE | STATUS
 *
 * TYPE chip colours  : QUIZ=blue, ASSIGNMENT=green, PRACTICAL=purple,
 *                      MID_EXAM=orange, PROJECT=teal
 * STATUS chip colours:
 *   ASSIGNMENT rows  → Not Submitted / Submitted / Late / Graded
 *   all other rows   → Upcoming / Completed  (based on assessment_date vs today)
 *
 * Action buttons:
 *   "Attempt"              — QUIZ / MID_EXAM / PRACTICAL / PROJECT rows
 *   "Submit / Re-submit"   — ASSIGNMENT rows (file picker → saved to disk + DB)
 *   "View Submission"      — opens the submitted file for ASSIGNMENT rows
 */
public class AssessmentsCard extends Card {

    // ── type chip colours ──────────────────────────────────────────────────
    private static final Map<String, Color> TYPE_COLORS = Map.of(
            "QUIZ",       BRAND,
            "ASSIGNMENT", OK,
            "PRACTICAL",  new Color(130, 70, 200),
            "MID_EXAM",   WARN,
            "PROJECT",    new Color(0, 160, 180)
    );
    private static final int TYPE_COL   = 2;

    // ── status chip colours ────────────────────────────────────────────────
    private static final Map<String, Color> STATUS_COLORS = Map.of(
            "Submitted",     OK,
            "Late",          WARN,
            "Not Submitted", BAD,
            "Graded",        BRAND,
            "Upcoming",      new Color(0, 160, 180),
            "Completed",     INK_2
    );
    private static final int STATUS_COL = 5;

    // ── table ──────────────────────────────────────────────────────────────
    private final ReadOnlyTableModel model = new ReadOnlyTableModel(
            new String[]{"COURSE", "ASSESSMENT", "TYPE", "START DATE", "DUE DATE", "STATUS"});
    private final JTable assessTable = table(model, null, -1);

    // parallel to model rows
    private final List<Assessment>           shownAssessments = new ArrayList<>();
    private final List<AssignmentSubmission> shownSubmissions = new ArrayList<>(); // null for non-ASSIGNMENT

    // ── DAOs ───────────────────────────────────────────────────────────────
    private final AssessmentData           assessmentData = new AssessmentData();
    private final AssignmentSubmissionData submissionData = new AssignmentSubmissionData();
    private final EnrollmentData           enrollmentData = new EnrollmentData();
    private final CourseOfferingData       offeringData   = new CourseOfferingData();
    private final CourseData               courseData     = new CourseData();

    private final int studentId;
    private static final Path UPLOAD_DIR = Paths.get("uploads", "assignments");

    // ── constructor ────────────────────────────────────────────────────────
    public AssessmentsCard(User user) {
        super(false);
        this.studentId = user == null ? 0 : user.getUser_id();

        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(18, 20, 18, 20));

        // heading
        JLabel heading = new JLabel("Assessments");
        heading.setFont(font(Font.BOLD, 16));
        heading.setForeground(INK);

        // action buttons
        RoundedButton attemptBtn = new RoundedButton("Attempt", true);
        RoundedButton submitBtn  = new RoundedButton("Submit / Re-submit", false);
        RoundedButton viewBtn    = new RoundedButton("View Submission", false);
        attemptBtn.addActionListener(e -> attemptAssessment());
        submitBtn.addActionListener(e  -> submitAssignment());
        viewBtn.addActionListener(e    -> viewSubmission());

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnBar.setOpaque(false);
        btnBar.add(attemptBtn);
        btnBar.add(submitBtn);
        btnBar.add(viewBtn);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(btnBar,  BorderLayout.CENTER);

        // apply chip renderers
        assessTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assessTable.getColumnModel().getColumn(TYPE_COL)
                .setCellRenderer(new ChipRenderer(TYPE_COLORS));
        assessTable.getColumnModel().getColumn(STATUS_COL)
                .setCellRenderer(new ChipRenderer(STATUS_COLORS));

        JComponent tCard = tableCard(assessTable);
        tCard.setPreferredSize(new Dimension(0, 280));

        add(top,   BorderLayout.NORTH);
        add(tCard, BorderLayout.CENTER);

        setPreferredSize(new Dimension(0, 390));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 390));

        load();
    }

    // ── load ───────────────────────────────────────────────────────────────

    private void load() {
        model.setRowCount(0);
        shownAssessments.clear();
        shownSubmissions.clear();

        try {
            for (Enrollment en : enrollmentData.findByStudent(studentId)) {
                if (!"ENROLLED".equalsIgnoreCase(
                        en.getEnrollment_status() == null ? "" : en.getEnrollment_status().trim()))
                    continue;

                CourseOffering offering = offeringData.findById(en.getOffering_id());
                if (offering == null) continue;

                Course course = courseData.findById(offering.getCourse_id());
                String code = course == null ? "?" : course.getCourse_code();

                for (Assessment a : assessmentData.findByOffering(en.getOffering_id())) {

                    String type = a.getAssessmentType() == null
                            ? "" : a.getAssessmentType().toUpperCase().trim();

                    // Look up submission only for ASSIGNMENT rows.
                    // If the assignment_submission table doesn't exist yet,
                    // treat it as "not submitted" instead of crashing the whole load.
                    AssignmentSubmission sub = null;
                    if (type.equals("ASSIGNMENT")) {
                        sub = safeGetSubmission(a.getAssessmentId());
                    }

                    model.addRow(new Object[]{
                            code,
                            a.getAssessmentName(),
                            type,
                            a.getAssessmentDate() == null ? "—" : a.getAssessmentDate().toString(),
                            a.getSubmittedDate()  == null ? "—" : a.getSubmittedDate().toString(),
                            deriveStatus(type, a, sub)
                    });
                    shownAssessments.add(a);
                    shownSubmissions.add(sub);
                }
            }
        } catch (Exception ex) {
            Ui.warn(this, "Error", "Could not load assessments: " + ex.getMessage());
        }
    }

    /**
     * Looks up a submission record; returns null (instead of throwing) when
     * the table is missing or any DB error occurs — keeps the rest of the
     * table loading even if the submission table hasn't been migrated yet.
     */
    private AssignmentSubmission safeGetSubmission(int assessmentId) {
        try {
            return submissionData.findByAssessmentAndStudent(assessmentId, studentId);
        } catch (Exception ignored) {
            return null;
        }
    }

    // ── status logic ────────────────────────────────────────────────────────

    private String deriveStatus(String type, Assessment a, AssignmentSubmission sub) {
        if (type.equals("ASSIGNMENT")) {
            if (sub == null) return "Not Submitted";
            if ("GRADED".equalsIgnoreCase(sub.getStatus())) return "Graded";
            if (a.getSubmittedDate() != null && sub.getSubmittedAt() != null) {
                LocalDate due  = a.getSubmittedDate().toLocalDate();
                LocalDate when = sub.getSubmittedAt().toLocalDateTime().toLocalDate();
                if (when.isAfter(due)) return "Late";
            }
            return "Submitted";
        }
        // QUIZ / MID_EXAM / PRACTICAL / PROJECT
        if (a.getAssessmentDate() != null) {
            return a.getAssessmentDate().toLocalDate().isAfter(LocalDate.now())
                    ? "Upcoming" : "Completed";
        }
        return "—";
    }

    // ── attempt (QUIZ / MID_EXAM / PRACTICAL / PROJECT) ───────────────────

    private void attemptAssessment() {
        int view = assessTable.getSelectedRow();
        if (view < 0) { Ui.info(this, "Please select an assessment first."); return; }

        int row = assessTable.convertRowIndexToModel(view);
        Assessment a = shownAssessments.get(row);

        String type = a.getAssessmentType() == null
                ? "" : a.getAssessmentType().toUpperCase().trim();

        if (type.equals("ASSIGNMENT")) {
            Ui.info(this, "This is an assignment — use \"Submit / Re-submit\" to upload your file.");
            return;
        }

        // Show attempt confirmation dialog with assessment details
        String startDate = a.getAssessmentDate() == null ? "Not set" : a.getAssessmentDate().toString();
        String dueDate   = a.getSubmittedDate()  == null ? "Not set" : a.getSubmittedDate().toString();
        String ref       = a.getReferenceNumber() == null ? "—"       : a.getReferenceNumber();

        JPanel info = new JPanel(new GridLayout(0, 1, 0, 6));
        info.setOpaque(false);
        info.add(row("Assessment",  a.getAssessmentName()));
        info.add(row("Type",        type));
        info.add(row("Reference",   ref));
        info.add(row("Date",        startDate));
        info.add(row("Due / End",   dueDate));
        info.add(row("Status",      deriveStatus(type, a, null)));

        // For Upcoming assessments warn the student; for Completed show info only
        if (a.getAssessmentDate() != null
                && !a.getAssessmentDate().toLocalDate().isAfter(LocalDate.now())) {
            // Past — show details only
            Ui.ask(this, a.getAssessmentName() + " — Details", buildPanel(info));
        } else {
            // Upcoming — confirm intent to attempt
            if (Ui.ask(this, "Attempt: " + a.getAssessmentName(), buildInfoWithNote(info,
                    "This assessment is upcoming. Contact your lecturer to attempt it in class."))) {
                Ui.info(this, "Your intent has been noted. Please attend the scheduled "
                        + type.toLowerCase().replace("_", " ") + " session.");
            }
        }
    }

    /** Helper: single labeled row inside the attempt dialog. */
    private JPanel row(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(font(Font.BOLD, 12));
        lbl.setForeground(INK_2);
        lbl.setPreferredSize(new Dimension(100, 20));
        JLabel val = new JLabel(value);
        val.setFont(font(Font.PLAIN, 13));
        val.setForeground(INK);
        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    /** Wraps a panel in a JPanel suitable for Ui.info (which takes a JComponent). */
    private JPanel buildPanel(JPanel inner) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(4, 4, 4, 4));
        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    /** Wraps a panel and appends a note label — used for Ui.ask. */
    private JPanel buildInfoWithNote(JPanel inner, String note) {
        JLabel noteLbl = new JLabel("<html><i>" + note + "</i></html>");
        noteLbl.setFont(font(Font.PLAIN, 12));
        noteLbl.setForeground(WARN);
        noteLbl.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(4, 4, 4, 4));
        wrap.add(inner,   BorderLayout.CENTER);
        wrap.add(noteLbl, BorderLayout.SOUTH);
        return wrap;
    }

    // ── submit assignment ──────────────────────────────────────────────────

    private void submitAssignment() {
        int view = assessTable.getSelectedRow();
        if (view < 0) { Ui.info(this, "Please select an assessment first."); return; }

        int row = assessTable.convertRowIndexToModel(view);
        Assessment a = shownAssessments.get(row);

        String type = a.getAssessmentType() == null
                ? "" : a.getAssessmentType().toUpperCase().trim();

        if (!type.equals("ASSIGNMENT")) {
            Ui.info(this, "Only ASSIGNMENT rows can be submitted here.\n"
                    + "For quizzes and exams, use the \"Attempt\" button.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose your assignment file");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File chosen = chooser.getSelectedFile();

        try {
            Files.createDirectories(UPLOAD_DIR);
            Path dest = UPLOAD_DIR.resolve(
                    studentId + "_" + a.getAssessmentId() + "_"
                    + System.currentTimeMillis() + "_" + chosen.getName());
            Files.copy(chosen.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            String saved = dest.toString().replace('\\', '/');

            AssignmentSubmission existing = shownSubmissions.get(row);
            if (existing == null) {
                AssignmentSubmission sub = new AssignmentSubmission();
                sub.setAssessmentId(a.getAssessmentId());
                sub.setStudentId(studentId);
                sub.setFilePath(saved);
                sub.setStatus("SUBMITTED");
                submissionData.insert(sub);
            } else {
                existing.setFilePath(saved);
                existing.setStatus("SUBMITTED");
                submissionData.update(existing);
            }

            Ui.info(this, "Assignment submitted successfully.");
            load();

        } catch (Exception ex) {
            Ui.warn(this, "Submit Error", "Could not submit: " + ex.getMessage());
        }
    }

    // ── view submission ────────────────────────────────────────────────────

    private void viewSubmission() {
        int view = assessTable.getSelectedRow();
        if (view < 0) { Ui.info(this, "Please select an assessment first."); return; }

        int row = assessTable.convertRowIndexToModel(view);
        AssignmentSubmission sub = shownSubmissions.get(row);

        if (sub == null || sub.getFilePath() == null) {
            Ui.info(this, "No file has been submitted for this assessment.");
            return;
        }
        File f = new File(sub.getFilePath());
        if (!f.exists()) {
            Ui.info(this, "Submitted file not found: " + sub.getFilePath());
            return;
        }
        try {
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            Ui.warn(this, "Open Error", "Could not open the file: " + ex.getMessage());
        }
    }

    // ── chip renderer ──────────────────────────────────────────────────────

    private static class ChipRenderer extends DefaultTableCellRenderer {

        private final Map<String, Color> colors;

        ChipRenderer(Map<String, Color> colors) { this.colors = colors; }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            String text  = value == null ? "" : value.toString();
            Color  color = colors.getOrDefault(text, INK_2);
            Chip chip = new Chip(text, color);
            chip.setOpaque(true);
            chip.setBackground(isSelected ? BRAND_SOFT : SURFACE);
            return chip;
        }
    }
}
