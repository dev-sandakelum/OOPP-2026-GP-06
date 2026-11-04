package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

import static UI.Kit.Palette.*;

// Table cell that shows its text as a colored chip. The color comes from the text via the map.
public class ChipRenderer extends JLabel implements TableCellRenderer {

    private final Map<String, Color> colors;
    private Color color = INK_2;
    private boolean selected;

    public ChipRenderer(Map<String, Color> colors) {
        this.colors = colors;
        setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean selected, boolean focus, int row, int col) {
        setText(String.valueOf(value));
        color = colors.getOrDefault(getText(), INK_2);
        this.selected = selected;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        if (selected) {
            g2.setColor(BRAND_SOFT);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.setFont(font(Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        int w = fm.stringWidth(text) + 20, h = 24, x = 16, y = (getHeight() - h) / 2;
        g2.setColor(Glass.alpha(color, 34));
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 12, 12));
        g2.setColor(color);
        g2.drawString(text, x + 10, y + (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
