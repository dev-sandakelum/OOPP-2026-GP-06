package UI.Pages.Components;

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

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Shape shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.setColor(filled ? (hover ? new Color(206, 228, 215) : BRAND_SOFT) : (hover ? LINE : SURFACE_2));
        g2.fill(shape);

        if (!filled) {
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
