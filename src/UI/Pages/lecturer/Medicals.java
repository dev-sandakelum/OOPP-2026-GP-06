package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.ChipRenderer;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.TextCellRenderer;

import data.CourseOfferingData;
import data.EnrollmentData;
import data.MedicalData;
import data.UserData;
import model.CourseOffering;
import model.Enrollment;
import model.Medical;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.field;
import static UI.Pages.Components.Ui.table;

public class Medicals extends LecturerUI.Page {

    // hairline under every row
    private static final MatteBorder ROW_LINE = new MatteBorder(0, 0, 1, 0, LINE);

    // status chip colors (same palette as the admin tables)
    private static final Map<String, Color> STATUS_COLORS =
            Map.of("Pending", WARN, "Approved", OK, "Rejected", BAD);

    // table columns
    private static final String[] COLUMNS =
            {"Ref", "Student", "Reg. No", "Medical Date", "Reason", "Reviewed", "Status"};
    private static final int STATUS_COL = 6;

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField = field(20);
    private final JComboBox<String> statusFilter =
            new JComboBox<>(new String[]{"All statuses", "Pending", "Approved", "Rejected"});
    private final JLabel footer = new JLabel();

    // ---------- Data layers ----------
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final MedicalData medicalData = new MedicalData();
    private final UserData userData = new UserData();
    private final int lecturerId;

    public Medicals(User user) {
        super("Medicals", "Review medical submissions", null);
        this.lecturerId = user == null ? 0 : user.getUser_id();

        // read-only model: no cell can be edited
        model = new ReadOnlyTableModel(COLUMNS);
        table = buildTable();
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildToolbar(), BorderLayout.NORTH);
        body.add(topAligned(buildTableCard()), BorderLayout.CENTER);
        setScrollBody(body);

        loadMedicals();
    }

    /* ===================================================================== */
    /*  DATABASE                                                              */
    /* ===================================================================== */

    /** Loads the medicals of the students in the lecturer's courses (newest first). */
    private void loadMedicals() {
        // every student enrolled in one of my courses, each one only once
        Set<Integer> studentIds = new LinkedHashSet<>();
        for (CourseOffering o : offeringData.findByLecturer(lecturerId)) {
            for (Enrollment en : enrollmentData.findByOffering(o.getOffering_id())) {
                studentIds.add(en.getStudent_id());
            }
        }

        List<Medical> medicals = new ArrayList<>();
        for (int id : studentIds) medicals.addAll(medicalData.findByStudent(id));
        medicals.sort((a, b) -> b.getMedicalDate().compareTo(a.getMedicalDate()));

        // one table row per medical, in the COLUMNS order
        List<String[]> rows = new ArrayList<>();
        for (Medical m : medicals) {
            User student = userData.findById(m.getStudentId());
            rows.add(new String[]{
                    "MED-" + m.getMedicalId(),
                    student == null ? "" : student.getName(),
                    String.valueOf(m.getStudentId()),
                    String.valueOf(m.getMedicalDate()),
                    m.getReason() == null ? "" : m.getReason(),
                    m.getReviewedDate() == null ? "-" : m.getReviewedDate().toString(),
                    statusLabel(m.getStatus())});
        }
        setMedicals(rows);
    }

    /** "PENDING" -> "Pending" (matches the chip colours and the status filter). */
    private static String statusLabel(String status) {
        if (status == null || status.isEmpty()) return "Pending";
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }

    /** Fill the table with rows from the database (each row follows the COLUMNS order). */
    public void setMedicals(List<String[]> rows) {
        model.setRowCount(0);
        for (String[] r : rows) model.addRow(r);
        updateFooter();
    }

    /* ===================================================================== */
    /*  UI                                                                    */
    /* ===================================================================== */

    private JComponent buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        searchField.setFont(font(Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search by student, reg. no or ref");  // shows with FlatLaf
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        statusFilter.setFont(font(Font.PLAIN, 13));
        statusFilter.setForeground(INK);
        statusFilter.setBackground(SURFACE);
        statusFilter.setPreferredSize(new Dimension(170, 38));
        statusFilter.addActionListener(e -> applyFilter());

        bar.add(searchField, BorderLayout.CENTER);
        bar.add(statusFilter, BorderLayout.EAST);
        return bar;
    }

    private JTable buildTable() {
        // shared flat table: soft header, 44px rows, tinted selection, status chips
        JTable t = table(model, STATUS_COLORS, STATUS_COL);
        t.setFont(font(Font.PLAIN, 13));
        t.setFocusable(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // text cells: Ref + Student in bold ink, the rest muted, hairline between rows
        t.setDefaultRenderer(Object.class, new TextCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, sel, foc, r, c);
                l.setFont(font(c <= 1 ? Font.BOLD : Font.PLAIN, 13));
                l.setForeground(c <= 1 ? INK : INK_2);
                l.setBorder(BorderFactory.createCompoundBorder(ROW_LINE, l.getBorder()));
                return l;
            }
        });

        // status chips also get the row hairline
        ChipRenderer chips = new ChipRenderer(STATUS_COLORS);
        chips.setBorder(ROW_LINE);
        t.getColumnModel().getColumn(STATUS_COL).setCellRenderer(chips);

        // header bottom line
        t.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, LINE));

        // column widths
        int[] w = {90, 190, 120, 150, 190, 100, 110};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        return t;
    }

    private JComponent buildTableCard() {
        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(8, 8, 4, 8));   // keeps header/rows inside the rounded corners

        footer.setFont(font(Font.PLAIN, 12));
        footer.setForeground(INK_2);
        footer.setBorder(new EmptyBorder(10, 16, 6, 16));

        JPanel grid = new JPanel(new BorderLayout());
        grid.setOpaque(false);
        grid.add(table.getTableHeader(), BorderLayout.NORTH);
        grid.add(table, BorderLayout.CENTER);

        card.add(grid, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    /* ===================================================================== */
    /*  FILTER                                                                */
    /* ===================================================================== */

    private void applyFilter() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        String text = searchField.getText().trim();
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 0, 1, 2));
        }
        String status = (String) statusFilter.getSelectedItem();
        if (status != null && !status.startsWith("All")) {
            filters.add(RowFilter.regexFilter("^" + status + "$", STATUS_COL));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        updateFooter();
    }

    private void updateFooter() {
        footer.setText(table.getRowCount() + " of " + model.getRowCount() + " medical submissions");
    }

    /* ===================================================================== */

    private static JComponent topAligned(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.NORTH);
        return p;
    }
}