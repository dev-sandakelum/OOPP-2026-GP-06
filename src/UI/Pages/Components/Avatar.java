package UI.Pages.Components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

import static UI.Kit.Palette.*;

// Round avatar with the person's initials.
public class Avatar extends JComponent {

    private final String initials;

    public Avatar(String name, int size) {
        this.initials = initialsOf(name);
        setPreferredSize(new Dimension(size, size));
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
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, BRAND_LT.darker(), getWidth(), getHeight(), BRAND_DK));
        g2.fill(new Ellipse2D.Float(0, 0, getWidth() - 1, getHeight() - 1));
        g2.setColor(Color.WHITE);
        g2.setFont(font(Font.BOLD, getHeight() / 3f));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initials, (getWidth() - fm.stringWidth(initials)) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
