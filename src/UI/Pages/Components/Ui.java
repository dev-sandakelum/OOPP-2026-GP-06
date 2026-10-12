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

    // Table inside a rounded white card
    public static JComponent tableCard(JTable table) {
        Card c = new Card(false);
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane sp = new JScrollPane(table);
        sp.setColumnHeaderView(table.getTableHeader());   // so the header shows even before the window is displayed
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        c.add(sp, BorderLayout.CENTER);
        return c;
    }

    // Form dialog with one text field per label. Returns the values, or null if cancelled.
    public static String[] form(Component parent, String title, String[] labels, String[] initial) {
        JPanel p = new JPanel(new GridLayout(labels.length * 2, 1, 0, 4));
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            p.add(label(labels[i]));
            fields[i] = field(22);
            if (initial != null && initial[i] != null) fields[i].setText(initial[i]);
            p.add(fields[i]);
        }
        int r = JOptionPane.showConfirmDialog(parent, p, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;

        String[] out = new String[labels.length];
        for (int i = 0; i < out.length; i++) out[i] = fields[i].getText().trim();
        return out;
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }
}
