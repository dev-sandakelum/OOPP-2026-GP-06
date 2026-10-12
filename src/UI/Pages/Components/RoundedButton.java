package UI.Pages.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Rounded button. primary = dark green, otherwise white with an outline.
public class RoundedButton extends JButton {

    private final boolean primary;

    public RoundedButton(String text, boolean primary) {
        super(text);
        this.primary = primary;
        setFont(font(Font.BOLD, 13));
        setForeground(primary ? Color.WHITE : INK);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);   // lets paintComponent ask the model for hover
        setBorder(new EmptyBorder(10, 18, 10, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        boolean hover = getModel().isRollover();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape s = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        if (primary) {
            g2.setColor(hover ? BRAND_DK : BRAND);
            g2.fill(s);
        } else {
            g2.setColor(hover ? SURFACE_2 : SURFACE);
            g2.fill(s);
            g2.setColor(LINE_2);
            g2.draw(s);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
