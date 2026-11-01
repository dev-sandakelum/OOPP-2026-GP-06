package UI.Kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// JOptionPane in the glass style: the same backdrop as the main windows (light-blue gradient
// plus the world-map art), a frosted glass card holding the message and the fields, a round
// badge icon, and the same pill buttons as RoundedButton (first button = royal blue, the rest
// frosted white). Installed through Theme.install(); plain JOptionPane.showXxx calls pick it up.
public class GlassOptionPaneUI extends BasicOptionPaneUI {

    // gap between the window edge and the glass card, and room under it for the card's shadow
    public static final int CARD_INSET = 14, CARD_SHADOW = 6;

    // padding inside the card (Theme adds CARD_INSET / CARD_SHADOW on top of this)
    public static final Insets CARD_PADDING = new Insets(22, 26, 20, 26);

    public static ComponentUI createUI(JComponent c) {
        return new GlassOptionPaneUI();
    }

    @Override
    protected void installComponents() {
        super.installComponents();
        clearPanels(optionPane);
    }

    // The message area is built from plain opaque JPanels; let the gradient show through them.
    private static void clearPanels(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JPanel p) p.setOpaque(false);
            if (child instanceof JLabel l && l.getText() != null) l.setForeground(INK);
            if (child instanceof Container inner) clearPanels(inner);
        }
    }

    @Override
    public void update(Graphics g, JComponent c) {
        Backdrop.paint(g, c);                       // gradient + world map, like the main windows
        Graphics2D g2 = Glass.aa(g);
        float x = CARD_INSET, y = CARD_INSET;
        float w = c.getWidth() - CARD_INSET * 2, h = c.getHeight() - CARD_INSET * 2 - CARD_SHADOW;
        Glass.shadow(g2, x, y, w, h, RADIUS_CARD);
        Glass.panel(g2, x, y, w, h, RADIUS_CARD, GLASS);
        g2.dispose();
        paint(g, c);
    }

    @Override
    protected void addButtonComponents(Container container, Object[] buttons, int initialIndex) {
        super.addButtonComponents(container, buttons, initialIndex);
        if (container instanceof JComponent jc) jc.setOpaque(false);
        int seen = 0;
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton b) {
                boolean primary = seen == Math.max(0, initialIndex);
                b.setUI(new PillButtonUI(primary));
                b.setFont(font(Font.BOLD, 13));
                b.setForeground(primary ? ON_BRAND : INK);
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setOpaque(false);
                b.setRolloverEnabled(true);
                b.setBorder(new EmptyBorder(9, 22, 11, 22));
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                seen++;
            }
        }
    }

    // Paints the RoundedButton look on a button JOptionPane has already created.
    private static final class PillButtonUI extends BasicButtonUI {
        private final boolean primary;

        PillButtonUI(boolean primary) {
            this.primary = primary;
        }

        @Override
        public Dimension getMinimumSize(JComponent c) {
            Dimension d = super.getPreferredSize(c);
            d.width = Math.max(d.width, 88);
            return d;
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return getMinimumSize(c);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            boolean hover = b.getModel().isRollover();
            boolean down  = b.getModel().isArmed() && b.getModel().isPressed();
            Graphics2D g2 = Glass.aa(g);
            float x = 1, y = 0, w = c.getWidth() - 2, h = c.getHeight() - 4;
            int r = RADIUS_PILL - 4;
            if (primary) {
                Glass.shadow(g2, x, y, w, h, r);
                Glass.brand(g2, x, y, w, h, r);
                if (hover || down) {
                    g2.setColor(down ? new Color(0, 0, 0, 40) : new Color(255, 255, 255, 40));
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
            super.paint(g, c);
        }

        @Override
        protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
            Graphics2D g2 = Glass.aa(g);
            FontMetrics fm = g2.getFontMetrics(b.getFont());
            g2.setColor(b.getForeground());
            g2.setFont(b.getFont());
            int tx = textRect.x + (textRect.width - fm.stringWidth(text)) / 2;
            int ty = textRect.y + (textRect.height - fm.getHeight()) / 2 + fm.getAscent() - 2;
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }
}
