package UI.Kit;

import java.awt.*;

public interface FrameData {
    int width = 1280;
    int height = 800;

    int sp_width = 300;
    int lp_height = 60;

    String[] adminTabs = {"Overview", "User Profiles", "Courses", "Notices", "Timetables", "My Profile"};

    // ---------- Colors ----------
    Color PAGE_BG = new Color(202, 253, 235);

    Color SIDEBAR_BG = new Color(240, 244, 242);
    Color ACTIVE_BG  = new Color(224, 238, 229);   // selected pill (soft green)
    Color HOVER_BG   = new Color(230, 237, 233);
    Color ACCENT     = new Color(215, 85, 55);     // not used by the nav anymore
    Color TEXT_ON    = new Color(18, 64, 42);      // selected text/icon (dark green)
    Color TEXT_OFF   = new Color(100, 112, 106);   // unselected text/icon
    Color LABEL_COL  = new Color(150, 162, 156);   // "ADMINISTRATOR" label
}