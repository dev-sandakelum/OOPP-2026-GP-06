package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Rounded horizontal bar filled to a share between 0 and 1.
public class BarMeter extends JComponent {

    private final double share;

    public BarMeter(double share) {
        this.share = Math.max(0, Math.min(1, share));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        int h = 10, y = (getHeight() - h) / 2;
        g2.setColor(BRAND_SOFT);
        g2.fill(new RoundRectangle2D.Float(0, y, getWidth(), h, h, h));
        float fw = (float) (getWidth() * share);
        if (fw > 0) {
            g2.setPaint(new GradientPaint(0, 0, BRAND_DK, Math.max(1, fw), 0, BRAND));
            g2.fill(new RoundRectangle2D.Float(0, y, Math.max(fw, h), h, h, h));
        }
        g2.dispose();
    }
}
