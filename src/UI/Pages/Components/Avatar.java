package UI.Pages.Components;

import UI.Kit.Glass;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static UI.Kit.Palette.*;

// Round avatar with either the user's photo or their initials, plus a thin white ring.
// Photos live in src/assets/photo/ (b1.jpg ... b15.jpg, g1.jpg ... g10.jpg) and are
// loaded once and cached, so the same avatar can be painted in table rows cheaply.
public class Avatar extends JComponent {

    private static final String PHOTO_DIR = "/assets/photo/";
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final BufferedImage MISSING = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private String initials;
    private BufferedImage photo;

    public Avatar(String name, int size) {
        this(name, null, size);
    }

    public Avatar(String name, String photoFilename, int size) {
        set(name, photoFilename);
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setOpaque(false);
    }

    /** Changes the person shown (used by detail panels that follow a table selection). */
    public void set(String name, String photoFilename) {
        this.initials = initialsOf(name);
        this.photo = photo(photoFilename);
        repaint();
    }

    public boolean hasPhoto() { return photo != null; }

    // ---------- Photo loading ----------

    /** Returns the cached profile photo, or null when the filename is empty or the file is missing. */
    public static synchronized BufferedImage photo(String filename) {
        if (filename == null || filename.isBlank()) return null;
        BufferedImage img = CACHE.get(filename);
        if (img == null) {
            img = MISSING;
            try (InputStream in = Avatar.class.getResourceAsStream(PHOTO_DIR + filename)) {
                if (in != null) {
                    BufferedImage loaded = ImageIO.read(in);
                    if (loaded != null) img = loaded;
                }
            } catch (Exception ignored) { }
            if (img == MISSING) System.err.println("Profile photo not found: " + PHOTO_DIR + filename);
            CACHE.put(filename, img);
        }
        return img == MISSING ? null : img;
    }

    public static String initialsOf(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\s+");
        String a = parts[0].substring(0, 1);
        String b = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (a + b).toUpperCase();
    }

    // ---------- Painting ----------

    // Photo already scaled and cut into a smooth circle, one per (photo, diameter). Clipping with
    // g2.clip() gives a jagged, non-antialiased edge, which showed up as a dark halo around the ring.
    private static final Map<BufferedImage, Map<Integer, BufferedImage>> ROUNDED = new IdentityHashMap<>();

    private static synchronized BufferedImage rounded(BufferedImage photo, int d) {
        Map<Integer, BufferedImage> bySize = ROUNDED.computeIfAbsent(photo, p -> new HashMap<>());
        BufferedImage img = bySize.get(d);
        if (img == null) {
            // scale so the photo covers the circle, then centre it (keeps the aspect ratio)
            float scale = Math.max((float) d / photo.getWidth(), (float) d / photo.getHeight());
            int w = Math.max(1, Math.round(photo.getWidth() * scale));
            int h = Math.max(1, Math.round(photo.getHeight() * scale));
            Image scaled = photo.getScaledInstance(w, h, Image.SCALE_SMOOTH);

            img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = img.createGraphics();
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ig.fill(new Ellipse2D.Float(0, 0, d, d));                  // antialiased mask
            ig.setComposite(AlphaComposite.SrcIn);                       // keep photo only inside it
            ig.drawImage(scaled, (d - w) / 2, (d - h) / 2, null);
            ig.dispose();
            bySize.put(d, img);
        }
        return img;
    }

    /** Paints a round avatar of diameter d at (x, y) on any Graphics2D (shared with table cells). */
    public static void paint(Graphics2D g2, BufferedImage photo, String initials, float x, float y, float d) {
        Ellipse2D circle = new Ellipse2D.Float(x, y, d, d);
        if (photo != null) {
            g2.drawImage(rounded(photo, Math.round(d)), Math.round(x), Math.round(y), null);
        } else {
            g2.setPaint(new GradientPaint(x, y, BRAND_LT, x + d, y + d, BRAND_DK));
            g2.fill(circle);
            g2.setColor(ON_BRAND);
            g2.setFont(font(Font.BOLD, d / 3f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initials, x + (d - fm.stringWidth(initials)) / 2,
                    y + (d - fm.getHeight()) / 2 + fm.getAscent());
        }
        // ring sits flush with the edge (outer side at the circle edge) so no photo shows outside it
        g2.setColor(new Color(255, 255, 255, 170));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new Ellipse2D.Float(x + 1f, y + 1f, d - 2, d - 2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        int d = Math.min(getWidth(), getHeight());
        int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2;
        paint(g2, photo, initials, x, y, d);
        g2.dispose();
    }
}
