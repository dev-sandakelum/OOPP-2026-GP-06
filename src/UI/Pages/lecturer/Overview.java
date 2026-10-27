package UI.Pages.lecturer;
import UI.Pages.Components.Card;
import UI.Pages.lecturer.LecturerUI;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static UI.Kit.Palette.*;
import static UI.Kit.Palette.font;

public class Overview extends LecturerUI.Page {

    public Overview(User user) {

        super("Overview",
                "Welcome back, " + ((user != null && user.getName() != null) ? user.getName() : "Administrator")
                        + "  ·  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                null);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(kpiRow(), BorderLayout.NORTH);
        body.add(middleSection(), BorderLayout.CENTER);
        body.add(bottomSection(), BorderLayout.SOUTH);
        setScrollBody(body);
    }

    private JComponent kpiRow() {
        String[][] kpis = {
                {"My Modules","7", "Credits"},
                {"Undergraduates", "6", "This semester"},
                {"Average Attendance", "5", "across my module"},
                {"Pending medicals", "8", "Awaiting decision"}
        };
        JPanel row = new JPanel(new GridLayout(1, kpis.length, 16, 0));
        row.setOpaque(false);
        for (int i = 0; i < kpis.length; i++) {
            row.add(kpiCard(kpis[i][0], kpis[i][1], kpis[i][2], i == 0));
        }
        return row;
    }

    private JComponent kpiCard(String label, String value, String sub, boolean dark) {
        Card c = new Card(dark);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(18, 20, 18, 20));
        c.setPreferredSize(new Dimension(0, 124));
        Color muted = dark ? new Color(190, 215, 200) : INK_2;

        JLabel l = new JLabel(label);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(muted);
        JLabel v = new JLabel(value);
        v.setFont(font(Font.BOLD, 34));
        v.setForeground(dark ? Color.WHITE : INK);
        JLabel s = new JLabel(sub);
        s.setFont(font(Font.PLAIN, 12));
        s.setForeground(muted);

        c.add(l);
        c.add(Box.createVerticalGlue());
        c.add(v);
        c.add(s);
        return c;
    }

    // ==================== MIDDLE SECTION ====================
    private JComponent middleSection() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);

        // Left: Average Marks per Course
        panel.add(averageMarksPanel());

        // Right: Today's Schedule
        panel.add(todaysSchedulePanel());

        return panel;
    }

    // ==================== AVERAGE MARKS CHART ====================
    private JComponent averageMarksPanel() {
        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Average marks per course");
        titleLabel.setFont(font(Font.BOLD, 14));
        titleLabel.setForeground(INK);

        // Chart Panel
        JPanel chartContent = createBarChartPanel();

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(chartContent, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBarChartPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setOpaque(false);

        // TODO: Connect to MySQL database to fetch average marks per course
        String[] courses = {"CS101", "CS201", "SE301", "CS202", "AM01"};
        int[] averages = {75, 82, 88, 76, 85}; // Sample data - replace with DB queries

        int maxHeight = 120;

        for (int i = 0; i < courses.length; i++) {
            JPanel barContainer = new JPanel(new BorderLayout(0, 8));
            barContainer.setOpaque(false);
            barContainer.setPreferredSize(new Dimension(50, 160));

            // Bar
            JPanel bar = new JPanel();
            bar.setBackground(new Color(80, 140, 100));
            int barHeight = (averages[i] * maxHeight) / 100;
            bar.setPreferredSize(new Dimension(40, barHeight));

            // Label
            JLabel courseLabel = new JLabel(courses[i]);
            courseLabel.setFont(font(Font.PLAIN, 10));
            courseLabel.setForeground(INK_2);
            courseLabel.setHorizontalAlignment(JLabel.CENTER);

            barContainer.add(bar, BorderLayout.CENTER);
            barContainer.add(courseLabel, BorderLayout.SOUTH);

            panel.add(barContainer);
        }

        return panel;
    }

    // ==================== TODAY'S SCHEDULE ====================
    private JComponent todaysSchedulePanel() {
        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Today's schedule");
        titleLabel.setFont(font(Font.BOLD, 14));
        titleLabel.setForeground(INK);

        card.add(titleLabel, BorderLayout.NORTH);

        // Schedule Table
        JPanel tablePanel = createScheduleTable();
        card.add(tablePanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createScheduleTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // TODO: Connect to MySQL database to fetch today's schedule
        String[] columns = {"Time", "Course", "Hall"};
        Object[][] data = {
                {"08:30", "CS101 - Programming", "LH-2"},
                {"10:30", "SE301 - Software Eng.", "Lab-1"},
                {"14:00", "AM02 - Machine Learning", "LH-5"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(font(Font.PLAIN, 11));
        table.setRowHeight(28);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(240, 240, 240));
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setForeground(INK);

        // Header styling
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.getTableHeader().setFont(font(Font.BOLD, 11));
        table.getTableHeader().setForeground(INK);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ==================== BOTTOM SECTION ====================
    private JComponent bottomSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Recent Notices & Medical Requests
        panel.add(recentNoticesPanel(), BorderLayout.CENTER);

        return panel;
    }

    // ==================== RECENT NOTICES & MEDICAL REQUESTS ====================
    private JComponent recentNoticesPanel() {
        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Recent notices & medical requests");
        titleLabel.setFont(font(Font.BOLD, 14));
        titleLabel.setForeground(INK);

        card.add(titleLabel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = createNoticesContentPanel();
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createNoticesContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // TODO: Connect to MySQL database to fetch notices and medical requests

        // Notice Item 1
        JPanel notice1 = createNoticeItem(
                "Mid exam timetable released",
                "Posted 1 days ago | 23 notices"
        );
        panel.add(notice1);
        panel.add(Box.createVerticalStrut(12));

        // Notice Item 2
        JPanel notice2 = createNoticeItem(
                "Medical submitted - IT2O34 Nirmal R.",
                "Awaiting review: 20 DU"
        );
        panel.add(notice2);
        panel.add(Box.createVerticalStrut(12));

        // Notice Item 3
        JPanel notice3 = createNoticeItem(
                "Assignment submission deadline extended",
                "Posted 3 days ago | Updated today"
        );
        panel.add(notice3);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createNoticeItem(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.BOLD, 12));
        titleLabel.setForeground(INK);

        // Subtitle
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(font(Font.PLAIN, 11));
        subtitleLabel.setForeground(INK_2);

        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        // Divider
        JPanel divider = new JPanel();
        divider.setBackground(new Color(240, 240, 240));
        divider.setPreferredSize(new Dimension(0, 1));

        panel.add(textPanel, BorderLayout.NORTH);
        panel.add(divider, BorderLayout.SOUTH);

        return panel;
    }
}
