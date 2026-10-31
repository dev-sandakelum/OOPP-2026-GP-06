package UI.Pages.Components;

import UI.Kit.Assets;
import UI.Kit.Glass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import static UI.Kit.Palette.*;

// Rounded panel showing an asset photo cropped to cover the panel ("object-fit: cover").
// (focusX, focusY) in 0..1 pick which point of the image stays in view when cropping.
// A dark-blue gradient is laid over the lower part so white caption text stays readable.
// Falls back to the blue gradient panel when the image is missing.
public class PhotoPanel extends JPanel {

    private final String asset;
    private final float focusX, focusY;
    private final int radius;

    public PhotoPanel(String asset, float focusX, float focusY, int radius) {
        super(new BorderLayout());
        this.asset = asset;
        this.focusX = focusX;
        this.focusY = focusY;
        this.radius = radius;
        setOpaque(false);
    }

    // White caption block (title, subtitle, note) to drop into SOUTH of the panel.
    public static JComponent caption(String title, String sub, String note) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(0, 26, 26, 26));

        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 26));
        t.setForeground(ON_BRAND);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s = new JLabel(sub);
        s.setFont(font(Font.BOLD, 12));
        s.setForeground(ON_BRAND_2);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel n = new JLabel(note);
        n.setFont(font(Font.PLAIN, 12));
        n.setForeground(ON_BRAND_2);
        n.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(t);
        p.add(Box.createVerticalStrut(2));
        p.add(s);
        p.add(Box.createVerticalStrut(10));
        p.add(n);
        return p;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        float x = Card.INSET_X, y = Card.INSET_TOP;
        float w = getWidth() - Card.INSET_X * 2, h = getHeight() - Card.INSET_TOP - Card.INSET_BOTTOM;
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, radius * 2, radius * 2);
        Glass.shadow(g2, x, y, w, h, radius);

        BufferedImage img = Assets.image(asset);
        if (img == null) {
            Glass.brand(g2, x, y, w, h, radius);
            g2.dispose();
            return;
        }

        Shape old = g2.getClip();
        g2.clip(shape);
        float scale = Math.max(w / img.getWidth(), h / img.getHeight());
        int dw = Math.round(img.getWidth() * scale), dh = Math.round(img.getHeight() * scale);
        int dx = Math.round(x + clamp(w / 2 - focusX * dw, w - dw, 0));
        int dy = Math.round(y + clamp(h / 2 - focusY * dh, h - dh, 0));
        g2.drawImage(img, dx, dy, dw, dh, null);

        // readability overlay for the caption
        g2.setPaint(new GradientPaint(0, y + h * 0.45f, Glass.alpha(BRAND_DK, 0),
                                      0, y + h, Glass.alpha(BRAND_DK, 170)));
        g2.fill(shape);
        g2.setClip(old);

        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, radius * 2, radius * 2));
        g2.dispose();
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
