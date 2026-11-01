package UI.Pages.admin;

import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.SimpleDocListener;
import data.UserData;
import model.User;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Admin "User Profiles" tab: search, filter by role, add / edit / delete users.
public class UserProfiles extends Page {

    private static final String[] COLS  = {"Name", "Role", "Email", "Contact", "Gender"};
    private static final String[] ROLES = {"Administrator", "Lecturer", "Technical Officer", "Undergraduate"};

    private final UserData userData = new UserData();

    // Parallel list keeps the user_id for each table row so we can call update/delete
    private final List<Integer> ids = new ArrayList<>();

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
    private final JTable table;
    private final TableRowSorter<ReadOnlyTableModel> sorter;
    private final JTextField search     = field(18);
    private final JComboBox<String> roleFilter = new JComboBox<>();

    public UserProfiles() {
        super("User Profiles", "Add, edit and remove accounts", null);

        table  = table(model, Map.of(
                "Administrator",    BRAND,
                "Lecturer",         OK,
                "Technical Officer", WARN,
                "Undergraduate",    INK_2), 1);
        sorter = new TableRowSorter<>(model);
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

        loadAll();
    }

    // -----------------------------------------------------------------
    //  Data loading
    // -----------------------------------------------------------------

    private void loadAll() {
        model.setRowCount(0);
        ids.clear();
        try {
            for (User u : userData.findAll()) {
                model.addRow(new String[]{
                        u.getName(),
                        roleLabel(u.getUserType()),
                        u.getEmail(),
                        u.getContact_number() == null ? "" : u.getContact_number(),
                        u.getGender() == null ? "" : u.getGender()
                });
                ids.add(u.getUser_id());
            }
        } catch (Exception ex) {
            info(this, "Could not load users: " + ex.getMessage());
        }
    }

    private static String roleLabel(String userType) {
        if (userType == null) return "";
        return switch (userType.toUpperCase()) {
            case "ADMIN"            -> "Administrator";
            case "LECTURER"         -> "Lecturer";
            case "TECHNICAL_OFFICER"-> "Technical Officer";
            case "UNDERGRADUATE"    -> "Undergraduate";
            default                 -> userType;
        };
    }

    private static String roleDbValue(String label) {
        return switch (label) {
            case "Administrator"    -> "ADMIN";
            case "Lecturer"         -> "LECTURER";
            case "Technical Officer"-> "TECHNICAL_OFFICER";
            case "Undergraduate"    -> "UNDERGRADUATE";
            default                 -> label.toUpperCase();
        };
    }

    // -----------------------------------------------------------------
    //  Toolbar & filter
    // -----------------------------------------------------------------

    private JComponent toolbar() {
        RoundedButton add  = new RoundedButton("+ Add user", true);
        add.addActionListener(e -> addUser());
        RoundedButton edit = new RoundedButton("Edit", false);
        edit.addActionListener(e -> editUser());
        RoundedButton del  = new RoundedButton("Delete", false);
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
        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void applyFilter() {
        String q    = search.getText().trim();
        String role = (String) roleFilter.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!q.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(q), 0, 2));
        }
        if (role != null && !role.equals("All roles")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(role) + "$", 1));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    // -----------------------------------------------------------------
    //  Ask dialog — returns {name, roleLabel, email, contact, gender} or null
    // -----------------------------------------------------------------

    private String[] ask(String title, String[] current) {
        String[] initial = current == null ? null
                : new String[]{current[0], current[2], current[3], current[4]};
        String[] v = form(this, title, new String[]{"Name", "Email", "Contact", "Gender (M/F/Other)"}, initial);
        if (v == null) return null;
        if (v[0].isEmpty() || v[1].isEmpty()) {
            info(this, "Name and email are required.");
            return null;
        }
        JComboBox<String> roleCb = new JComboBox<>(ROLES);
        if (current != null) roleCb.setSelectedItem(current[1]);
        int r = JOptionPane.showConfirmDialog(this, roleCb, "Role",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;
        return new String[]{v[0], (String) roleCb.getSelectedItem(), v[1], v[2], v[3]};
    }

    // -----------------------------------------------------------------
    //  CRUD actions
    // -----------------------------------------------------------------

    private void addUser() {
        String[] v = ask("Add user", null);
        if (v == null) return;

        // Ask for a password
        String[] pw = form(this, "Set password", new String[]{"Password"}, null);
        if (pw == null || pw[0].isEmpty()) {
            info(this, "A password is required.");
            return;
        }

        User u = new User();
        u.setName(v[0]);
        u.setEmail(v[2]);
        u.setContact_number(v[3]);
        u.setGender(v[4]);
        u.setPassword(pw[0]);           // store hash in production
        u.setUserType(roleDbValue(v[1]));
        
        // Photo will be auto-assigned by database trigger or can be set later
        u.setPhoto(null);

        try {
            userData.insert(u);
            model.addRow(new String[]{v[0], v[1], v[2], v[3], v[4]});
            ids.add(u.getUser_id());
        } catch (Exception ex) {
            info(this, "Could not add user: " + ex.getMessage());
        }
    }

    private void editUser() {
        int row = table.getSelectedRow();
        if (row < 0) { info(this, "Select a user first."); return; }
        int m = table.convertRowIndexToModel(row);

        String[] current = new String[COLS.length];
        for (int i = 0; i < current.length; i++) current[i] = (String) model.getValueAt(m, i);

        String[] v = ask("Edit user", current);
        if (v == null) return;

        int userId = ids.get(m);
        User u = new User();
        u.setUser_id(userId);
        u.setName(v[0]);
        u.setEmail(v[2]);
        u.setContact_number(v[3]);
        u.setGender(v[4]);
        u.setUserType(roleDbValue(v[1]));

        try {
            userData.update(u);
            for (int i = 0; i < v.length; i++) model.setValueAt(v[i], m, i);
        } catch (Exception ex) {
            info(this, "Could not update user: " + ex.getMessage());
        }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) { info(this, "Select a user first."); return; }
        int m = table.convertRowIndexToModel(row);

        if (!confirm(this, "Delete " + model.getValueAt(m, 0) + "?")) return;

        int userId = ids.get(m);
        try {
            userData.delete(userId);
            model.removeRow(m);
            ids.remove(m);
        } catch (Exception ex) {
            info(this, "Could not delete user: " + ex.getMessage());
        }
    }
}
