package UI.Pages;

import UI.Kit.FrameData;
import UI.Pages.Components.NavIcon;
import UI.Pages.Components.Sidebar;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

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

    public LecturerPage() {
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(width, height));
        setIconImage(new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/assets/img.png"))).getImage());

        sidebar = new Sidebar("Lecturer", lecturerTabs, ICONS,
                "Mrs. N. P. Jayasuriya", "Administration · admin", PAGE_BG);
        sidebar.setOnSelect(i -> cards.show(cardHost, lecturerTabs[i]));
        sidebar.setOnLogout(() -> dispose());   // replace with your login flow

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(container(), BorderLayout.CENTER);
    }

    private JPanel container() {
        cardHost.setBackground(PAGE_BG);
        for (String tab : lecturerTabs) {
            JPanel page = new JPanel(new BorderLayout());
            page.setOpaque(false);
            JLabel title = new JLabel(tab);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            title.setBorder(new EmptyBorder(24, 28, 0, 0));
            page.add(title, BorderLayout.NORTH);   // replace with your real page for each tab
            cardHost.add(page, tab);
        }
        return cardHost;
    }

    // number of the selected card (0-based), straight from the sidebar
    public int getSelectedIndex() { return sidebar.getSelectedIndex(); }
}