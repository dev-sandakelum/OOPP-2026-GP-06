package UI.Pages.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Small rounded tag with a tinted background, e.g. "Staff".
public class Chip extends JLabel {

    private final Color color;

    public Chip(String text, Color color) {
        super(text, SwingConstants.CENTER);
        this.color = color;
        setFont(font(Font.BOLD, 11));
        setForeground(color);
        setBorder(new EmptyBorder(3, 10, 3, 10));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
        g2.dispose();
        super.paintComponent(g);
    }
}
