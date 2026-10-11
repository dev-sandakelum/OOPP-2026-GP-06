package UI.Pages.Components;

import UI.Kit.FrameData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

// Rounded card for the page content area. Same look as the Sidebar card.
// Usage: add(new ContentCard(content, PAGE_BG), BorderLayout.CENTER);
public class ContentCard extends JPanel implements FrameData {

    private static final int RADIUS = 28;   // keep equal to Sidebar.RADIUS

    private final Color mask;

    public ContentCard(JComponent content, Color pageBg) {
        super(new BorderLayout());
        this.mask = pageBg;
        setOpaque(false);
        content.setOpaque(false);
        add(content, BorderLayout.CENTER);
    }

    // wraps the card with the invisible gaps (top, left, bottom, right)
    public JPanel withGaps(Color pageBg) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(pageBg);
        wrap.setBorder(new EmptyBorder(12, 6, 12, 12));
        wrap.add(this, BorderLayout.CENTER);
        return wrap;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(SIDEBAR_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS * 2, RADIUS * 2));
        g2.dispose();
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        // hide the children's square corners with the gap color
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Area corners = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
        corners.subtract(new Area(new RoundRectangle2D.Float(
                0, 0, getWidth(), getHeight(), RADIUS * 2, RADIUS * 2)));
        g2.setColor(mask);
        g2.fill(corners);
        g2.dispose();
    }
}