package UI.Pages.Components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

// Small colored status dot.
public class Dot extends JComponent {

    private final Color color;

    public Dot(Color color) {
        this.color = color;
        setPreferredSize(new Dimension(14, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fill(new Ellipse2D.Float(2, 6, 9, 9));
        g2.dispose();
    }
}
