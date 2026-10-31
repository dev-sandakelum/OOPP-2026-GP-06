package UI.Pages;

import UI.Kit.FrameData;
import UI.Pages.Components.ContentCard;
import UI.Pages.Components.NavIcon;
import UI.Pages.Components.Sidebar;
import UI.Pages.Officer.*;
import model.TechnicalOfficer;
import model.Undergraduate;
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
        setTitle("AFMS — Officer · " + officerTabs[0]);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(width, height));
        setIconImage(new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/assets/img.png"))).getImage());

        sidebar = new Sidebar(user, "TECHNICAL OFFICER", officerTabs, ICONS, PAGE_BG);
        sidebar.setOnSelect(i -> {
            cards.show(cardHost, officerTabs[i]);
            setTitle("AFMS — Officer · " + officerTabs[i]);
        });
        sidebar.setOnLogout(() -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
        });   // login flow comes later

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(container(user), BorderLayout.CENTER);
    }

    // rounded content card, same look as the sidebar
    private JPanel container(User user) {
        TechnicalOfficer to = new TechnicalOfficer(user);
        cardHost.setOpaque(false);   // so the card's fill shows through

        cardHost.add(new Overview(user), officerTabs[0]);
        cardHost.add(new AttendanceRegister(), officerTabs[1]);
        cardHost.add(new Medicals(), officerTabs[2]);
        cardHost.add(new Notices(), officerTabs[3]);
        cardHost.add(new Timetables(), officerTabs[4]);
        cardHost.add(new MyProfile(to), officerTabs[5]);


        return new ContentCard(cardHost, PAGE_BG).withGaps(PAGE_BG);
    }

    // number of the selected card (0-based), straight from the sidebar
    public int getSelectedIndex() { return sidebar.getSelectedIndex(); }
}
