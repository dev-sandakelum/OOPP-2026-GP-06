package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Thin rounded scrollbar with no arrows and a transparent track, for scroll panes on glass cards.
public class GlassScrollBarUI extends BasicScrollBarUI {

    public static final int THICKNESS = 8;

    // Applies the thin scrollbar to both bars of a scroll pane.
    public static void install(JScrollPane sp) {
        for (JScrollBar bar : new JScrollBar[]{sp.getVerticalScrollBar(), sp.getHorizontalScrollBar()}) {
            bar.setUI(new GlassScrollBarUI());
            bar.setOpaque(false);
            bar.setUnitIncrement(16);
        }
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(THICKNESS, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, THICKNESS));
    }

    @Override
    protected void configureScrollBarColors() {
        thumbColor = LINE_2;
        trackColor = new Color(0, 0, 0, 0);
    }

    @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
    @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

    private static JButton zeroButton() {
        JButton b = new JButton();
        Dimension d = new Dimension(0, 0);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);
        b.setFocusable(false);
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = Glass.aa(g);
        g2.setColor(isThumbRollover() || isDragging ? BRAND_LT : LINE_2);
        g2.fill(new RoundRectangle2D.Float(r.x + 1, r.y + 1, r.width - 2, r.height - 2, THICKNESS, THICKNESS));
        g2.dispose();
    }
}
