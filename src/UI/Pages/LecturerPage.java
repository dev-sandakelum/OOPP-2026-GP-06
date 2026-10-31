package UI.Pages;

import UI.Kit.FrameData;
import UI.Pages.Components.ContentCard;
import UI.Pages.Components.NavIcon;
import UI.Pages.Components.Sidebar;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

import UI.Pages.lecturer.Attendance;
import UI.Pages.lecturer.Grades_GPA;
import UI.Pages.lecturer.Marks;
import UI.Pages.lecturer.Medicals;
import UI.Pages.lecturer.MyCourses;
import UI.Pages.lecturer.MyProfile;
import UI.Pages.lecturer.Notices;
import UI.Pages.lecturer.Overview;
import UI.Pages.lecturer.Undergraduates;
import model.User;

public class LecturerPage extends JFrame implements FrameData {

    // one icon per entry in adminTabs, same order
    private static final NavIcon[] ICONS = {
            NavIcon.GRID, NavIcon.BOOK,
            NavIcon.MARKS, NavIcon.STUDENTS, NavIcon.ATTENDANCE, NavIcon.GRADES,
            NavIcon.MEDICAL, NavIcon.MEGA, NavIcon.USER
    };

    private final Sidebar sidebar;
    private final CardLayout cards = new CardLayout();
    private final JPanel cardHost = new JPanel(cards);

    public LecturerPage(User user) {
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(width, height));
        setIconImage(new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/assets/img.png"))).getImage());

        sidebar = new Sidebar(user, "LECTURER", lecturerTabs, ICONS, PAGE_BG);
        sidebar.setOnSelect(i -> cards.show(cardHost, lecturerTabs[i]));
        sidebar.setOnLogout(() -> dispose());   // replace with your login flow

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(container(user), BorderLayout.CENTER);
    }

    private JPanel container(User user) {
        cardHost.setBackground(PAGE_BG);

        // same order as lecturerTabs
        cardHost.add(new Overview(user),   lecturerTabs[0]);
        cardHost.add(new MyCourses(user), lecturerTabs[1]);
        cardHost.add(new Marks(user), lecturerTabs[2]);
        cardHost.add(new Undergraduates(user), lecturerTabs[3]);
        cardHost.add(new Attendance(user), lecturerTabs[4]);
        cardHost.add(new Grades_GPA(user), lecturerTabs[5]);
        cardHost.add(new Medicals(user), lecturerTabs[6]);
        cardHost.add(new Notices(user), lecturerTabs[7]);
        cardHost.add(new MyProfile(user),  lecturerTabs[8]);

        return new ContentCard(cardHost, PAGE_BG).withGaps(PAGE_BG);
    }

    // number of the selected card (0-based), straight from the sidebar
    public int getSelectedIndex() { return sidebar.getSelectedIndex(); }
}