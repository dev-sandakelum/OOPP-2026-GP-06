package UI.Pages;

import UI.Kit.FrameData;
import UI.Pages.Components.ContentCard;
import UI.Pages.Components.NavIcon;
import UI.Pages.Components.Sidebar;
import UI.Pages.Student.*;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class StudentPages extends JFrame implements FrameData {

    // one icon per entry in adminTabs, same order
    private static final NavIcon[] ICONS = {
            NavIcon.GRID, NavIcon.BOOK, NavIcon.ATTENDANCE,
            NavIcon.GRADES,NavIcon.MEDICAL, NavIcon.MEGA,NavIcon.CAL, NavIcon.USER
    };

    private final Sidebar sidebar;
    private final CardLayout cards = new CardLayout();
    private final JPanel cardHost = new JPanel(cards);

    public StudentPages(User user) {
        setTitle("AFMS — Student · " + studentTabs[0]);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(width, height));
        setIconImage(new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/assets/img.png"))).getImage());

        sidebar = new Sidebar(user, "STUDENT", studentTabs, ICONS, PAGE_BG);
        sidebar.setOnSelect(i -> {
            cards.show(cardHost, studentTabs[i]);
            setTitle("AFMS — Student · " + studentTabs[i]);
        });
        sidebar.setOnLogout(() -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(container(user), BorderLayout.CENTER);
    }

    private JPanel container(User user) {
        cardHost.setBackground(PAGE_BG);
        cardHost.add(new Overview(user), studentTabs[0]);
        cardHost.add(new MyCourses(user), studentTabs[1]);
        cardHost.add(new MyAttendance(user), studentTabs[2]);
        cardHost.add(new MarksGPA(user), studentTabs[3]);
        cardHost.add(new Medicals(user), studentTabs[4]);
        cardHost.add(new Notices(user), studentTabs[5]);
        cardHost.add(new Timetable(user), studentTabs[6]);
        cardHost.add(new MyProfile(user), studentTabs[7]);
        


        return new ContentCard(cardHost,PAGE_BG).withGaps(PAGE_BG);
    }

    // number of the selected card (0-based), straight from the sidebar
    public int getSelectedIndex() { return sidebar.getSelectedIndex(); }
}
