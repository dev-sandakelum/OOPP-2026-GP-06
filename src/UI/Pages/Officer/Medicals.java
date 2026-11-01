package UI.Pages.Officer;

import Exception.DataAccessException;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.SimpleDocListener;
import data.MedicalData;
import data.UserData;
import model.Medical;
import model.User;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Officer "Medicals" tab: review student medical certificates.
// Loads from MedicalData, filters by status, and approves / rejects the selected PENDING row.
public class Medicals extends Page {

    private static final String[] COLS = {"Medical ID", "Student", "Student ID", "Date", "Reason", "Status"};
    private static final int STATUS_COL = 5;

    // label shown in the filter -> status stored in the database (null = all)
    private static final String[] FILTERS = {"All", "Pending", "Approved", "Rejected"};
    private static final String[] STATUSES = {"PENDING", "APPROVED", "REJECTED"};

    private final MedicalData medicalData = new MedicalData();
    private final UserData userData = new UserData();
    private final Map<Integer, String> nameCache = new HashMap<>();
    private final Runnable onChanged;   // lets the Overview refresh its pending count

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
    private final JTable table;
    private final TableRowSorter<ReadOnlyTableModel> sorter;
    private final JTextField search = field(18);
    private final JComboBox<String> statusFilter = new JComboBox<>(FILTERS);
    private final RoundedButton approveBtn = new RoundedButton("Approve", true);
    private final RoundedButton rejectBtn = new RoundedButton("Reject", false);

    public Medicals(Runnable onChanged) {
        super("Medicals", "Review student medical certificates", null);
        this.onChanged = onChanged == null ? () -> { } : onChanged;

        table = table(model, Map.of(
                "APPROVED", OK,
                "PENDING", WARN,
                "REJECTED", BAD
        ), STATUS_COL);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateButtons();
        });

        search.getDocument().addDocumentListener(new SimpleDocListener(this::applySearch));
        statusFilter.setSelectedItem("Pending");        // default view = pending medicals
        statusFilter.addActionListener(e -> load());

        approveBtn.addActionListener(e -> review("APPROVED"));
        rejectBtn.addActionListener(e -> review("REJECTED"));

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(toolbar(), BorderLayout.NORTH);
        body.add(tableCard(table), BorderLayout.CENTER);
        setBody(body);

        load();
    }

    private JComponent toolbar() {
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(search);
        left.add(statusFilter);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(rejectBtn);
        right.add(approveBtn);

        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ---------------------------------------------------------------- loading

    /** Reloads the table for the selected status filter. */
    private void load() {
        model.setRowCount(0);
        try {
            for (Medical m : fetch()) {
                model.addRow(new Object[]{
                        m.getMedicalId(),
                        studentName(m.getStudentId()),
                        m.getStudentId(),
                        m.getMedicalDate() == null ? "" : m.getMedicalDate().toString(),
                        m.getReason() == null ? "" : m.getReason(),
                        m.getStatus()
                });
            }
        } catch (DataAccessException ex) {
            info(this, "Failed to load medicals: " + ex.getMessage());
        }
        applySearch();
        updateButtons();
    }

    private List<Medical> fetch() {
        int pick = statusFilter.getSelectedIndex();
        if (pick > 0) {
            return medicalData.findByStatus(STATUSES[pick - 1]);
        }
        List<Medical> all = new ArrayList<>();
        for (String s : STATUSES) all.addAll(medicalData.findByStatus(s));
        all.sort((a, b) -> b.getMedicalDate().compareTo(a.getMedicalDate()));
        return all;
    }

    private String studentName(int studentId) {
        String cached = nameCache.get(studentId);
        if (cached != null) return cached;
        User u = userData.findById(studentId);
        String name = u == null ? "Student " + studentId : u.getName();
        nameCache.put(studentId, name);
        return name;
    }

    private void applySearch() {
        String q = search.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(q), 1, 2, 4));
    }

    // Approve / Reject only work on a selected PENDING row.
    private void updateButtons() {
        int row = table.getSelectedRow();
        boolean pending = row >= 0 && "PENDING".equals(
                model.getValueAt(table.convertRowIndexToModel(row), STATUS_COL));
        approveBtn.setEnabled(pending);
        rejectBtn.setEnabled(pending);
    }

    // ---------------------------------------------------------------- review

    private void review(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            info(this, "Select a pending medical first.");
            return;
        }
        int m = table.convertRowIndexToModel(row);
        int medicalId = (Integer) model.getValueAt(m, 0);
        String student = (String) model.getValueAt(m, 1);

        String verb = newStatus.equals("APPROVED") ? "Approve" : "Reject";
        String note = JOptionPane.showInputDialog(this,
                verb + " the medical of " + student + "?\nReviewer note:",
                verb + " medical", JOptionPane.PLAIN_MESSAGE);
        if (note == null) return;   // cancelled

        try {
            boolean ok = medicalData.updateStatus(medicalId, newStatus, note.trim(),
                    Date.valueOf(LocalDate.now()));
            if (!ok) {
                info(this, "No changes were saved — the medical record may no longer exist.");
            }
        } catch (DataAccessException ex) {
            info(this, "Failed to update medical status: " + ex.getMessage());
            return;
        }
        load();
        onChanged.run();
    }
}