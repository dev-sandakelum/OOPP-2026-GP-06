package UI.Kit;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static UI.Kit.Palette.*;

// Drawing helpers for the frosted-glass look: soft shadow, glass panel, blue gradient panel.
public final class Glass {
    private Glass() { }

    public static final int SHADOW_LAYERS = 5;

    // Soft shadow under a rounded rect. Paints up to ~SHADOW_LAYERS px outside the rect.
    public static void shadow(Graphics2D g2, float x, float y, float w, float h, int radius) {
        for (int i = SHADOW_LAYERS; i >= 1; i--) {
            g2.setColor(new Color(SHADOW.getRed(), SHADOW.getGreen(), SHADOW.getBlue(),
                    Math.max(4, SHADOW.getAlpha() - i * 4)));
            g2.fill(new RoundRectangle2D.Float(x - i * 0.6f, y + i * 0.8f, w + i * 1.2f, h + i * 0.6f,
                    radius * 2 + i, radius * 2 + i));
        }
    }

    // Frosted white panel with a white hairline and a light sheen at the top.
    public static void panel(Graphics2D g2, float x, float y, float w, float h, int radius, Color fill) {
        RoundRectangle2D s = new RoundRectangle2D.Float(x, y, w, h, radius * 2, radius * 2);
        g2.setColor(fill);
        g2.fill(s);
        g2.setPaint(new GradientPaint(x, y, new Color(255, 255, 255, 110),
                                      x, y + Math.max(1, h * 0.45f), new Color(255, 255, 255, 0)));
        g2.fill(s);
        g2.setColor(GLASS_LINE);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, radius * 2, radius * 2));
    }

    // Royal-blue gradient panel with a glossy highlight (the "Total users" card, buttons, badges).
    public static void brand(Graphics2D g2, float x, float y, float w, float h, int radius) {
        RoundRectangle2D s = new RoundRectangle2D.Float(x, y, w, h, radius * 2, radius * 2);
        g2.setPaint(new GradientPaint(x, y, BRAND_LT, x + w, y + h, BRAND_DK));
        g2.fill(s);
        Shape old = g2.getClip();
        g2.clip(s);
        g2.setPaint(new RadialGradientPaint(
                new Point(Math.round(x + w * 0.85f), Math.round(y + h * 1.05f)), Math.max(1f, w * 0.6f),
                new float[]{0f, 1f}, new Color[]{new Color(255, 255, 255, 80), new Color(255, 255, 255, 0)}));
        g2.fill(s);
        g2.setPaint(new GradientPaint(x, y, new Color(255, 255, 255, 60),
                                      x, y + Math.max(1, h * 0.5f), new Color(255, 255, 255, 0)));
        g2.fill(s);
        g2.setClip(old);
        g2.setColor(new Color(255, 255, 255, 90));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, radius * 2, radius * 2));
    }

    // Graphics copy with the usual anti-aliasing hints switched on.
    public static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    // Same color with a different alpha.
    public static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
}
