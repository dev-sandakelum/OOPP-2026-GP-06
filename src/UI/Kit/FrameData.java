package UI.Kit;

import java.awt.*;

public interface FrameData {
    int width = 1280;
    int height = 800;

    int sp_width = 300;
    int lp_height = 60;

    String[] adminTabs = {"Overview", "User Profiles", "Courses", "Notices", "Timetables", "My Profile"};

    String[] lecturerTabs = {"Overview", "My Courses", "Marks", "Undergraduates",
            "Attendance", "Grades & GPA", "Medicals", "Notices", "My Profile"};

    String[] officerTabs  = {"Overview", "Attendance Register", "Medicals",
            "Notices", "Timetables", "My Profile"};

    String[] studentTabs  = {"Overview", "My Courses", "My Attendance", "Marks & GPA",
            "Medicals", "Notices", "Timetables", "My Profile"};

    // ---------- Colors (AFMS) ----------
    Color PAGE_BG = new Color(228, 238, 252);           // solid fallback behind the Backdrop gradient

    Color SIDEBAR_BG = new Color(247, 250, 255);        // opaque stand-in for the frosted sidebar
    Color ACTIVE_BG  = new Color(219, 232, 252);        // selected pill (soft blue)
    Color HOVER_BG   = new Color(236, 242, 253);
    Color ACCENT     = Palette.BRAND_LT;
    Color TEXT_ON    = Palette.BRAND;                   // selected text/icon (royal blue)
    Color TEXT_OFF   = new Color(52, 70, 110);          // unselected text/icon
    Color LABEL_COL  = new Color(90, 108, 148);         // "ADMINISTRATOR" label
}
