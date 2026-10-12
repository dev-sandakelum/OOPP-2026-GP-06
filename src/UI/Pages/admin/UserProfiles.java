package UI.Pages.admin;

import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.SimpleDocListener;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Admin "User Profiles" tab: search, filter by role, add / edit / delete users.
public class UserProfiles extends Page {

    private static final String[] COLS = {"Name", "Role", "Email", "Contact"};
    private static final String[] ROLES = {"Administrator", "Lecturer", "Technical Officer", "Undergraduate"};

    // ---------- Placeholder data (swap for services later) ----------
    private static final String[][] SEED = {
            {"Mrs. N. P. Jayasuriya", "Administrator", "admin@fot.ruh.ac.lk", "0771000001"},
            {"Dr. K. Perera", "Lecturer", "kperera@fot.ruh.ac.lk", "0771000002"},
            {"Dr. S. Fernando", "Lecturer", "sfernando@fot.ruh.ac.lk", "0771000003"},
            {"Mr. R. Silva", "Technical Officer", "rsilva@fot.ruh.ac.lk", "0771000004"},
            {"Ms. T. Wickrama", "Technical Officer", "twickrama@fot.ruh.ac.lk", "0771000005"},
            {"A. B. Kumara", "Undergraduate", "kumara@std.ruh.ac.lk", "0771000006"},
            {"C. D. Nadeesha", "Undergraduate", "nadeesha@std.ruh.ac.lk", "0771000007"},
            {"E. F. Rathnayake", "Undergraduate", "rathnayake@std.ruh.ac.lk", "0771000008"}
    };
    // ---------- end placeholder data ----------

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
    private final JTable table;
    private final TableRowSorter<ReadOnlyTableModel> sorter;
    private final JTextField search = field(18);
    private final JComboBox<String> roleFilter = new JComboBox<>();

    public UserProfiles() {
        super("User Profiles", "Add, edit and remove accounts", null);

        for (String[] r : SEED) model.addRow(r);

        table = table(model, Map.of(
                "Administrator", BRAND,
                "Lecturer", OK,
                "Technical Officer", WARN,
                "Undergraduate", INK_2), 1);
        sorter = new TableRowSorter<>(model);   // created after the rows are added
        table.setRowSorter(sorter);

        search.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));
        roleFilter.addItem("All roles");
        for (String r : ROLES) roleFilter.addItem(r);
        roleFilter.addActionListener(e -> applyFilter());

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(toolbar(), BorderLayout.NORTH);
        body.add(tableCard(table), BorderLayout.CENTER);
        setBody(body);
    }

    private JComponent toolbar() {
        RoundedButton add = new RoundedButton("+ Add user", true);
        add.addActionListener(e -> addUser());
        RoundedButton edit = new RoundedButton("Edit", false);
        edit.addActionListener(e -> editUser());
        RoundedButton del = new RoundedButton("Delete", false);
        del.addActionListener(e -> deleteUser());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(search);
        left.add(roleFilter);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(edit);
        right.add(del);
        right.add(add);

        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void applyFilter() {
        String q = search.getText().trim();
        String role = (String) roleFilter.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!q.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(q), 0, 2));   // name or email
        }
        if (role != null && !role.equals("All roles")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(role) + "$", 1));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    // asks for the details, returns {name, role, email, contact} or null if cancelled
    private String[] ask(String title, String[] current) {
        String[] initial = current == null ? null : new String[]{current[0], current[2], current[3]};
        String[] v = form(this, title, new String[]{"Name", "Email", "Contact"}, initial);
        if (v == null) return null;
        if (v[0].isEmpty() || v[1].isEmpty()) {
            info(this, "Name and email are required.");
            return null;
        }

        JComboBox<String> role = new JComboBox<>(ROLES);
        if (current != null) role.setSelectedItem(current[1]);
        int r = JOptionPane.showConfirmDialog(this, role, "Role",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;

        return new String[]{v[0], (String) role.getSelectedItem(), v[1], v[2]};
    }

    private void addUser() {
        String[] v = ask("Add user", null);
        if (v != null) model.addRow(v);
    }

    private void editUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            info(this, "Select a user first.");
            return;
        }
        int m = table.convertRowIndexToModel(row);
        String[] current = new String[COLS.length];
        for (int i = 0; i < current.length; i++) current[i] = (String) model.getValueAt(m, i);

        String[] v = ask("Edit user", current);
        if (v != null) {
            for (int i = 0; i < v.length; i++) model.setValueAt(v[i], m, i);
        }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            info(this, "Select a user first.");
            return;
        }
        int m = table.convertRowIndexToModel(row);
        if (confirm(this, "Delete " + model.getValueAt(m, 0) + "?")) model.removeRow(m);
    }
}
