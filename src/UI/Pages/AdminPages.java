package UI.Pages;

import UI.Kit.FrameData;
import UI.Pages.Components.ContentCard;
import UI.Pages.Components.NavIcon;
import UI.Pages.Components.Sidebar;
import UI.Pages.admin.Courses;
import UI.Pages.admin.MyProfile;
import UI.Pages.admin.Notices;
import UI.Pages.admin.Overview;
import UI.Pages.admin.Timetables;
import UI.Pages.admin.UserProfiles;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class AdminPages extends JFrame implements FrameData {


    // one icon per entry in adminTabs, same order
    private static final NavIcon[] ICONS = {
            NavIcon.GRID, NavIcon.USERS, NavIcon.BOOK,
            NavIcon.MEGA, NavIcon.CAL, NavIcon.USER
    };

    private final Sidebar sidebar;
    private final CardLayout cards = new CardLayout();
    private final JPanel cardHost = new JPanel(cards);

    public AdminPages(User user) {
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(width, height));
        setIconImage(new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/assets/img.png"))).getImage());

        sidebar = new Sidebar(user, "ADMINISTRATOR", adminTabs, ICONS, PAGE_BG);
        sidebar.setOnSelect(i -> cards.show(cardHost, adminTabs[i]));
        sidebar.setOnLogout(this::dispose);   // login flow comes later

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(container(user), BorderLayout.CENTER);
    }

    // rounded content card, same look as the sidebar
    private JPanel container(User user) {
        cardHost.setOpaque(false);   // so the card's fill shows through

        // same order as adminTabs
        cardHost.add(new Overview(user),       adminTabs[0]);
        cardHost.add(new UserProfiles(),       adminTabs[1]);
        cardHost.add(new Courses(),            adminTabs[2]);
        cardHost.add(new Notices(user),        adminTabs[3]);
        cardHost.add(new Timetables(),         adminTabs[4]);
        cardHost.add(new MyProfile(user),      adminTabs[5]);

        return new ContentCard(cardHost, PAGE_BG).withGaps(PAGE_BG);
    }

    // number of the selected card (0-based), straight from the sidebar
    public int getSelectedIndex() { return sidebar.getSelectedIndex(); }
}
