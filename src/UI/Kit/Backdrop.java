package UI.Kit;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static UI.Kit.Palette.*;

// The window-wide background: light blue gradient, soft glows and, when the
// files exist, the dotted world map (top-right) and the glass swirl (bottom-right).
//
// paint(g, c) draws in window coordinates, so side-by-side components
// (sidebar + content) can each paint it and the result is seamless.
public final class Backdrop {
    private Backdrop() { }

    public static void paint(Graphics g, JComponent c) {
        JRootPane root = SwingUtilities.getRootPane(c);
        int w, h;
        Point off;
        if (root == null) {
            w = c.getWidth();
            h = c.getHeight();
            off = new Point(0, 0);
        } else {
            w = root.getWidth();
            h = root.getHeight();
            off = SwingUtilities.convertPoint(c, 0, 0, root);
        }
        if (w <= 0 || h <= 0) return;

        Graphics2D g2 = Glass.aa(g);
        g2.translate(-off.x, -off.y);

        // base gradient
        g2.setPaint(new GradientPaint(0, 0, BG_1, w, h, BG_2));
        g2.fillRect(0, 0, w, h);

        // soft light blobs
        glow(g2, w * 0.88f, h * 0.95f, w * 0.45f, GLOW, 70);
        glow(g2, w * 0.40f, h * 0.10f, w * 0.35f, Color.WHITE, 140);
        glow(g2, w * 0.10f, h * 0.85f, w * 0.30f, GLOW, 40);

        // optional images (see docs/ui-assets.md)
        BufferedImage swirl = Assets.image(Assets.BG_SWIRL);
        if (swirl != null) {
            int sw = Math.round(w * 0.55f);
            int sh = Math.round(sw * (swirl.getHeight() / (float) swirl.getWidth()));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
            g2.drawImage(swirl, w - sw, h - sh, sw, sh, null);
            g2.setComposite(AlphaComposite.SrcOver);
        }
        BufferedImage map = Assets.image(Assets.WORLD_MAP);
        if (map != null) {
            int mw = Math.round(w * 0.50f);
            int mh = Math.round(mw * (map.getHeight() / (float) map.getWidth()));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            g2.drawImage(map, w - mw - 24, 12, mw, mh, null);
            g2.setComposite(AlphaComposite.SrcOver);
        }
        g2.dispose();
    }

    private static void glow(Graphics2D g2, float cx, float cy, float r, Color c, int alpha) {
        if (r < 1) return;
        g2.setPaint(new RadialGradientPaint(new Point(Math.round(cx), Math.round(cy)), r,
                new float[]{0f, 1f},
                new Color[]{Glass.alpha(c, alpha), Glass.alpha(c, 0)}));
        g2.fillRect(Math.round(cx - r), Math.round(cy - r), Math.round(r * 2), Math.round(r * 2));
    }

    // Panel that paints the backdrop. Use it as a frame's content pane or as a wrapper.
    public static class Panel extends JPanel {
        public Panel(LayoutManager lm) {
            super(lm);
            setOpaque(true);
            setBackground(FrameData.PAGE_BG);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Backdrop.paint(g, this);
        }
    }
}
