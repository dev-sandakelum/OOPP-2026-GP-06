package UI.Pages.Components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Rounded card. White with a hairline, or dark green gradient when dark = true.
public class Card extends JPanel {

    private final boolean dark;

    public Card(boolean dark) {
        this.dark = dark;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape s = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 36, 36);
        if (dark) {
            g2.setPaint(new GradientPaint(0, 0, BRAND_LT.darker(), getWidth(), getHeight(), BRAND_DK));
            g2.fill(s);
        } else {
            g2.setColor(SURFACE);
            g2.fill(s);
            g2.setColor(LINE);
            g2.draw(s);
        }
        g2.dispose();
    }
}
