package UI.Pages.Officer;

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

// Technical Officer "Medicals" tab: view, filter by status, search, and approve/reject submissions.
public class Medicals extends Page {

    private static final String[] COLS = {
            "Submitted", "Undergraduate", "Leave Period", "Reason", "Sessions Affected", "Status"
    };

    private static final String[] STATUSES = {"All", "Pending", "Approved", "Rejected"};

    // ---------- Seed / Placeholder data ----------
    private static final String[][] SEED = {
            {"16 Sep", "W. A. N. Silva (FOT/2023/061)", "24 Aug – 15 Sep", "Migraine — clinic observation", "ICT2131-T#12, ICT2132-T#12, ICT2134-T#15", "Approved"},
            {"16 Sep", "P. L. Herath (FOT/2023/069)", "29 Jun – 15 Sep", "Viral fever", "ICT2134-T#08, ICT2135-T#04, ICT2136-T#09", "Approved"},
            {"16 Sep", "L. F. Ekanayake (FOT/2022/119)", "23 Jun – 15 Sep", "Fractured wrist — follow-up", "ICT2134-T#15, ICT2135-T#04, ICT2136-T#08", "Approved"},
            {"16 Sep", "C. W. Liyanage (FOT/2023/072)", "15 Jun – 15 Sep", "Dengue fever", "ICT2131-T#05, ICT2132-T#14, ICT2133-T#02", "Approved"},
            {"18 Sep", "K. D. Fernando (FOT/2023/044)", "10 Sep – 17 Sep", "Severe flu & fever", "ICT2131-T#10, ICT2133-T#06", "Pending"},
            {"19 Sep", "M. R. Perera (FOT/2023/088)", "12 Sep – 14 Sep", "Dental surgery", "ICT2135-P#02", "Pending"},
            {"20 Sep", "S. T. Wickrama (FOT/2022/051)", "01 Sep – 05 Sep", "Unverified doctor note", "ICT2132-P#04", "Rejected"}
    };
    // ---------- end placeholder data ----------

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
    private final JTable table;
    private final TableRowSorter<ReadOnlyTableModel> sorter;
    private final JTextField search = field(18);
    private final JComboBox<String> statusFilter = new JComboBox<>();

    public Medicals() {
        super("Medicals", "Review student medical submissions and update attendance status", null);

        for (String[] r : SEED) {
            model.addRow(r);
        }

        // Status column is at index 5
        Color DANGER = null;
        table = table(model, Map.of(
                "Approved", OK,
                "Pending", WARN,
                "Rejected", DANGER != null ? DANGER : INK_2
        ), 5);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        search.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));
        for (String s : STATUSES) {
            statusFilter.addItem(s);
        }
        statusFilter.addActionListener(e -> applyFilter());

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        // Header section: description note + toolbar
        JPanel northPanel = new JPanel(new BorderLayout(0, 10));
        northPanel.setOpaque(false);
        northPanel.add(createNoticeBanner(), BorderLayout.NORTH);
        northPanel.add(toolbar(), BorderLayout.SOUTH);

        body.add(northPanel, BorderLayout.NORTH);
        body.add(tableCard(table), BorderLayout.CENTER);

        setBody(body);
    }

    private JComponent createNoticeBanner() {
        JLabel notice = new JLabel("<html><i>Approved leave is flagged as medical in the register; rejected submissions count as absent. Decide on pending medicals below.</i></html>");
        notice.setFont(notice.getFont().deriveFont(12.5f));
        notice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(null, 2, 4),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return notice;
    }

    private JComponent toolbar() {
        RoundedButton approveBtn = new RoundedButton("Approve", true);
        approveBtn.addActionListener(e -> updateStatus("Approved"));

        RoundedButton rejectBtn = new RoundedButton("Reject", false);
        rejectBtn.addActionListener(e -> updateStatus("Rejected"));

        RoundedButton detailsBtn = new RoundedButton("View Details", false);
        detailsBtn.addActionListener(e -> showDetails());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(search);
        left.add(statusFilter);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(detailsBtn);
        right.add(rejectBtn);
        right.add(approveBtn);

        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void applyFilter() {
        String q = search.getText().trim();
        String status = (String) statusFilter.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!q.isEmpty()) {
            // Search across student info (1), reason (3), or sessions (4)
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(q), 1, 3, 4));
        }
        if (status != null && !status.equals("All")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(status) + "$", 5));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void updateStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            info(this, "Select a medical record first.");
            return;
        }
        int m = table.convertRowIndexToModel(row);
        String student = (String) model.getValueAt(m, 1);

        if (confirm(this, "Mark submission for " + student + " as " + newStatus + "?")) {
            model.setValueAt(newStatus, m, 5);
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            info(this, "Select a medical record first.");
            return;
        }
        int m = table.convertRowIndexToModel(row);

        String message = String.format(
                "Undergraduate: %s\nSubmission Date: %s\nLeave Period: %s\nReason: %s\nSessions: %s\nCurrent Status: %s",
                model.getValueAt(m, 1),
                model.getValueAt(m, 0),
                model.getValueAt(m, 2),
                model.getValueAt(m, 3),
                model.getValueAt(m, 4),
                model.getValueAt(m, 5)
        );
        info(this, "Medical Details", message);
    }
}