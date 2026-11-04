package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// One clickable slot of the timetable grid. Empty code = free slot.
public class TimetableCell extends JButton {

    private final String code;
    private final String room;

    public TimetableCell(String code, String room, Runnable onClick) {
        this.code = code;
        this.room = room;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setPreferredSize(new Dimension(100, 76));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(e -> onClick.run());
    }

    @Override
    protected void paintComponent(Graphics g) {
        boolean hover = getModel().isRollover();
        boolean filled = !code.isEmpty();

        Graphics2D g2 = Glass.aa(g);
        Shape shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 18, 18);

        if (filled) {
            g2.setColor(hover ? Glass.alpha(BRAND_LT, 80) : BRAND_SOFT);
            g2.fill(shape);
            g2.setColor(GLASS_LINE);
            g2.draw(shape);
        } else {
            g2.setColor(hover ? GLASS : GLASS_FAINT);
            g2.fill(shape);
            g2.setColor(LINE_2);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
            g2.draw(shape);
        }

        if (filled) {
            g2.setFont(font(Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(BRAND);
            g2.drawString(code, (getWidth() - fm.stringWidth(code)) / 2, getHeight() / 2 - 2);
            g2.setFont(font(Font.PLAIN, 12));
            fm = g2.getFontMetrics();
            g2.setColor(INK_2);
            g2.drawString(room, (getWidth() - fm.stringWidth(room)) / 2, getHeight() / 2 + 16);
        } else {
            g2.setFont(font(Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(INK_2);
            g2.drawString("Free", (getWidth() - fm.stringWidth("Free")) / 2, (getHeight() + fm.getAscent()) / 2 - 2);
        }
        g2.dispose();
    }
}
