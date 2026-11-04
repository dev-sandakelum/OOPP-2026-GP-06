package UI.Pages.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static UI.Kit.Palette.*;

// Table header cell: small bold muted text on a soft blue background.
public class HeaderRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean selected, boolean focus, int row, int col) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, col);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(INK_2);
        l.setBackground(SURFACE_2);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(0, 16, 0, 16));
        return l;
    }
}
