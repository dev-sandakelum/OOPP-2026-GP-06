package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

import static UI.Kit.Palette.*;

// Round blue avatar with the person's initials and a thin white ring.
public class Avatar extends JComponent {

    private final String initials;

    public Avatar(String name, int size) {
        this.initials = initialsOf(name);
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setOpaque(false);
    }

    private static String initialsOf(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        String s = parts[0].substring(0, 1);
        if (parts.length > 1) s += parts[parts.length - 1].substring(0, 1);
        return s.toUpperCase();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        int d = Math.min(getWidth(), getHeight());
        int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2;
        g2.setPaint(new GradientPaint(x, y, BRAND_LT, x + d, y + d, BRAND_DK));
        g2.fill(new Ellipse2D.Float(x, y, d, d));
        g2.setColor(new Color(255, 255, 255, 170));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new Ellipse2D.Float(x + 1.5f, y + 1.5f, d - 3, d - 3));
        g2.setColor(ON_BRAND);
        g2.setFont(font(Font.BOLD, d / 3f));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initials, x + (d - fm.stringWidth(initials)) / 2,
                y + (d - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
