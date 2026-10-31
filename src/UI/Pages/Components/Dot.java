package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

// Small colored status dot.
public class Dot extends JComponent {

    private final Color color;

    public Dot(Color color) {
        this.color = color;
        setPreferredSize(new Dimension(14, 22));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        g2.setColor(Glass.alpha(color, 50));
        g2.fill(new Ellipse2D.Float(0, 5, 13, 13));
        g2.setColor(color);
        g2.fill(new Ellipse2D.Float(2, 7, 9, 9));
        g2.dispose();
    }
}
