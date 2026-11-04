package UI.Pages.Components;

import UI.Kit.Backdrop;
import UI.Kit.FrameData;
import UI.Kit.Glass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static UI.Kit.Palette.*;

// Faint glass panel for the page content area, floating on the window Backdrop.
// Usage: add(new ContentCard(content, PAGE_BG).withGaps(PAGE_BG), BorderLayout.CENTER);
public class ContentCard extends JPanel implements FrameData {

    private static final int RADIUS = RADIUS_PANEL;   // keep equal to Sidebar.RADIUS

    public ContentCard(JComponent content, Color pageBg) {
        super(new BorderLayout());
        setOpaque(false);
        content.setOpaque(false);
        add(content, BorderLayout.CENTER);
    }

    // wraps the card with the gaps (top, left, bottom, right); the wrapper paints the Backdrop
    public JPanel withGaps(Color pageBg) {
        JPanel wrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) { Backdrop.paint(g, this); }
        };
        wrap.setOpaque(true);
        wrap.setBackground(pageBg);
        wrap.setBorder(new EmptyBorder(12, 6, 12, 12));
        wrap.add(this, BorderLayout.CENTER);
        return wrap;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        Glass.panel(g2, 0, 0, getWidth(), getHeight(), RADIUS, GLASS_FAINT);
        g2.dispose();
    }
}
