package UI.Kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Combo box in the glass style: rounded white field, blue outline when open or focused,
// chevron instead of the Metal arrow button, and a flat popup list with tinted selection.
// Installed for every JComboBox through Theme.install().
public class GlassComboBoxUI extends BasicComboBoxUI {

    private static final int RADIUS = 10;
    private static final int HEIGHT = 36;
    private static final int ARROW_WIDTH = 30;

    public static ComponentUI createUI(JComponent c) {
        return new GlassComboBoxUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        LookAndFeel.installProperty(comboBox, "opaque", Boolean.FALSE);
    }

    @Override
    protected ListCellRenderer<Object> createRenderer() {
        return new GlassRenderer();
    }

    @Override
    protected JButton createArrowButton() {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Glass.aa(g);
                g2.setColor(comboBox.isEnabled() ? BRAND : INK_2);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                float cx = getWidth() / 2f, cy = getHeight() / 2f;
                boolean open = comboBox.isPopupVisible();
                Path2D p = new Path2D.Float();
                if (open) {
                    p.moveTo(cx - 4.5f, cy + 2.5f); p.lineTo(cx, cy - 2); p.lineTo(cx + 4.5f, cy + 2.5f);
                } else {
                    p.moveTo(cx - 4.5f, cy - 2); p.lineTo(cx, cy + 2.5f); p.lineTo(cx + 4.5f, cy - 2);
                }
                g2.draw(p);
                g2.dispose();
            }
        };
        b.setName("ComboBox.arrowButton");
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setPreferredSize(new Dimension(ARROW_WIDTH, HEIGHT));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    @Override
    protected ComboPopup createPopup() {
        return new GlassPopup(comboBox);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        Dimension d = super.getMinimumSize(c);
        d.height = Math.max(d.height, HEIGHT);
        d.width += 8;
        return d;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = Glass.aa(g);
        float w = c.getWidth(), h = c.getHeight();
        boolean active = comboBox.isPopupVisible() || (comboBox.isEnabled() && comboBox.hasFocus());
        RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, w, h, RADIUS * 2, RADIUS * 2);
        g2.setColor(comboBox.isEnabled() ? SURFACE : SURFACE_2);
        g2.fill(shape);
        g2.setColor(active ? BRAND_LT : LINE_2);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, RADIUS * 2, RADIUS * 2));
        g2.dispose();

        hasFocus = comboBox.hasFocus();
        Rectangle r = rectangleForCurrentValue();
        paintCurrentValue(g, r, false);   // never the "focused" look: the outline shows focus instead
    }

    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        // the rounded field is painted in paint(); nothing to do here
    }

    // Popup list: 1px outline, no bevel, padded rows, thin scrollbar.
    private static final class GlassPopup extends BasicComboPopup {
        GlassPopup(JComboBox<Object> combo) {
            super(combo);
        }

        @Override
        protected void configurePopup() {
            super.configurePopup();
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE_2, 1), new EmptyBorder(4, 0, 4, 0)));
            setBackground(SURFACE);
            setOpaque(true);
        }

        @Override
        protected void configureList() {
            super.configureList();
            list.setBackground(SURFACE);
            list.setForeground(INK);
            list.setSelectionBackground(BRAND_SOFT);
            list.setSelectionForeground(INK);
            list.setFont(comboBox.getFont());
        }

        @Override
        protected void configureScroller() {
            super.configureScroller();
            scroller.setBorder(null);
            scroller.setBackground(SURFACE);
            scroller.getViewport().setBackground(SURFACE);
            // the popup has no horizontal bar (BasicComboPopup sets it to null), so style only the vertical one
            JScrollBar bar = scroller.getVerticalScrollBar();
            bar.setUI(new UI.Pages.Components.GlassScrollBarUI());
            bar.setOpaque(false);
            bar.setUnitIncrement(16);
            bar.setPreferredSize(new Dimension(UI.Pages.Components.GlassScrollBarUI.THICKNESS, 0));
        }

        @Override
        protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
            return super.computePopupBounds(px, py + 4, pw, ph);
        }
    }

    // Row renderer shared by the field and the list.
    private static final class GlassRenderer extends DefaultListCellRenderer implements UIResource {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, false);
            setBorder(new EmptyBorder(6, 10, 6, 8));
            setFont(list.getFont());
            setOpaque(index >= 0);   // transparent inside the field, filled rows in the list
            if (index < 0) {
                setForeground(list.isEnabled() ? INK : INK_2);
            } else {
                setBackground(isSelected ? BRAND_SOFT : SURFACE);
                setForeground(INK);
            }
            return this;
        }
    }
}
