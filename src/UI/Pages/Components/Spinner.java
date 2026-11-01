package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

import static UI.Kit.Palette.*;

// Round progress indicator: a faint track with a royal-blue arc that spins while the
// component is showing. Call setDone(true) to swap the arc for a green tick.
public class Spinner extends JComponent {

    private static final int FRAME_MS = 16;      // ~60 fps

    private final Timer timer = new Timer(FRAME_MS, e -> tick());
    private final int size;
    private float angle;                         // start angle of the arc, degrees
    private boolean done;

    public Spinner(int size) {
        this.size = size;
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setOpaque(false);
    }

    /** Switches between the spinning arc and the finished tick. */
    public void setDone(boolean done) {
        this.done = done;
        repaint();
    }

    private void tick() {
        angle = (angle + 7f) % 360f;
        repaint();
    }

    // Spin only while visible, so hidden spinners cost nothing.
    @Override
    public void addNotify() {
        super.addNotify();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        float stroke = Math.max(3f, size / 11f);
        float x = (getWidth() - size) / 2f + stroke, y = (getHeight() - size) / 2f + stroke;
        float d = size - stroke * 2;
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (done) {
            g2.setColor(Glass.alpha(OK, 40));
            g2.fill(new Ellipse2D.Float(x, y, d, d));
            g2.setColor(OK);
            g2.draw(new Ellipse2D.Float(x, y, d, d));
            // tick mark
            float cx = x + d / 2, cy = y + d / 2, u = d / 10;
            g2.drawLine(Math.round(cx - 2.6f * u), Math.round(cy),
                        Math.round(cx - 0.6f * u), Math.round(cy + 2f * u));
            g2.drawLine(Math.round(cx - 0.6f * u), Math.round(cy + 2f * u),
                        Math.round(cx + 3f * u),   Math.round(cy - 2.2f * u));
        } else {
            g2.setColor(BRAND_SOFT);
            g2.draw(new Ellipse2D.Float(x, y, d, d));
            g2.setPaint(new GradientPaint(x, y, BRAND_LT, x + d, y + d, BRAND_DK));
            g2.draw(new Arc2D.Float(x, y, d, d, -angle, 100, Arc2D.OPEN));
        }
        g2.dispose();
    }
}
