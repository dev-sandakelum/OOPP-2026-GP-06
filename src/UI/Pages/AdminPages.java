package UI.Pages;

import UI.Kit.Page;

public class AdminPages extends Page {
    private final String[] tabs = {"Overview", "User Profiles", "Courses", "Notices", "Timetables", "My Profile"};

    public AdminPages(){
        Page pg = new Page();
        pg.createFrame(1280 , 800);
        pg.setVisible();
    }
}
