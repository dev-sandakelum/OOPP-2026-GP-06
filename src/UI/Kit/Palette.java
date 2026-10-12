package UI.Kit;

import java.awt.*;

// Colors and font shared by every page.
public final class Palette {
    private Palette() { }

    public static final Color INK        = new Color(13, 31, 22);
    public static final Color INK_2      = new Color(122, 135, 128);
    public static final Color LINE       = new Color(229, 235, 231);
    public static final Color LINE_2     = new Color(211, 220, 215);
    public static final Color SURFACE    = Color.WHITE;
    public static final Color SURFACE_2  = new Color(245, 248, 246);
    public static final Color BRAND      = new Color(15, 61, 42);
    public static final Color BRAND_LT   = new Color(38, 144, 95);
    public static final Color BRAND_DK   = new Color(8, 38, 25);
    public static final Color BRAND_SOFT = new Color(223, 237, 229);
    public static final Color OK         = new Color(31, 122, 74);
    public static final Color WARN       = new Color(154, 106, 12);
    public static final Color BAD        = new Color(192, 58, 43);

    public static Font font(int style, float size) {
        return new Font("Segoe UI", style, Math.round(size));
    }
}
