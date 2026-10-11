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

public class OfficerPages extends JFrame implements FrameData {


    // one icon per entry in officerTabs, same order
    private static final NavIcon[] ICONS = {
            NavIcon.GRID, NavIcon.REGISTER, NavIcon.MEDICAL,
            NavIcon.MEGA, NavIcon.CAL, NavIcon.USER
    };

    private final Sidebar sidebar;
    private final CardLayout cards = new CardLayout();
    private final JPanel cardHost = new JPanel(cards);

    public OfficerPages(User user) {
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(width, height));
        setIconImage(new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/assets/img.png"))).getImage());

        sidebar = new Sidebar(user, "TECHNICAL OFFICER", officerTabs, ICONS, PAGE_BG);
        sidebar.setOnSelect(i -> cards.show(cardHost, officerTabs[i]));
        sidebar.setOnLogout(this::dispose);   // login flow comes later

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(container(), BorderLayout.CENTER);
    }

    // rounded content card, same look as the sidebar
    private JPanel container() {
        cardHost.setOpaque(false);   // so the card's fill shows through

        // Loop through officerTabs to create placeholder panels
        for (String tabTitle : officerTabs) {
            JPanel placeholder = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
            placeholder.setOpaque(false);

            JLabel titleLabel = new JLabel(tabTitle);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
            placeholder.add(titleLabel);

            cardHost.add(placeholder, tabTitle);
        }

        return new ContentCard(cardHost, PAGE_BG).withGaps(PAGE_BG);
    }

    // number of the selected card (0-based), straight from the sidebar
    public int getSelectedIndex() { return sidebar.getSelectedIndex(); }
}
