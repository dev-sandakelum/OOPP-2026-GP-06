package UI.Pages.admin;

import UI.Pages.admin.AdminUi.Btn;
import UI.Pages.admin.AdminUi.Page;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Map;

import static UI.Pages.admin.AdminUi.*;

// Admin "User Profiles" tab: search, filter by role, add / edit / delete users.
public class UserProfiles extends Page {

    private static final String[] COLS = {"Name", "Role", "Email", "Contact"};
    private static final String[] ROLES = {"Administrator", "Lecturer", "Technical Officer", "Undergraduate"};

    // ---------- Placeholder data (swap for MockData / services later) ----------
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

    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table;
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
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        search.putClientProperty("JTextField.placeholderText", "Search name or email");
        search.getDocument().addDocumentListener(new SimpleDoc(() -> applyFilter(sorter)));
        roleFilter.addItem("All roles");
        for (String r : ROLES) roleFilter.addItem(r);
        roleFilter.addActionListener(e -> applyFilter(sorter));

        Btn add = new Btn("+ Add user", true);
        add.addActionListener(e -> addUser());
        Btn edit = new Btn("Edit", false);
        edit.addActionListener(e -> editUser());
        Btn del = new Btn("Delete", false);
        del.addActionListener(e -> deleteUser());

        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(search);
        left.add(roleFilter);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(edit);
        right.add(del);
        right.add(add);
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(bar, BorderLayout.NORTH);
        body.add(tableCard(table), BorderLayout.CENTER);
        setBody(body);
    }

    private void applyFilter(TableRowSorter<DefaultTableModel> sorter) {
        String q = search.getText().trim();
        String role = (String) roleFilter.getSelectedItem();
        java.util.List<RowFilter<Object, Object>> fs = new java.util.ArrayList<>();
        if (!q.isEmpty()) {
            fs.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q), 0, 2));
        }
        if (role != null && !role.equals("All roles")) {
            fs.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(role) + "$", 1));
        }
        sorter.setRowFilter(fs.isEmpty() ? null : RowFilter.andFilter(fs));
    }

    private String[] ask(String title, String[] initial) {
        JComboBox<String> role = new JComboBox<>(ROLES);
        if (initial != null) role.setSelectedItem(initial[1]);
        String[] v = form(this, title, new String[]{"Name", "Email", "Contact"},
                initial == null ? null : new String[]{initial[0], initial[2], initial[3]});
        if (v == null) return null;
        if (v[0].isEmpty() || v[1].isEmpty()) { info(this, "Name and email are required."); return null; }
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
        if (row < 0) { info(this, "Select a user first."); return; }
        int m = table.convertRowIndexToModel(row);
        String[] cur = new String[COLS.length];
        for (int i = 0; i < cur.length; i++) cur[i] = (String) model.getValueAt(m, i);
        String[] v = ask("Edit user", cur);
        if (v != null) for (int i = 0; i < v.length; i++) model.setValueAt(v[i], m, i);
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) { info(this, "Select a user first."); return; }
        int m = table.convertRowIndexToModel(row);
        if (confirm(this, "Delete " + model.getValueAt(m, 0) + "?")) model.removeRow(m);
    }

    // tiny DocumentListener that runs one callback
    static class SimpleDoc implements javax.swing.event.DocumentListener {
        private final Runnable r;
        SimpleDoc(Runnable r) { this.r = r; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }
}
