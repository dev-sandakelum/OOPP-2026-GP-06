package UI.Pages.lecturer;

import UI.Pages.Components.Card;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static UI.Kit.Palette.*;

public class Medicals extends LecturerUI.Page {

    private static final Color HEADER_BG = new Color(0xF3F6F3);
    private static final Color ROW_LINE  = new Color(0xEEF2EF);
    private static final Color BORDER    = new Color(0xE0E8E2);

    // table columns
    private static final String[] COLUMNS =
            {"Ref", "Student", "Reg. No", "Period", "Affects", "Submitted", "Status"};
    private static final int STATUS_COL = 6;

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> statusFilter =
            new JComboBox<>(new String[]{"All statuses", "Pending", "Approved", "Rejected"});
    private final JLabel footer = new JLabel();

    public Medicals() {
        super("Medicals", "Review medical submissions", null);

        // read-only model: no cell can be edited
        model = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
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

        searchField.setFont(font(Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 12, 8, 12)));
        searchField.putClientProperty("JTextField.placeholderText", "Search by student, reg. no or ref");  // shows with FlatLaf
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        statusFilter.setFont(font(Font.PLAIN, 12));
        statusFilter.setPreferredSize(new Dimension(160, 36));
        statusFilter.addActionListener(e -> applyFilter());

        bar.add(searchField, BorderLayout.CENTER);
        bar.add(statusFilter, BorderLayout.EAST);
        return bar;
    }

    private JTable buildTable() {
        JTable t = new JTable(model);
        t.setFont(font(Font.PLAIN, 13));
        t.setForeground(INK);
        t.setRowHeight(46);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setRowSelectionAllowed(false);      // view only
        t.setCellSelectionEnabled(false);
        t.setFocusable(false);
        t.getTableHeader().setReorderingAllowed(false);

        // cells
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tb, v, false, false, r, c);
                setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, ROW_LINE), new EmptyBorder(0, 14, 0, 14)));
                setForeground(c == 1 ? INK : INK_2);
                setFont(font(c == 0 || c == 1 ? Font.BOLD : Font.PLAIN, 13));
                setBackground(Color.WHITE);
                return this;
            }
        };
        t.setDefaultRenderer(Object.class, cell);
        t.getColumnModel().getColumn(STATUS_COL).setCellRenderer(new StatusRenderer());

        // header
        JTableHeader h = t.getTableHeader();
        h.setPreferredSize(new Dimension(0, 40));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setText(String.valueOf(v).toUpperCase());
                l.setFont(font(Font.BOLD, 11));
                l.setForeground(INK_2);
                l.setBackground(HEADER_BG);
                l.setOpaque(true);
                l.setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 14, 0, 14)));
                return l;
            }
        });

        // column widths
        int[] w = {90, 190, 120, 150, 190, 100, 110};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        return t;
    }

    private JComponent buildTableCard() {
        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        footer.setFont(font(Font.PLAIN, 12));
        footer.setForeground(INK_2);
        footer.setBorder(new EmptyBorder(11, 14, 11, 14));
        JPanel foot = new JPanel(new BorderLayout());
        foot.setBackground(HEADER_BG);
        foot.add(footer, BorderLayout.WEST);

        card.add(table.getTableHeader(), BorderLayout.NORTH);
        card.add(table, BorderLayout.CENTER);
        card.add(foot, BorderLayout.SOUTH);
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
    /*  STATUS TAG                                                            */
    /* ===================================================================== */

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0)) {
                @Override public Dimension getPreferredSize() { return new Dimension(0, 46); }
            };
            wrap.setBackground(Color.WHITE);
            wrap.setBorder(new MatteBorder(0, 0, 1, 0, ROW_LINE));

            JPanel center = new JPanel(new GridBagLayout());   // vertically centre the pill
            center.setOpaque(false);
            center.setPreferredSize(new Dimension(90, 46));
            center.add(new Pill(String.valueOf(v)));
            wrap.add(center);
            return wrap;
        }
    }

    private static class Pill extends JLabel {
        private final Color bg;

        Pill(String status) {
            super(status, SwingConstants.CENTER);
            Color fg;
            switch (status) {
                case "Approved" -> { bg = new Color(0xE1F1E8); fg = new Color(0x2E7D5B); }
                case "Rejected" -> { bg = new Color(0xFBE6E3); fg = new Color(0xC0453A); }
                default         -> { bg = new Color(0xFBF1DC); fg = new Color(0xB97A14); }   // Pending
            }
            setOpaque(false);
            setForeground(fg);
            setFont(font(Font.BOLD, 11));
            setBorder(new EmptyBorder(4, 12, 4, 12));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ===================================================================== */

    private static JComponent topAligned(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.NORTH);
        return p;
    }
}