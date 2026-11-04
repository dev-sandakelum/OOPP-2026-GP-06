package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Rounded button. primary = royal-blue gradient, otherwise frosted white with an outline.
public class RoundedButton extends JButton {

    private final boolean primary;

    public RoundedButton(String text, boolean primary) {
        super(text);
        this.primary = primary;
        setFont(font(Font.BOLD, 13));
        setForeground(primary ? ON_BRAND : INK);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);   // lets paintComponent ask the model for hover
        setBorder(new EmptyBorder(10, 18, 12, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        boolean hover = getModel().isRollover();
        boolean down  = getModel().isArmed() && getModel().isPressed();
        Graphics2D g2 = Glass.aa(g);
        float x = 1, y = 0, w = getWidth() - 2, h = getHeight() - 4;
        int r = RADIUS_PILL - 4;
        if (primary) {
            Glass.shadow(g2, x, y, w, h, r);
            Glass.brand(g2, x, y, w, h, r);
            if (hover || down) {
                g2.setColor(new Color(255, 255, 255, down ? 0 : 40));
                if (down) g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new RoundRectangle2D.Float(x, y, w, h, r * 2, r * 2));
            }
        } else {
            RoundRectangle2D s = new RoundRectangle2D.Float(x, y, w, h, r * 2, r * 2);
            g2.setColor(hover ? BRAND_SOFT : GLASS_STRONG);
            g2.fill(s);
            g2.setColor(LINE_2);
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, r * 2, r * 2));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
