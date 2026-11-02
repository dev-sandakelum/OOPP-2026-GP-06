package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static UI.Kit.Palette.*;

public class MyCourses extends Page {

    private static final Object[][] SEED = {
            {"ICT2131", "Programming with Java", "2T+1P", "Dr. A. P. Perera", 90.0, "A"},
            {"ICT2132", "Object Oriented Programming Practicum", "1T+2P", "Dr. R. S. Fernando", 93.3, "A+"},
            {"ICT2133", "Database Management Systems", "2T+1P", "Prof. H. L. Wijewardena", 93.3, "A+"},
            {"ICT2134", "Data Structures & Algorithms", "3T+0P", "Dr. M. G. T. Bandara", 93.3, "A"},
            {"ICT2135", "Software Engineering", "2T+1P", "Mrs. C. M. de Silva", 90.0, "A"},
            {"ICT2136", "Computer Networks", "2T+1P", "Dr. A. P. Perera", 90.0, "A"}
    };

    public MyCourses(User user) {
        super(
                "My Courses",
                "View your registered modules",
                null
        );

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // KPI cards at the top
        JComponent kpis = kpiRow();
        kpis.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(kpis);

        body.add(Box.createVerticalStrut(25));

        // Courses table section below KPI cards
        JComponent coursesTable = createTableCard();
        coursesTable.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(coursesTable);

        body.add(Box.createVerticalStrut(25));

        // Lecture materials uploaded by the lecturers of my courses
        JComponent materials = new MaterialsCard(user);
        materials.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(materials);

        setScrollBody(body);
    }

    // =================================================
    // KPI CARDS
    // =================================================

    private JComponent kpiRow() {
        String[][] kpis = {
                {"Modules", "6", "18 credits"},
                {"Theory Sessions", "90", "2h each"},
                {"Practical Sessions", "75", "2h each"}
        };

        JPanel row = new JPanel(new GridLayout(1, kpis.length, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));

        for (int i = 0; i < kpis.length; i++) {
            row.add(
                    kpiCard(
                            kpis[i][0],
                            kpis[i][1],
                            kpis[i][2],
                            i == 0
                    )
            );
        }

        return row;
    }

    private JComponent kpiCard(
            String label,
            String value,
            String sub,
            boolean dark
    ) {
        Card card = new Card(dark);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        Color muted = dark
                ? ON_BRAND_2
                : INK_2;

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(font(Font.BOLD, 13));
        titleLabel.setForeground(muted);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(font(Font.BOLD, 34));
        valueLabel.setForeground(
                dark ? Color.WHITE : INK
        );

        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(font(Font.PLAIN, 12));
        subLabel.setForeground(muted);

        card.add(titleLabel);
        card.add(Box.createVerticalGlue());
        card.add(valueLabel);
        card.add(subLabel);

        return card;
    }

    // =================================================
    // COURSES TABLE CARD (Aligned Layout)
    // =================================================

    private JComponent createTableCard() {
        Card tableCard = new Card(false);
        tableCard.setLayout(new BoxLayout(tableCard, BoxLayout.Y_AXIS));
        tableCard.setBorder(new EmptyBorder(15, 20, 20, 20));

        // Table Header
        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.X_AXIS));
        headerRow.setOpaque(false);
        headerRow.setBorder(new EmptyBorder(10, 0, 15, 0));

        String[] cols = {"MODULE", "TITLE", "CR", "LECTURER", "MY ATTENDANCE", "MY GRADE"};
        int[] widths = {100, 310, 60, 210, 150, 90};

        for (int i = 0; i < cols.length; i++) {
            JLabel l = new JLabel(cols[i]);
            l.setFont(font(Font.BOLD, 11));
            l.setForeground(INK_2);
            headerRow.add(wrapCell(l, widths[i]));
        }

        tableCard.add(headerRow);
        tableCard.add(new JSeparator(SwingConstants.HORIZONTAL));

        // Add Rows from SEED data
        for (Object[] rowData : SEED) {
            addCourseRow(
                    tableCard,
                    (String) rowData[0],
                    (String) rowData[1],
                    (String) rowData[2],
                    (String) rowData[3],
                    (Double) rowData[4],
                    (String) rowData[5],
                    widths
            );
        }

        return tableCard;
    }

    private void addCourseRow(
            JPanel container,
            String code,
            String title,
            String cr,
            String lecturer,
            double attendance,
            String grade,
            int[] widths
    ) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 0, 12, 0));

        // 1. Module Code
        JLabel codeLbl = new JLabel(code);
        codeLbl.setFont(font(Font.BOLD, 13));
        codeLbl.setForeground(INK);
        row.add(wrapCell(codeLbl, widths[0]));

        // 2. Title
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(font(Font.PLAIN, 12));
        titleLbl.setForeground(INK_2);
        row.add(wrapCell(titleLbl, widths[1]));

        // 3. Credits (CR)
        JLabel crLbl = new JLabel(cr);
        crLbl.setFont(font(Font.PLAIN, 12));
        crLbl.setForeground(INK);
        row.add(wrapCell(crLbl, widths[2]));

        // 4. Lecturer
        JLabel lecLbl = new JLabel(lecturer);
        lecLbl.setFont(font(Font.PLAIN, 12));
        lecLbl.setForeground(INK_2);
        row.add(wrapCell(lecLbl, widths[3]));

        // 5. Attendance (Progress bar + Percentage)
        JPanel attPanel = new JPanel();
        attPanel.setLayout(new BoxLayout(attPanel, BoxLayout.Y_AXIS));
        attPanel.setOpaque(false);

        String attStr = (attendance == Math.floor(attendance))
                ? String.format("%.0f%%", attendance)
                : String.format("%.1f%%", attendance);

        JLabel pctLbl = new JLabel(attStr);
        pctLbl.setFont(font(Font.BOLD, 12));
        pctLbl.setForeground(INK);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.round(attendance));
        bar.setPreferredSize(new Dimension(110, 6));
        bar.setMaximumSize(new Dimension(110, 6));
        bar.setForeground(OK);
        bar.setBackground(BRAND_SOFT);
        bar.setBorderPainted(false);

        attPanel.add(pctLbl);
        attPanel.add(Box.createVerticalStrut(4));
        attPanel.add(bar);
        row.add(wrapCell(attPanel, widths[4]));

        // 6. Grade Badge
        JPanel gradePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gradePanel.setOpaque(false);
        gradePanel.add(new Chip("● " + grade, OK));
        row.add(wrapCell(gradePanel, widths[5]));

        container.add(row);
        container.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    // Helper method to lock column widths perfectly
    private JComponent wrapCell(JComponent content, int width) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(content, BorderLayout.WEST);
        Dimension d = new Dimension(width, 40);
        p.setPreferredSize(d);
        p.setMinimumSize(d);
        p.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        return p;
    }
}