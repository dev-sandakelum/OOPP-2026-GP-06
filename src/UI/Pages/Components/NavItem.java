package UI.Pages.Components;

import UI.Kit.FrameData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class NavItem extends JComponent implements FrameData {

    private static final int ITEM_HEIGHT = 44;
    private static final int PAD_LEFT    = 14;   // pill edge -> icon
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
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        Color fg = selected ? TEXT_ON : TEXT_OFF;

        // pill
        if (selected || hover) {
            g2.setColor(selected ? ACTIVE_BG : HOVER_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));
        }

        // icon
        icon.draw(g2, PAD_LEFT, (h - NavIcon.SIZE) / 2, fg);

        // label
        g2.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 15));
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        int tx = PAD_LEFT + NavIcon.SIZE + GAP;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}