package UI.Pages.Components;

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
    private Color background = SURFACE;

    public ChipRenderer(Map<String, Color> colors) {
        this.colors = colors;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean selected, boolean focus, int row, int col) {
        setText(String.valueOf(value));
        color = colors.getOrDefault(getText(), INK_2);
        background = selected ? BRAND_SOFT : SURFACE;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(background);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setFont(font(Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        int w = fm.stringWidth(text) + 20, h = 24, x = 16, y = (getHeight() - h) / 2;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 10, 10));
        g2.setColor(color);
        g2.drawString(text, x + 10, y + (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
