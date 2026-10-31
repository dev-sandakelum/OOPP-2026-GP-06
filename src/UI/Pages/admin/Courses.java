package UI.Pages.admin;

import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

/**
 * Admin "Courses" tab: add / edit / delete courses.
 *
 * TODO (team task): wire loadAll(), addCourse(), editCourse(), deleteCourse()
 * to a CourseData / AdminData service once that is delivered.
 * See docs/issue-admin-ui-data-connect.md for the spec.
 */
public class Courses extends Page {

    private static final String[] COLS = {"Code", "Title", "Credits", "Practical", "Lecturer"};

    // ---------- Placeholder data (swap for services later) ----------
    private static final String[][] SEED = {
            {"TICT1113", "Programming Fundamentals",   "3", "Yes", "Dr. K. Perera"},
            {"TICT1123", "Database Systems",            "3", "Yes", "Dr. S. Fernando"},
            {"TICT1133", "Web Technologies",            "3", "Yes", "Dr. K. Perera"},
            {"TICT1142", "Mathematics for Technology",  "2", "No",  "Dr. S. Fernando"},
            {"TICT1152", "Communication Skills",        "2", "No",  "Dr. K. Perera"},
            {"TICT1162", "Computer Networks",           "2", "No",  "Dr. S. Fernando"}
    };
    // ---------- end placeholder data ----------

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
    private final JTable table;

    public Courses() {
        super("Courses", "Manage the courses offered this semester", null);

        for (String[] r : SEED) model.addRow(r);
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
    //  CRUD (in-memory until data layer is delivered)
    // -----------------------------------------------------------------

    private void addCourse() {
        String[] v = ask("Add course", null);
        if (v != null) model.addRow(v);
    }

    private void editCourse() {
        int row = table.getSelectedRow();
        if (row < 0) { info(this, "Select a course first."); return; }
        int m = table.convertRowIndexToModel(row);

        String[] current = new String[COLS.length];
        for (int i = 0; i < current.length; i++) current[i] = (String) model.getValueAt(m, i);

        String[] v = ask("Edit course", current);
        if (v != null) {
            for (int i = 0; i < v.length; i++) model.setValueAt(v[i], m, i);
        }
    }

    private void deleteCourse() {
        int row = table.getSelectedRow();
        if (row < 0) { info(this, "Select a course first."); return; }
        int m = table.convertRowIndexToModel(row);
        if (confirm(this, "Delete " + model.getValueAt(m, 0) + "?")) model.removeRow(m);
    }
}
