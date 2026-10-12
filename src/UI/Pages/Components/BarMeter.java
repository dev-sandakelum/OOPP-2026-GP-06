package UI.Pages.Components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Thin horizontal bar that is filled to a share between 0 and 1.
public class BarMeter extends JComponent {

    private final double share;

    public BarMeter(double share) {
        this.share = Math.max(0, Math.min(1, share));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = 8, y = (getHeight() - h) / 2;
        g2.setColor(SURFACE_2);
        g2.fill(new RoundRectangle2D.Float(0, y, getWidth(), h, h, h));
        g2.setColor(BRAND);
        g2.fill(new RoundRectangle2D.Float(0, y, (float) (getWidth() * share), h, h, h));
        g2.dispose();
    }
}
