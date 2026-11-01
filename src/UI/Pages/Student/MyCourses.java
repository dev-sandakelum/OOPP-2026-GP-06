
package UI.Pages.Student;

import UI.Pages.Components.Page;
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
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private DefaultTableModel tableModel;
    private JTable coursesTable;

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
        body.setBackground(new Color(245, 247, 250));
        body.setBorder(new EmptyBorder(20, 20, 20, 20));


        // ==============================
        // ENROLLED MODULES KPI CARD
        // ==============================

        JPanel kpiCard = new JPanel(new BorderLayout());

        kpiCard.setBackground(Color.WHITE);

        kpiCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 235)),
                new EmptyBorder(18, 20, 18, 20)
        ));

        kpiCard.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 95)
        );

        JLabel kpiTitle = new JLabel("Enrolled Modules");

        kpiTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        kpiTitle.setForeground(new Color(100, 110, 120));

        moduleCountLabel.setFont(
                new Font("Arial", Font.BOLD, 28)
        );

        moduleCountLabel.setForeground(
                new Color(35, 90, 65)
        );

        JPanel kpiContent = new JPanel();

        kpiContent.setLayout(
                new BoxLayout(kpiContent, BoxLayout.Y_AXIS)
        );

        kpiContent.setOpaque(false);

        kpiContent.add(kpiTitle);
        kpiContent.add(Box.createVerticalStrut(5));
        kpiContent.add(moduleCountLabel);

        kpiCard.add(kpiContent, BorderLayout.CENTER);

        body.add(kpiCard);
        body.add(Box.createVerticalStrut(20));


        // ==============================
        // COURSES TABLE
        // ==============================

        String[] columns = {
                "CODE",
                "COURSE NAME",
                "SEMESTER",
                "LECTURER"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        coursesTable = new JTable(tableModel);

        coursesTable.setRowHeight(35);

        coursesTable.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        coursesTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        coursesTable.setFillsViewportHeight(true);

        // Enable table sorting
        coursesTable.setAutoCreateRowSorter(true);

        coursesTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        coursesTable.getTableHeader().setBackground(
                new Color(235, 240, 237)
        );

        coursesTable.getTableHeader().setForeground(
                new Color(40, 60, 50)
        );

        JScrollPane scrollPane = new JScrollPane(coursesTable);

        scrollPane.setPreferredSize(
                new Dimension(750, 300)
        );


        // Table card
        JPanel tableCard = new JPanel(
                new BorderLayout(10, 10)
        );

        tableCard.setBackground(Color.WHITE);

        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 235)
                ),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel tableTitle = new JLabel(
                "My Registered Courses"
        );

        tableTitle.setFont(
                new Font("Arial", Font.BOLD, 17)
        );

        JLabel instruction = new JLabel(
                "Select a course to view its learning materials."
        );

        instruction.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        instruction.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel();

        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        titlePanel.setOpaque(false);

        titlePanel.add(tableTitle);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(instruction);

        tableCard.add(titlePanel, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        body.add(tableCard);

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

                JOptionPane.showMessageDialog(
                        this,
                        "You currently have no enrolled courses.",
                        "My Courses",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (DataAccessException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load your courses.\n" +
                            e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "An unexpected error occurred.\n" +
                            e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }


    // =====================================================
    // DISPLAY COURSE LEARNING MATERIALS
    // =====================================================

    private void showCourseMaterials(int offeringId) {

        try {

            // Get materials belonging to selected offering
            List<CourseMaterial> materials =
                    materialData.findByOffering(offeringId);

            if (materials == null || materials.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "No learning materials available for this course.",
                        "Course Materials",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }


            // Create list model
            DefaultListModel<CourseMaterial> listModel =
                    new DefaultListModel<>();

            for (CourseMaterial material : materials) {

                if (material != null) {
                    listModel.addElement(material);
                }
            }

            if (listModel.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "No learning materials available for this course.",
                        "Course Materials",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }


            // Create materials list
            JList<CourseMaterial> materialsList =
                    new JList<>(listModel);

            materialsList.setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION
            );

            materialsList.setFont(
                    new Font("Arial", Font.PLAIN, 13)
            );

            materialsList.setFixedCellHeight(35);


            // Custom list renderer
            materialsList.setCellRenderer(
                    (list, value, index, isSelected, cellHasFocus) -> {

                        String title = value.getTitle() != null
                                ? value.getTitle()
                                : "Untitled Material";

                        String type = value.getMaterial_type() != null
                                ? value.getMaterial_type()
                                : "File";

                        JLabel label = new JLabel(
                                title + " (" + type + ")"
                        );

                        label.setOpaque(true);

                        label.setBorder(
                                new EmptyBorder(5, 10, 5, 10)
                        );

                        if (isSelected) {

                            label.setBackground(
                                    new Color(220, 235, 225)
                            );

                        } else {

                            label.setBackground(Color.WHITE);
                        }

                        return label;
                    }
            );


            JScrollPane materialScrollPane =
                    new JScrollPane(materialsList);

            materialScrollPane.setPreferredSize(
                    new Dimension(450, 220)
            );


            // Show materials dialog
            int result = JOptionPane.showConfirmDialog(
                    this,
                    materialScrollPane,
                    "Course Learning Materials",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );


            // Open selected material
            if (result == JOptionPane.OK_OPTION) {

                CourseMaterial selectedMaterial =
                        materialsList.getSelectedValue();

                if (selectedMaterial != null) {

                    openMaterial(selectedMaterial);

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Please select a learning material.",
                            "Course Materials",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        } catch (DataAccessException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load course materials.\n" +
                            e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "An unexpected error occurred while loading materials.\n" +
                            e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

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

                    JOptionPane.showMessageDialog(
                            this,
                            "The material file could not be found.",
                            "File Not Found",
                            JOptionPane.WARNING_MESSAGE
                    );

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

                JOptionPane.showMessageDialog(
                        this,
                        "No file path or external link is available.",
                        "Material Unavailable",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to open the selected material.\n" +
                            e.getMessage(),
                    "Open Material Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}
