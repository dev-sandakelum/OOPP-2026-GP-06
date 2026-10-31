package UI.Pages.Components;

import UI.Kit.FrameData;
import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// One row of the sidebar nav: icon + label inside a soft blue pill when selected.
public class NavItem extends JComponent implements FrameData {

    private static final int ITEM_HEIGHT = 46;
    private static final int PAD_LEFT    = 18;   // pill edge -> icon
    private static final int GAP         = 14;   // icon -> label

    private final String text;
    private final NavIcon icon;
    private boolean selected;
    private boolean hover;

    public NavItem(String text, NavIcon icon) {
        this.text = text;
        this.icon = icon;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(0, ITEM_HEIGHT));
        setMinimumSize(new Dimension(0, ITEM_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, ITEM_HEIGHT));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
        });
    }

    public void setSelected(boolean s) {
        if (selected != s) {
            selected = s;
            repaint();
        }
    }

    public boolean isSelected() { return selected; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);

        int w = getWidth(), h = getHeight();
        Color fg = selected ? TEXT_ON : TEXT_OFF;

        // pill
        if (selected || hover) {
            RoundRectangle2D pill = new RoundRectangle2D.Float(0, 0, w, h, RADIUS_PILL * 2, RADIUS_PILL * 2);
            g2.setColor(selected ? ACTIVE_BG : HOVER_BG);
            g2.fill(pill);
            g2.setColor(GLASS_LINE);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, RADIUS_PILL * 2, RADIUS_PILL * 2));
        }

        // icon
        icon.draw(g2, PAD_LEFT, (h - NavIcon.SIZE) / 2, fg);

        // label
        g2.setFont(font(selected ? Font.BOLD : Font.PLAIN, 15));
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        int tx = PAD_LEFT + NavIcon.SIZE + GAP;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}
