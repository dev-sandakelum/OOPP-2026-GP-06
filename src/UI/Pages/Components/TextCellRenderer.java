package UI.Pages.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static UI.Kit.Palette.*;

// Normal table cell: padded text, soft green when selected.
public class TextCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean selected, boolean focus, int row, int col) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, selected, false, row, col);
        l.setBorder(new EmptyBorder(0, 16, 0, 16));
        l.setBackground(selected ? BRAND_SOFT : SURFACE);
        l.setOpaque(true);
        return l;
    }
}
