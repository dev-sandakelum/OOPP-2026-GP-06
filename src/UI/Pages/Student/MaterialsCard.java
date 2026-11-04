package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.Ui;
import data.CourseData;
import data.CourseMaterialData;
import data.CourseOfferingData;
import data.EnrollmentData;
import model.Course;
import model.CourseMaterial;
import model.CourseOffering;
import model.Enrollment;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.label;
import static UI.Pages.Components.Ui.table;
import static UI.Pages.Components.Ui.tableCard;

/**
 * Read-only lecture materials for a student: pick one of the enrolled courses,
 * then open or download the files the lecturer uploaded.
 */
public class MaterialsCard extends Card {

    private final JComboBox<String> courseBox = new JComboBox<>();
    private final DefaultTableModel model =
            new ReadOnlyTableModel(new String[]{"Title", "File Name", "Size", "Uploaded On"});
    private final JTable materialTable = table(model, null, -1);

    private final CourseMaterialData materialData = new CourseMaterialData();
    private List<CourseOffering> offerings = new ArrayList<>();
    private List<CourseMaterial> shown = new ArrayList<>();

    public MaterialsCard(User user) {
        super(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel heading = new JLabel("Lecture Materials");
        heading.setFont(font(Font.BOLD, 16));
        heading.setForeground(INK);

        courseBox.setFont(font(Font.PLAIN, 13));
        courseBox.setForeground(INK);
        courseBox.setBackground(SURFACE);
        courseBox.setPreferredSize(new Dimension(320, 38));

        RoundedButton openBtn = new RoundedButton("Open", true);
        RoundedButton saveBtn = new RoundedButton("Download", false);
        openBtn.addActionListener(e -> open());
        saveBtn.addActionListener(e -> download());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.add(label("COURSE"));
        bar.add(courseBox);
        bar.add(openBtn);
        bar.add(saveBtn);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(bar, BorderLayout.CENTER);

        materialTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) open();
            }
        });

        add(top, BorderLayout.NORTH);
        add(tableCard(materialTable), BorderLayout.CENTER);
        setPreferredSize(new Dimension(0, 460));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));

        loadCourses(user == null ? 0 : user.getUser_id());
        courseBox.addActionListener(e -> loadMaterials());
        loadMaterials();
    }

    private void loadCourses(int studentId) {
        try {
            CourseOfferingData offeringData = new CourseOfferingData();
            CourseData courseData = new CourseData();
            for (Enrollment en : new EnrollmentData().findByStudent(studentId)) {
                CourseOffering o = offeringData.findById(en.getOffering_id());
                if (o == null) continue;
                offerings.add(o);
                Course c = courseData.findById(o.getCourse_id());
                courseBox.addItem(c == null ? "Offering " + o.getOffering_id()
                        : c.getCourse_code() + " - " + c.getCourse_name());
            }
        } catch (Exception ex) {
            Ui.info(this, "Could not load your courses: " + ex.getMessage());
        }
    }

    private void loadMaterials() {
        model.setRowCount(0);
        shown = new ArrayList<>();
        int i = courseBox.getSelectedIndex();
        if (i < 0 || i >= offerings.size()) return;
        try {
            shown = materialData.findByOffering(offerings.get(i).getOffering_id());
        } catch (Exception ex) {
            Ui.info(this, "Could not load materials: " + ex.getMessage());
            return;
        }
        for (CourseMaterial m : shown) {
            String name = m.getFile_path() == null ? "" : new File(m.getFile_path()).getName();
            String size = m.getFile_size() == null ? "" : (m.getFile_size() / 1024) + " KB";
            String date = m.getUpload_date() == null ? "" : m.getUpload_date().toString().substring(0, 10);
            model.addRow(new Object[]{m.getTitle(), name, size, date});
        }
    }

    // Selected material whose file exists on disk (null + message otherwise)
    private File selectedFile() {
        int row = materialTable.getSelectedRow();
        if (row < 0) {
            Ui.info(this, "Please select a material first.");
            return null;
        }
        CourseMaterial m = shown.get(materialTable.convertRowIndexToModel(row));
        File f = m.getFile_path() == null ? null : new File(m.getFile_path());
        if (f == null || !f.exists()) {
            Ui.info(this, "The file is not available.");
            return null;
        }
        return f;
    }

    private void open() {
        File f = selectedFile();
        if (f == null) return;
        try {
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            Ui.info(this, "Could not open the file: " + ex.getMessage());
        }
    }

    private void download() {
        File f = selectedFile();
        if (f == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(f.getName()));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            Path target = chooser.getSelectedFile().toPath();
            Files.copy(f.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            Ui.info(this, "Saved to " + target);
        } catch (Exception ex) {
            Ui.info(this, "Could not save the file: " + ex.getMessage());
        }
    }
}
