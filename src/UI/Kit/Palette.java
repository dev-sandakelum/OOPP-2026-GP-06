package UI.Kit;

import java.awt.*;

// Colors and font shared by every page.
// Theme: "AFMS" (Academic & Faculty Management System) - light blue backdrop, frosted white cards, royal-blue accent.
public final class Palette {
    private Palette() { }

    // ---------- Text ----------
    public static final Color INK        = new Color(14, 30, 70);     // headings, body text
    public static final Color INK_2      = new Color(94, 110, 148);   // muted text
    public static final Color ON_BRAND   = Color.WHITE;               // text on blue surfaces
    public static final Color ON_BRAND_2 = new Color(214, 228, 255);  // muted text on blue surfaces

    // ---------- Lines & surfaces ----------
    public static final Color LINE       = new Color(222, 231, 246);
    public static final Color LINE_2     = new Color(200, 213, 236);
    public static final Color SURFACE    = Color.WHITE;
    public static final Color SURFACE_2  = new Color(238, 243, 252);

    // ---------- Brand (royal blue) ----------
    public static final Color BRAND      = new Color(22, 78, 196);
    public static final Color BRAND_LT   = new Color(64, 128, 255);
    public static final Color BRAND_DK   = new Color(10, 44, 150);
    public static final Color BRAND_SOFT = new Color(224, 235, 252);

    // ---------- Status ----------
    public static final Color OK         = new Color(22, 163, 92);
    public static final Color WARN       = new Color(214, 140, 10);
    public static final Color BAD        = new Color(222, 56, 56);

    // ---------- Glass ----------
    public static final Color GLASS        = new Color(255, 255, 255, 178);  // frosted card fill
    public static final Color GLASS_STRONG = new Color(255, 255, 255, 222);  // account card, inputs
    public static final Color GLASS_FAINT  = new Color(255, 255, 255, 70);   // content area wash
    public static final Color GLASS_LINE   = new Color(255, 255, 255, 235);  // hairline on glass
    public static final Color SHADOW       = new Color(30, 70, 160, 26);     // soft blue shadow

    // ---------- Backdrop gradient ----------
    public static final Color BG_1  = new Color(246, 249, 255);   // top-left
    public static final Color BG_2  = new Color(212, 227, 250);   // bottom-right
    public static final Color GLOW  = new Color(120, 170, 255);   // soft light blobs

    // ---------- Radii ----------
    public static final int RADIUS_PANEL = 28;   // sidebar / content panel
    public static final int RADIUS_CARD  = 22;   // cards
    public static final int RADIUS_PILL  = 16;   // nav pills, buttons

    public static Font font(int style, float size) {
        return new Font("Segoe UI", style, Math.round(size));
    }
}
