package UI.Pages.admin;

import UI.Pages.admin.AdminUi.Btn;
import UI.Pages.admin.AdminUi.Page;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

import static UI.Pages.admin.AdminUi.*;

// Admin "Courses" tab: add / edit / delete courses.
public class Courses extends Page {

    private static final String[] COLS = {"Code", "Title", "Credits", "Practical", "Lecturer"};

    // ---------- Placeholder data (swap for MockData / services later) ----------
    private static final String[][] SEED = {
            {"TICT1113", "Programming Fundamentals", "3", "Yes", "Dr. K. Perera"},
            {"TICT1123", "Database Systems", "3", "Yes", "Dr. S. Fernando"},
            {"TICT1133", "Web Technologies", "3", "Yes", "Dr. K. Perera"},
            {"TICT1142", "Mathematics for Technology", "2", "No", "Dr. S. Fernando"},
            {"TICT1152", "Communication Skills", "2", "No", "Dr. K. Perera"},
            {"TICT1162", "Computer Networks", "2", "No", "Dr. S. Fernando"}
    };
    // ---------- end placeholder data ----------

    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table;

    public Courses() {
        super("Courses", "Manage the courses offered this semester", null);
        for (String[] r : SEED) model.addRow(r);
        table = table(model, Map.of("Yes", OK, "No", INK_2), 3);

        Btn add = new Btn("+ Add course", true);
        add.addActionListener(e -> addCourse());
        Btn edit = new Btn("Edit", false);
        edit.addActionListener(e -> editCourse());
        Btn del = new Btn("Delete", false);
        del.addActionListener(e -> deleteCourse());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(edit);
        right.add(del);
        right.add(add);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(right, BorderLayout.NORTH);
        body.add(tableCard(table), BorderLayout.CENTER);
        setBody(body);
    }

    private String[] ask(String title, String[] initial) {
        String[] v = form(this, title, new String[]{"Code", "Title", "Credits (number)", "Practical (Yes / No)", "Lecturer"}, initial);
        if (v == null) return null;
        if (v[0].isEmpty() || v[1].isEmpty()) { info(this, "Code and title are required."); return null; }
        try {
            if (Integer.parseInt(v[2]) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) { info(this, "Credits must be a positive number."); return null; }
        v[3] = v[3].equalsIgnoreCase("yes") || v[3].equalsIgnoreCase("y") ? "Yes" : "No";
        return v;
    }

    private void addCourse() {
        String[] v = ask("Add course", null);
        if (v != null) model.addRow(v);
    }

    private void editCourse() {
        int row = table.getSelectedRow();
        if (row < 0) { info(this, "Select a course first."); return; }
        int m = table.convertRowIndexToModel(row);
        String[] cur = new String[COLS.length];
        for (int i = 0; i < cur.length; i++) cur[i] = (String) model.getValueAt(m, i);
        String[] v = ask("Edit course", cur);
        if (v != null) for (int i = 0; i < v.length; i++) model.setValueAt(v[i], m, i);
    }

    private void deleteCourse() {
        int row = table.getSelectedRow();
        if (row < 0) { info(this, "Select a course first."); return; }
        int m = table.convertRowIndexToModel(row);
        if (confirm(this, "Delete " + model.getValueAt(m, 0) + "?")) model.removeRow(m);
    }
}
