package UI.Pages.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

import static UI.Kit.Palette.*;

// Small static helpers: text field, label, table, dialogs.
public final class Ui {
    private Ui() { }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(font(Font.PLAIN, 14));
        f.setForeground(INK);
        f.setBackground(SURFACE);
        f.setCaretColor(BRAND);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_2, 1, true), new EmptyBorder(8, 12, 8, 12)));
        return f;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(INK_2);
        return l;
    }

    // Flat table: no grid, 44px rows, soft header, tinted selection.
    // chipColors / chipColumn: show that column as colored chips (pass null and -1 for none).
    public static JTable table(DefaultTableModel model, Map<String, Color> chipColors, int chipColumn) {
        JTable t = new JTable(model);
        t.setRowHeight(44);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setFont(font(Font.PLAIN, 14));
        t.setForeground(INK);
        t.setSelectionBackground(BRAND_SOFT);
        t.setSelectionForeground(INK);
        t.setOpaque(false);
        t.setAutoCreateRowSorter(true);

        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));
        t.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        t.setDefaultRenderer(Object.class, new TextCellRenderer());

        if (chipColors != null && chipColumn >= 0) {
            t.getColumnModel().getColumn(chipColumn).setCellRenderer(new ChipRenderer(chipColors));
        }
        return t;
    }

    // Table inside a rounded glass card
    public static JComponent tableCard(JTable table) {
        Card c = new Card(false);
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(10, 10, 14, 10));
        JScrollPane sp = new JScrollPane(table);
        sp.setColumnHeaderView(table.getTableHeader());   // so the header shows even before the window is displayed
        // a non-UIResource empty border stops JTable from re-applying the LAF's "Table.scrollPaneBorder"
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        GlassScrollBarUI.install(sp);
        c.add(sp, BorderLayout.CENTER);
        return c;
    }

    // -----------------------------------------------------------------
    //  Dialogs: every pop-up goes through GlassDialog (frosted wash over the window,
    //  one glass card, no title bar). Buttons are the only way to close them.
    // -----------------------------------------------------------------

    // Form dialog with one text field per label. Returns the values, or null if cancelled.
    public static String[] form(Component parent, String title, String[] labels, String[] initial) {
        JPanel p = new JPanel(new GridLayout(labels.length * 2, 1, 0, 4));
        p.setOpaque(false);
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            p.add(label(labels[i]));
            fields[i] = field(22);
            if (initial != null && initial[i] != null) fields[i].setText(initial[i]);
            p.add(fields[i]);
        }
        if (!ask(parent, title, p)) return null;

        String[] out = new String[labels.length];
        for (int i = 0; i < out.length; i++) out[i] = fields[i].getText().trim();
        return out;
    }

    // Any component with OK / Cancel under it. Returns true when OK was pressed.
    public static boolean ask(Component parent, String title, JComponent content) {
        JOptionPane pane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        return Integer.valueOf(JOptionPane.OK_OPTION).equals(GlassDialog.show(parent, title, pane));
    }

    // Yes / No question. Returns true for Yes.
    public static boolean confirm(Component parent, String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
        return Integer.valueOf(JOptionPane.YES_OPTION).equals(GlassDialog.show(parent, null, pane));
    }

    public static void info(Component parent, String message) {
        info(parent, null, message);
    }

    public static void info(Component parent, String title, String message) {
        GlassDialog.show(parent, title, new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE));
    }

    public static void warn(Component parent, String title, String message) {
        GlassDialog.show(parent, title, new JOptionPane(message, JOptionPane.WARNING_MESSAGE));
    }

    public static void error(Component parent, String message) {
        error(parent, null, message);
    }

    public static void error(Component parent, String title, String message) {
        GlassDialog.show(parent, title, new JOptionPane(message, JOptionPane.ERROR_MESSAGE));
    }
}
