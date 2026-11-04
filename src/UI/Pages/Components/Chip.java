package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Small rounded tag with a tinted background, e.g. "Staff".
public class Chip extends JLabel {

    private final Color color;

    public Chip(String text, Color color) {
        this(text, color, false);
    }

    /** withDot = show a small painted status dot before the text (e.g. "● Eligible"). */
    public Chip(String text, Color color, boolean withDot) {
        super(text, SwingConstants.CENTER);
        this.color = color;
        setFont(font(Font.BOLD, 11));
        setForeground(color);
        setBorder(new EmptyBorder(3, 10, 3, 10));
        setOpaque(false);
        if (withDot) {
            setIcon(ShapeIcon.dot(color, 7));
            setIconTextGap(6);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        g2.setColor(Glass.alpha(color, 34));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        g2.dispose();
        super.paintComponent(g);
    }
}
