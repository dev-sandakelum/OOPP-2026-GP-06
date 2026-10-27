package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static UI.Kit.Palette.*;

public class Timetable extends Page {

    private static final String[] DAYS  = {"MON", "TUE", "WED", "THU", "FRI"};
    private static final String[] SLOTS = {"08.30 - 10.30", "10.45 - 12.45", "13.30 - 15.30", "15.45 - 17.45"};

    // [slot][day] = {course code, type (T/P), details (room + lecturer)}
    private final String[][][] grid = new String[SLOTS.length][DAYS.length][];

    private final JPanel gridPanel = new JPanel(new GridBagLayout());

    public Timetable() {
        super("Timetable", "Level II · Semester I · 2026 — both theory and practical count 15 sessions, 2h each.", null);

        // Grid එක හිස් කිරීම
        for (String[][] row : grid) {
            for (int d = 0; d < row.length; d++) row[d] = new String[]{"", "", ""};
        }

        // පින්තූරයට අදාළ Sample Data පිරවීම
        grid[0][0] = new String[]{"ICT2131", "T", "LH-1 · Dr. Perera"};
        grid[1][0] = new String[]{"ICT2132", "P", "Lab-201 · Dr. Fernando"};
        grid[2][0] = new String[]{"ICT2134", "T", "LH-2 · Dr. Bandara"};

        grid[0][1] = new String[]{"ICT2133", "T", "LH-1 · Prof. Wijewardena"};
        grid[1][1] = new String[]{"ICT2133", "P", "Lab-202 · Prof. Wijewardena"};
        grid[2][1] = new String[]{"ICT2136", "T", "LH-3 · Dr. Perera"};

        grid[0][2] = new String[]{"ICT2134", "T", "LH-2 · Dr. Bandara"};
        grid[2][2] = new String[]{"ICT2135", "T", "LH-1 · Mrs. Silva"};
        grid[3][2] = new String[]{"ICT2135", "P", "Lab-201 · Mrs. Silva"};

        grid[0][3] = new String[]{"ICT2131", "P", "Lab-202 · Dr. Perera"};
        grid[1][3] = new String[]{"ICT2132", "T", "LH-1 · Dr. Fernando"};
        grid[2][3] = new String[]{"ICT2136", "P", "Lab-203 · Dr. Perera"};

        grid[0][4] = new String[]{"ICT2136", "T", "LH-3 · Dr. Perera"};
        grid[1][4] = new String[]{"ICT2135", "T", "LH-1 · Mrs. Silva"};

        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);

        // Legend එක එකතු කිරීම (Theory / Practical)
        container.add(createLegend(), BorderLayout.NORTH);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(0, 0, 0, 0));
        gridPanel.setOpaque(false);
        card.add(gridPanel, BorderLayout.CENTER);

        container.add(card, BorderLayout.CENTER);

        setScrollBody(container);
        rebuild();
    }

    private JPanel createLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        legend.setOpaque(false);

        // Theory legend item
        JLabel theoryLbl = new JLabel(" theory");
        theoryLbl.setIcon(new LegendIcon(false));
        theoryLbl.setFont(font(Font.PLAIN, 12));
        theoryLbl.setForeground(INK_2);

        // Practical legend item
        JLabel practicalLbl = new JLabel(" practical");
        practicalLbl.setIcon(new LegendIcon(true));
        practicalLbl.setFont(font(Font.PLAIN, 12));
        practicalLbl.setForeground(INK_2);

        legend.add(theoryLbl);
        legend.add(practicalLbl);
        return legend;
    }

    private void rebuild() {
        gridPanel.removeAll();
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(1, 1, 1, 1); // Cell අතර පරතරය

        // හිස් ඉහළ වම් කෙළවර
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; gc.weighty = 0;
        gridPanel.add(Box.createHorizontalStrut(90), gc);

        // දවස්වල නම් (Headers)
        for (int d = 0; d < DAYS.length; d++) {
            gc.gridx = d + 1; gc.gridy = 0; gc.weightx = 1;

            boolean isToday = (d == 4); // සිකුරාදා 'TODAY' ලෙස Highlight කිරීම
            JPanel headerCell = new JPanel(new BorderLayout());
            headerCell.setBackground(isToday ? new Color(27, 94, 32) : Color.WHITE);
            headerCell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
            headerCell.setPreferredSize(new Dimension(100, 45));

            JLabel l = new JLabel(isToday ? DAYS[d] + " · TODAY" : DAYS[d], SwingConstants.CENTER);
            l.setFont(font(Font.BOLD, 12));
            l.setForeground(isToday ? Color.WHITE : INK_2);

            headerCell.add(l, BorderLayout.CENTER);
            gridPanel.add(headerCell, gc);
        }

        // Time Slots සහ Cells
        for (int s = 0; s < SLOTS.length; s++) {
            gc.gridx = 0; gc.gridy = s + 1; gc.weightx = 0; gc.weighty = 1;

            JPanel timeCell = new JPanel(new BorderLayout());
            timeCell.setBackground(Color.WHITE);
            timeCell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(230, 230, 230)));

            JLabel l = new JLabel(SLOTS[s], SwingConstants.CENTER);
            l.setFont(font(Font.PLAIN, 12));
            l.setForeground(INK_2);
            timeCell.add(l, BorderLayout.CENTER);
            gridPanel.add(timeCell, gc);

            for (int d = 0; d < DAYS.length; d++) {
                gc.gridx = d + 1; gc.weightx = 1;
                gridPanel.add(createCourseCell(grid[s][d]), gc);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createCourseCell(String[] data) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(235, 235, 235)),
                new EmptyBorder(12, 15, 12, 15)
        ));

        if (data == null || data[0].isEmpty()) {
            p.setBackground(Color.WHITE);
            return p;
        }

        boolean isPractical = "P".equals(data[1]);
        // Practical නම් ලා කොළ පාට, නැත්නම් සුදු පාට
        p.setBackground(isPractical ? new Color(232, 245, 233) : Color.WHITE);

        // Course Code සහ Badge
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);

        JLabel code = new JLabel(data[0]);
        code.setFont(font(Font.BOLD, 13));
        code.setForeground(INK);
        top.add(code);

        JLabel badge = new JLabel(data[1], SwingConstants.CENTER);
        badge.setFont(font(Font.BOLD, 10));
        badge.setPreferredSize(new Dimension(20, 20));
        badge.setOpaque(true);

        if (isPractical) {
            badge.setBackground(new Color(27, 94, 32));
            badge.setForeground(Color.WHITE);
        } else {
            badge.setBackground(Color.WHITE);
            badge.setForeground(Color.DARK_GRAY);
            badge.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }
        top.add(badge);

        // Location සහ Lecturer
        JLabel loc = new JLabel(data[2]);
        loc.setFont(font(Font.PLAIN, 12));
        loc.setForeground(Color.GRAY);

        p.add(top, BorderLayout.NORTH);
        p.add(loc, BorderLayout.CENTER);

        return p;
    }

    // Legend එකේ icon එක අඳින්න පොඩි custom Icon class එකක්
    private static class LegendIcon implements Icon {
        private final boolean filled;

        public LegendIcon(boolean filled) {
            this.filled = filled;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (filled) {
                g2.setColor(new Color(27, 94, 32));
                g2.fillRoundRect(x, y + 2, 12, 12, 4, 4);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y + 2, 12, 12, 4, 4);
                g2.setColor(Color.GRAY);
                g2.drawRoundRect(x, y + 2, 12, 12, 4, 4);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 12; }
        @Override
        public int getIconHeight() { return 16; }
    }
}