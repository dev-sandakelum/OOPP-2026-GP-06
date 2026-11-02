package UI.Pages.admin;

import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import data.CourseData;
import data.CourseOfferingData;
import model.Course;
import model.CourseOffering;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

/**
 * Admin "Courses" tab: add / edit / delete courses.
 */
public class Courses extends Page {

    private static final String[] COLS = {"Code", "Title", "Credits", "Practical", "Lecturer"};

    private final CourseData courseData = new CourseData();
    private final CourseOfferingData offeringData = new CourseOfferingData();

    // Parallel list keeps the course_id for each table row so we can call update/delete
    private final List<Integer> ids = new ArrayList<>();

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
    private final JTable table;

    public Courses() {
        super("Courses", "Manage the courses offered this semester", null);

        table = table(model, Map.of("Yes", OK, "No", INK_2), 3);

        RoundedButton add  = new RoundedButton("+ Add course", true);
        add.addActionListener(e -> addCourse());
        RoundedButton edit = new RoundedButton("Edit", false);
        edit.addActionListener(e -> editCourse());
        RoundedButton del  = new RoundedButton("Delete", false);
        del.addActionListener(e -> deleteCourse());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(edit);
        buttons.add(del);
        buttons.add(add);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(buttons, BorderLayout.NORTH);
        body.add(tableCard(table), BorderLayout.CENTER);
        setBody(body);

        loadAll();
    }

    // -----------------------------------------------------------------
    //  Data loading
    // -----------------------------------------------------------------

    private void loadAll() {
        model.setRowCount(0);
        ids.clear();
        try {
            // Build a map of course_id -> lecturer name from offerings
            Map<Integer, String> lecturerMap = new HashMap<>();
            for (CourseOffering offering : offeringData.findAll()) {
                String lecturer = offering.getLecturer_id() != null 
                    ? "Lecturer " + offering.getLecturer_id() 
                    : "Unassigned";
                lecturerMap.put(offering.getCourse_id(), lecturer);
            }

            for (Course c : courseData.findAll()) {
                // Check if it's a practical course (you can adjust logic based on your schema)
                String practical = "No";  // Default
                String lecturer = lecturerMap.getOrDefault(c.getCourse_id(), "Unassigned");
                
                model.addRow(new String[]{
                        c.getCourse_code(),
                        c.getCourse_name(),
                        String.valueOf((int) c.getCredit()),
                        practical,
                        lecturer
                });
                ids.add(c.getCourse_id());
            }
        } catch (Exception ex) {
            info(this, "Could not load courses: " + ex.getMessage());
        }
    }

    // -----------------------------------------------------------------
    //  Ask dialog — returns {code, title, credits, practical, lecturer}
    // -----------------------------------------------------------------

    private String[] ask(String title, String[] current) {
        String[] labels = {"Code", "Title", "Credits (number)", "Practical (Yes / No)", "Lecturer"};
        String[] v = form(this, title, labels, current);
        if (v == null) return null;

        if (v[0].isEmpty() || v[1].isEmpty()) {
            info(this, "Code and title are required.");
            return null;
        }
        try {
            if (Integer.parseInt(v[2]) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            info(this, "Credits must be a positive number.");
            return null;
        }
        boolean yes = v[3].equalsIgnoreCase("yes") || v[3].equalsIgnoreCase("y");
        v[3] = yes ? "Yes" : "No";
        return v;
    }

    // -----------------------------------------------------------------
    //  CRUD actions
    // -----------------------------------------------------------------

    private void addCourse() {
        String[] v = ask("Add course", null);
        if (v == null) return;

        Course c = new Course();
        c.setCourse_code(v[0]);
        c.setCourse_name(v[1]);
        c.setCredit(Double.parseDouble(v[2]));
        // dep_id can be set if needed; leaving null for now

        try {
            courseData.insert(c);
            model.addRow(v);
            ids.add(c.getCourse_id());
        } catch (Exception ex) {
            info(this, "Could not add course: " + ex.getMessage());
        }
    }

    private void editCourse() {
        int row = table.getSelectedRow();
        if (row < 0) { info(this, "Select a course first."); return; }
        int m = table.convertRowIndexToModel(row);

        String[] current = new String[COLS.length];
        for (int i = 0; i < current.length; i++) current[i] = (String) model.getValueAt(m, i);

        String[] v = ask("Edit course", current);
        if (v == null) return;

        int courseId = ids.get(m);
        Course c = new Course();
        c.setCourse_id(courseId);
        c.setCourse_code(v[0]);
        c.setCourse_name(v[1]);
        c.setCredit(Double.parseDouble(v[2]));

        try {
            courseData.update(c);
            for (int i = 0; i < v.length; i++) model.setValueAt(v[i], m, i);
        } catch (Exception ex) {
            info(this, "Could not update course: " + ex.getMessage());
        }
    }

    private void deleteCourse() {
        int row = table.getSelectedRow();
        if (row < 0) { info(this, "Select a course first."); return; }
        int m = table.convertRowIndexToModel(row);

        if (!confirm(this, "Delete " + model.getValueAt(m, 0) + "?")) return;

        int courseId = ids.get(m);
        try {
            courseData.delete(courseId);
            model.removeRow(m);
            ids.remove(m);
        } catch (Exception ex) {
            info(this, "Could not delete course: " + ex.getMessage());
        }
    }
}
