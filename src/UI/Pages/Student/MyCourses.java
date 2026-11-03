
package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.Ui;
import Exception.DataAccessException;

import data.EnrollmentData;
import data.CourseOfferingData;
import data.CourseData;
import data.SemesterData;
import data.LecturerData;
import data.CourseMaterialData;

import model.User;
import model.Enrollment;
import model.CourseOffering;
import model.Course;
import model.Semester;
import model.Lecturer;
import model.CourseMaterial;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static UI.Kit.Palette.*;

public class MyCourses extends Page {

    private final User user;

    // Database access classes
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final SemesterData semesterData = new SemesterData();
    private final LecturerData lecturerData = new LecturerData();
    private final CourseMaterialData materialData = new CourseMaterialData();

    // Table components
    private final ReadOnlyTableModel tableModel =
            new ReadOnlyTableModel(new String[]{"CODE", "COURSE NAME", "SEMESTER", "LECTURER"});
    private final JTable coursesTable = Ui.table(tableModel, null, -1);

    // Stores offering IDs in the same order as table model rows
    private final List<Integer> offeringIds = new ArrayList<>();

    // KPI label
    private final JLabel moduleCountLabel = new JLabel("0");

    // Constructor
    public MyCourses(User user) {

        super("My Courses", "View your registered modules", null);

        this.user = user;

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ==============================
        // ENROLLED MODULES KPI CARD
        // ==============================

        Card kpiCard = new Card(true);
        kpiCard.setLayout(new BoxLayout(kpiCard, BoxLayout.Y_AXIS));
        kpiCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        kpiCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel kpiTitle = new JLabel("Enrolled Modules");
        kpiTitle.setFont(font(Font.BOLD, 13));
        kpiTitle.setForeground(ON_BRAND_2);

        moduleCountLabel.setFont(font(Font.BOLD, 34));
        moduleCountLabel.setForeground(ON_BRAND);

        JLabel kpiSub = new JLabel("Active course registrations");
        kpiSub.setFont(font(Font.PLAIN, 11));
        kpiSub.setForeground(ON_BRAND_2);

        kpiTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        moduleCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        kpiCard.add(kpiTitle);
        kpiCard.add(Box.createVerticalGlue());
        kpiCard.add(moduleCountLabel);
        kpiCard.add(kpiSub);

        body.add(kpiCard);
        body.add(Box.createVerticalStrut(20));

        // ==============================
        // COURSES TABLE
        // ==============================

        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JComponent tableCard = Ui.tableCard(coursesTable);
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableCard.setPreferredSize(new Dimension(0, 320));
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        body.add(tableCard);

        // ==============================
        // ASSESSMENTS CARD
        // ==============================

        body.add(Box.createVerticalStrut(25));

        JComponent assignments = new AssessmentsCard(user);
        assignments.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(assignments);

        // ==============================
        // LECTURER UPLOADED MATERIALS CARD
        // ==============================

        body.add(Box.createVerticalStrut(25));

        JComponent materials = new MaterialsCard(user);
        materials.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(materials);

        setScrollBody(body);

        // ==============================
        // LOAD COURSES
        // ==============================

        loadCourses();

        // ==============================
        // COURSE SELECTION LISTENER
        // ==============================

        coursesTable.getSelectionModel().addListSelectionListener(event -> {

            if (event.getValueIsAdjusting()) {
                return;
            }

            int selectedViewRow = coursesTable.getSelectedRow();

            if (selectedViewRow >= 0) {

                // Convert sorted view row to original model row
                int selectedModelRow =
                        coursesTable.convertRowIndexToModel(
                                selectedViewRow
                        );

                if (selectedModelRow >= 0 &&
                        selectedModelRow < offeringIds.size()) {

                    int offeringId =
                            offeringIds.get(selectedModelRow);

                    showCourseMaterials(offeringId);
                }
            }
        });
    }

    // =====================================================
    // LOAD ONLY ENROLLED COURSES OF THE LOGGED-IN STUDENT
    // =====================================================

    private void loadCourses() {

        tableModel.setRowCount(0);
        offeringIds.clear();

        moduleCountLabel.setText("0");

        try {

            if (user == null) {

                throw new IllegalStateException(
                        "Logged-in user is not available."
                );
            }

            // Get logged-in student's ID
            int studentId = user.getUser_id();

            // Get enrollments belonging to this student
            List<Enrollment> enrollments =
                    enrollmentData.findByStudent(studentId);

            if (enrollments == null || enrollments.isEmpty()) {
                return;
            }

            // Prevent duplicate offering IDs
            Set<Integer> addedOfferingIds = new HashSet<>();

            for (Enrollment enrollment : enrollments) {

                if (enrollment == null) {
                    continue;
                }

                // Get enrollment status
                String status = enrollment.getEnrollment_status();

                // Display only ENROLLED records
                if (status == null ||
                        !"ENROLLED".equalsIgnoreCase(status.trim())) {

                    continue;
                }

                int offeringId = enrollment.getOffering_id();

                // Skip duplicate offerings
                if (!addedOfferingIds.add(offeringId)) {
                    continue;
                }

                // Get course offering
                CourseOffering offering =
                        offeringData.findById(offeringId);

                if (offering == null) {
                    continue;
                }

                // Get course details
                Course course =
                        courseData.findById(
                                offering.getCourse_id()
                        );

                if (course == null) {
                    continue;
                }

                // Get semester details
                Semester semester =
                        semesterData.findById(
                                offering.getSemester_id()
                        );

                String semesterName = "N/A";

                if (semester != null) {
                    semesterName = "Semester " +
                            semester.getSemester_number();
                }

                // Get lecturer details
                String lecturerName = "Not Assigned";

                Integer lecturerId = offering.getLecturer_id();

                if (lecturerId != null) {

                    Lecturer lecturer =
                            lecturerData.findById(lecturerId);

                    if (lecturer != null &&
                            lecturer.getName() != null &&
                            !lecturer.getName().trim().isEmpty()) {

                        lecturerName = lecturer.getName();
                    }
                }

                // Add course details to table
                tableModel.addRow(new Object[]{

                        course.getCourse_code(),
                        course.getCourse_name(),
                        semesterName,
                        lecturerName

                });

                // Keep offering ID matching the model row
                offeringIds.add(offeringId);
            }

            // Update enrolled modules count
            int enrolledCount = tableModel.getRowCount();

            moduleCountLabel.setText(
                    String.valueOf(enrolledCount)
            );

            // Display message when no enrolled courses exist
            if (enrolledCount == 0) {
                Ui.info(this, "My Courses", "You currently have no enrolled courses.");
            }

        } catch (DataAccessException e) {
            Ui.warn(this, "Database Error", "Unable to load your courses.\n" + e.getMessage());

        } catch (Exception e) {
            Ui.warn(this, "Error", "An unexpected error occurred.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================
    // DISPLAY COURSE LEARNING MATERIALS
    // =====================================================

    private void showCourseMaterials(int offeringId) {

        try {

            List<CourseMaterial> materials =
                    materialData.findByOffering(offeringId);

            if (materials == null || materials.isEmpty()) {
                Ui.info(this, "No learning materials available for this course.");
                return;
            }

            DefaultListModel<CourseMaterial> listModel = new DefaultListModel<>();
            for (CourseMaterial material : materials) {
                if (material != null) listModel.addElement(material);
            }

            if (listModel.isEmpty()) {
                Ui.info(this, "No learning materials available for this course.");
                return;
            }

            JList<CourseMaterial> materialsList = new JList<>(listModel);
            materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            materialsList.setFont(font(Font.PLAIN, 13));
            materialsList.setFixedCellHeight(35);
            materialsList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
                String title = value.getTitle() != null ? value.getTitle() : "Untitled Material";
                String type  = value.getMaterial_type() != null ? value.getMaterial_type() : "File";
                JLabel label = new JLabel(title + "  (" + type + ")");
                label.setOpaque(true);
                label.setBorder(new EmptyBorder(5, 10, 5, 10));
                label.setBackground(isSelected ? BRAND_SOFT : SURFACE);
                label.setForeground(INK);
                return label;
            });

            JScrollPane sp = new JScrollPane(materialsList);
            sp.setPreferredSize(new Dimension(450, 220));

            if (Ui.ask(this, "Course Learning Materials", sp)) {
                CourseMaterial selected = materialsList.getSelectedValue();
                if (selected != null) {
                    openMaterial(selected);
                } else {
                    Ui.info(this, "Please select a learning material.");
                }
            }

        } catch (DataAccessException e) {
            Ui.warn(this, "Database Error", "Unable to load course materials.\n" + e.getMessage());
        } catch (Exception e) {
            Ui.warn(this, "Error", "An unexpected error occurred while loading materials.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================
    // OPEN SELECTED MATERIAL
    // =====================================================

    private void openMaterial(CourseMaterial material) {

        try {

            if (material == null) {
                return;
            }

            String externalLink = material.getExternal_link();

            String filePath = material.getFile_path();

            // Open external link using default browser
            if (externalLink != null &&
                    !externalLink.trim().isEmpty()) {

                if (!Desktop.isDesktopSupported()) {

                    throw new UnsupportedOperationException(
                            "Desktop operations are not supported."
                    );
                }

                Desktop desktop = Desktop.getDesktop();

                if (!desktop.isSupported(Desktop.Action.BROWSE)) {

                    throw new UnsupportedOperationException(
                            "Opening web links is not supported."
                    );
                }

                desktop.browse(
                        new URI(externalLink.trim())
                );

            }

            // Open local file
            else if (filePath != null &&
                    !filePath.trim().isEmpty()) {

                File file = new File(filePath);

                if (!file.exists() || !file.isFile()) {
                    Ui.info(this, "The material file could not be found.");
                    return;
                }

                if (!Desktop.isDesktopSupported()) {

                    throw new UnsupportedOperationException(
                            "Desktop operations are not supported."
                    );
                }

                Desktop desktop = Desktop.getDesktop();

                if (!desktop.isSupported(Desktop.Action.OPEN)) {

                    throw new UnsupportedOperationException(
                            "Opening files is not supported."
                    );
                }

                desktop.open(file);

            }

            // No link or file path available
            else {
                Ui.info(this, "No file path or external link is available.");
            }

        } catch (Exception e) {
            Ui.warn(this, "Open Material Error", "Unable to open the selected material.\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
