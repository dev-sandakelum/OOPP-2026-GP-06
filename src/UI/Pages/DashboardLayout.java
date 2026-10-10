package UI.Pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardLayout extends JFrame {

    public DashboardLayout() {
        setTitle("FOTIMS - Faculty of Tech");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 850);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        // Root container: BorderLayout
        getContentPane().setLayout(new BorderLayout());

        // 1. Sidebar (Fixed West)
        getContentPane().add(createSidebar(), BorderLayout.WEST);

        // 2. Main Content (Scrollable Center)
        JScrollPane scrollPane = new JScrollPane(createMainContent());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(new Color(245, 244, 240)); // Warm background tone

        // Top: Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        logoPanel.setOpaque(false);
        logoPanel.add(new JLabel("FOTIMS"));
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Center: Navigation List
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(10, 16, 10, 16));
        // Add nav buttons/items here
        sidebar.add(navList, BorderLayout.CENTER);

        // Bottom: User profile & DB status
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
        // Add user chip and status text here
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 32, 32, 32)); // Generous dashboard padding
        content.setBackground(new Color(248, 247, 244));

        // Add sections with vertical spacing between them
        content.add(createHeaderSection());
        content.add(Box.createVerticalStrut(24));

        content.add(createStatCardsSection());
        content.add(Box.createVerticalStrut(32));

        content.add(createAttendanceSection());
        content.add(Box.createVerticalStrut(32));

        content.add(createRequiresAttentionSection());

        return content;
    }

    // --- Section Builders ---

    private JPanel createHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Left: Title + subtitle stacked vertically
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(new JLabel("Overview"));
        titleBox.add(new JLabel("System snapshot"));
        header.add(titleBox, BorderLayout.WEST);

        // Right: Role pills
        JPanel rolesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rolesPanel.setOpaque(false);
        // Add role badges (Administrator, Lecturer, etc.)
        header.add(rolesPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatCardsSection() {
        // 1 row, 4 equal columns with 16px horizontal spacing
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Add 4 stat cards (Users, Courses, Medical Records, Notices)
        statsPanel.add(createPlaceholderCard("USERS"));
        statsPanel.add(createPlaceholderCard("COURSES"));
        statsPanel.add(createPlaceholderCard("MEDICAL RECORDS"));
        statsPanel.add(createPlaceholderCard("NOTICES"));

        return statsPanel;
    }

    private JPanel createAttendanceSection() {
        JPanel section = new JPanel(new BorderLayout(0, 12));
        section.setOpaque(false);

        // Header + Legend
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(new JLabel("Attendance by module"), BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        legend.setOpaque(false);
        // Add legend indicators (present, medical leave, absent)
        header.add(legend, BorderLayout.EAST);

        section.add(header, BorderLayout.NORTH);

        // Card Container for Data Table
        JPanel cardTable = new JPanel(new BorderLayout());
        cardTable.setBackground(Color.WHITE);
        // Add table / row components here
        section.add(cardTable, BorderLayout.CENTER);

        return section;
    }

    private JPanel createRequiresAttentionSection() {
        JPanel section = new JPanel(new BorderLayout(0, 12));
        section.setOpaque(false);

        // Header + Action Button
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(new JLabel("Requires attention"), BorderLayout.WEST);

        JButton actionBtn = new JButton("Open profiles");
        header.add(actionBtn, BorderLayout.EAST);

        section.add(header, BorderLayout.NORTH);

        // Card Container for Attention Table
        JPanel cardTable = new JPanel(new BorderLayout());
        cardTable.setBackground(Color.WHITE);
        // Add table / row components here
        section.add(cardTable, BorderLayout.CENTER);

        return section;
    }

    private JPanel createPlaceholderCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(new JLabel(title), BorderLayout.NORTH);
        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardLayout().setVisible(true));
    }
}