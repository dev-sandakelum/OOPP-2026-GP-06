package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

import static UI.Kit.Palette.*;

// Icon inside a tinted shape.
//   SOFT    - light-blue circle, blue icon            (KPI cards on glass)
//   ON_DARK - translucent white circle, white icon    (KPI card on the blue card)
//   SOLID   - blue gradient rounded square, white icon (section headers)
public class IconBadge extends JComponent {

    public enum Style { SOFT, ON_DARK, SOLID }

    private final NavIcon icon;
    private final Style style;

    public IconBadge(NavIcon icon, Style style, int size) {
        this.icon = icon;
        this.style = style;
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        int s = Math.min(getWidth(), getHeight());
        int x = (getWidth() - s) / 2, y = (getHeight() - s) / 2;
        Color iconColor;

        switch (style) {
            case SOLID -> {
                Glass.brand(g2, x, y, s, s, Math.round(s * 0.28f));
                iconColor = ON_BRAND;
            }
            case ON_DARK -> {
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fill(new Ellipse2D.Float(x, y, s, s));
                g2.setColor(new Color(255, 255, 255, 110));
                g2.draw(new Ellipse2D.Float(x + 0.5f, y + 0.5f, s - 1, s - 1));
                iconColor = ON_BRAND;
            }
            default -> {
                g2.setColor(BRAND_SOFT);
                g2.fill(new Ellipse2D.Float(x, y, s, s));
                g2.setColor(GLASS_LINE);
                g2.draw(new Ellipse2D.Float(x + 0.5f, y + 0.5f, s - 1, s - 1));
                iconColor = BRAND;
            }
        }

        float scale = (s * 0.5f) / NavIcon.SIZE;
        int iconPx = Math.round(NavIcon.SIZE * scale);
        icon.draw(g2, x + (s - iconPx) / 2, y + (s - iconPx) / 2, iconColor, scale);
        g2.dispose();
    }
}
