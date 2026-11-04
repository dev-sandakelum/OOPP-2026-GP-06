package UI.Kit;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// Optional images used by the theme. Every entry is loaded from the classpath
// (src/assets/...) and is allowed to be missing: callers get null and paint
// a fallback instead. See docs/ui-assets.md for the list of files to add.
public final class Assets {
    private Assets() { }

    public static final String APP_ICON      = "/assets/img.png";
    public static final String WORLD_MAP     = "/assets/bg_world_map.png";      // dotted world map, top-right of the window
    public static final String BG_SWIRL      = "/assets/bg_swirl.png";          // soft glass swirl, bottom-right of the window
    public static final String SIDEBAR_PHOTO = "/assets/sidebar_building.png";  // faculty building, lower half of the sidebar

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final BufferedImage MISSING = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    // Returns the image, or null when the file is not on the classpath.
    public static synchronized BufferedImage image(String path) {
        BufferedImage img = CACHE.get(path);
        if (img == null) {
            img = MISSING;
            try (InputStream in = Assets.class.getResourceAsStream(path)) {
                if (in != null) {
                    BufferedImage loaded = ImageIO.read(in);
                    if (loaded != null) img = loaded;
                }
            } catch (Exception ignored) { }
            CACHE.put(path, img);
        }
        return img == MISSING ? null : img;
    }

    public static boolean has(String path) { return image(path) != null; }
}
