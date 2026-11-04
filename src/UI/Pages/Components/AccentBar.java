package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Short royal-blue underline used beneath titles (page header, section header, logo).
public class AccentBar extends JComponent {

    public AccentBar(int width, int height) {
        Dimension d = new Dimension(width, height);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        g2.setPaint(new GradientPaint(0, 0, BRAND_LT, getWidth(), 0, BRAND));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
        g2.dispose();
    }
}
