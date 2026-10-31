package UI.Pages.lecturer;

import UI.Pages.Components.Card;
import UI.Pages.Components.ChipRenderer;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.TextCellRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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
            {"Ref", "Student", "Reg. No", "Period", "Affects", "Submitted", "Status"};
    private static final int STATUS_COL = 6;

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField = field(20);
    private final JComboBox<String> statusFilter =
            new JComboBox<>(new String[]{"All statuses", "Pending", "Approved", "Rejected"});
    private final JLabel footer = new JLabel();

    public Medicals() {
        super("Medicals", "Review medical submissions", null);

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

    /**
     * TODO (DATABASE): load the medical submissions here.
     *
     * Get the medical records for the students of the logged-in lecturer's courses from MySQL,
     * for example:
     *
     *   SELECT m.medical_id, s.name, s.reg_no, m.from_date, m.to_date,
     *          m.affected_assessment, m.submitted_date, m.status
     *   FROM medicals m
     *   JOIN students s ON s.student_id = m.student_id
     *   JOIN courses  c ON c.course_id  = m.course_id
     *   WHERE c.lecturer_id = ?
     *   ORDER BY m.submitted_date DESC;
     *
     * Then convert every row into a String[] in the SAME order as COLUMNS:
     *   { ref, studentName, regNo, "14 Oct – 18 Oct", affects, submittedDate, status }
     * and call setMedicals(list).
     *
     * (The mock data below is only for design preview. Delete it when the DB code is ready.)
     */
    private void loadMedicals() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"MED-0415", "Thisara Ranasinghe",  "CS/2021/024", "21 Oct – 23 Oct", "Lecture – CS3012",       "24 Oct", "Pending"});
        rows.add(new String[]{"MED-0412", "Nethmi Fernando",     "CS/2021/012", "14 Oct – 18 Oct", "Mid-Term – CS3012",      "20 Oct", "Pending"});
        rows.add(new String[]{"MED-0409", "Tharushi Silva",      "CS/2021/015", "12 Oct – 13 Oct", "Quiz 2 – CS3022",        "15 Oct", "Pending"});
        rows.add(new String[]{"MED-0401", "Ravindu Senanayake",  "CS/2021/018", "05 Oct – 09 Oct", "Assignment 1 – CS2042",  "10 Oct", "Approved"});
        rows.add(new String[]{"MED-0397", "Lakshan Bandara",     "CS/2021/020", "01 Oct – 02 Oct", "Lab 3 – CS3022",         "03 Oct", "Rejected"});
        rows.add(new String[]{"MED-0390", "Janith Kumara",       "CS/2021/023", "28 Sep – 30 Sep", "Mid-Term – CS4013",      "01 Oct", "Approved"});
        setMedicals(rows);
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