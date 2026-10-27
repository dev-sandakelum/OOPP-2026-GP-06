//
//package UI.Pages.Student;
//
//import UI.Pages.Components.Card;
//import UI.Pages.Components.Page;
//import data.UserData;
//import model.User;
//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import java.awt.*;
//
//import static UI.Kit.Palette.*;
//
//public class MarksGPA extends Page {
//
//    private final UserData userData = new UserData();
//
//    public MarksGPA(User user) {
//        super(
//                "My Attendace",
//                "View Attendance",
//                null
//        );
//
//        JPanel body = new JPanel(new BorderLayout(0, 20));
//
//        body.add(kpiRow(), BorderLayout.NORTH);
//
//        setScrollBody(body);
//    }
//
//    // KPI cards
//    private JComponent kpiRow() {
//
//        String[][] kpis = {
//                {"CA eligible", "6/6", "CA>=40 required"},
//                {"Finals sat", "6/6", "AB graded later"},
//                {"SGPA", "3.80", "18 credits . Level 11 sem 1"},
//                {"CGPA", "3.48", "inclu.Level 1 . 22 credits"}
//        };
//
//        JPanel row = new JPanel(
//                new GridLayout(1, kpis.length, 16, 0)
//        );
//
//        row.setOpaque(false);
//
//        for (int i = 0; i < kpis.length; i++) {
//            row.add(
//                    kpiCard(
//                            kpis[i][0],
//                            kpis[i][1],
//                            kpis[i][2],
//                            i == 0
//                    )
//            );
//        }
//
//        return row;
//    }
//
//    // Individual KPI card
//    private JComponent kpiCard(
//            String label,
//            String value,
//            String sub,
//            boolean dark
//    ) {
//
//        Card card = new Card(dark);
//
//        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
//        card.setBorder(new EmptyBorder(18, 20, 18, 20));
//        card.setPreferredSize(new Dimension(0, 124));
//
//        Color muted = dark
//                ? new Color(190, 215, 200)
//                : INK_2;
//
//        JLabel titleLabel = new JLabel(label);
//        titleLabel.setFont(font(Font.BOLD, 13));
//        titleLabel.setForeground(muted);
//
//        JLabel valueLabel = new JLabel(value);
//        valueLabel.setFont(font(Font.BOLD, 34));
//        valueLabel.setForeground(dark ? Color.WHITE : INK);
//
//        JLabel subLabel = new JLabel(sub);
//        subLabel.setFont(font(Font.PLAIN, 12));
//        subLabel.setForeground(muted);
//
//        card.add(titleLabel);
//        card.add(Box.createVerticalGlue());
//        card.add(valueLabel);
//        card.add(subLabel);
//
//        return card;
//    }
//}


package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import data.UserData;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import static UI.Kit.Palette.*;

public class MarksGPA extends Page {

    private final UserData userData = new UserData();

    private static final String[] COLS = {
            "MODULE", "CR", "CA / 100",
            "FINAL / 100", "TOTAL", "GRADE", "GP"
    };

    // Sample results based on the reference screenshot
    private static final Object[][] SEED = {
            {"ICT2131", "Programming with Java", 3, 76, 81, 79.0, "A", 3.70},
            {"ICT2132", "Object Oriented Programming Practicum", 3, 82, 91, 87.4, "A+", 4.00},
            {"ICT2133", "Database Management Systems", 3, 75, 96, 87.6, "A+", 4.00},
            {"ICT2134", "Data Structures & Algorithms", 3, 75, 86, 81.6, "A", 3.70},
            {"ICT2135", "Software Engineering", 3, 73, 79, 76.6, "A", 3.70},
            {"ICT2136", "Computer Networks", 3, 73, 90, 83.2, "A", 3.70}
    };

    private static final String[][] GRADES = {
            {"A+", "85–100", "4.00"},
            {"A",  "70–84",  "3.70"},
            {"A-", "65–69",  "3.30"},
            {"B+", "60–64",  "3.00"},
            {"B",  "55–59",  "2.70"},
            {"B-", "50–54",  "2.30"},
            {"C+", "45–49",  "2.00"},
            {"C",  "40–44",  "1.70"},
            {"C-", "35–39",  "1.30"},
            {"F",  "0–34",   "0.00"}
    };

    private final ReadOnlyTableModel model =
            new ReadOnlyTableModel(COLS);

    private final JTable table;

    public MarksGPA(User user) {

        super(
                "Marks & GPA",
                "View your semester results and grade point average",
                null
        );

        // Add sample semester results
        for (Object[] row : SEED) {
            Object[] tableRow = {
                    row[0] + " " + row[1],
                    row[2],
                    row[3],
                    row[4],
                    row[5],
                    row[6],
                    row[7]
            };

            model.addRow(tableRow);
        }

        table = new JTable(model);

        styleTable();

        table.getColumnModel().getColumn(0)
                .setCellRenderer(new ModuleRenderer());

        for (int i = 1; i <= 4; i++) {
            table.getColumnModel().getColumn(i)
                    .setCellRenderer(new NumberRenderer());
        }

        table.getColumnModel().getColumn(5)
                .setCellRenderer(new GradeRenderer());

        table.getColumnModel().getColumn(6)
                .setCellRenderer(new NumberRenderer());

        JPanel body = new JPanel(
                new BorderLayout(0, 20)
        );

        body.setOpaque(false);

        // Existing KPI cards at the top
        body.add(kpiRow(), BorderLayout.NORTH);

        // Results and grade scale below the cards
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(resultsSection());
        content.add(Box.createVerticalStrut(26));
        content.add(gradeScaleSection());

        body.add(content, BorderLayout.CENTER);

        setScrollBody(body);
    }

    // =================================================
    // KPI CARDS - PRESERVED
    // =================================================

    private JComponent kpiRow() {

        String[][] kpis = {
                {"CA eligible", "6/6", "CA ≥ 40 required"},
                {"Finals sat", "6/6", "AB graded later"},
                {"SGPA", "3.80", "18 credits · Level II Sem I"},
                {"CGPA", "3.48", "incl. Level I · 22 credits"}
        };

        JPanel row = new JPanel(
                new GridLayout(1, kpis.length, 14, 0)
        );

        row.setOpaque(false);

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

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                new EmptyBorder(18, 20, 18, 20)
        );

        card.setPreferredSize(
                new Dimension(0, 124)
        );

        Color muted = dark
                ? new Color(190, 215, 200)
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
        subLabel.setFont(font(Font.PLAIN, 11));
        subLabel.setForeground(muted);

        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalGlue());
        card.add(valueLabel);
        card.add(subLabel);

        return card;
    }

    // =================================================
    // SEMESTER RESULTS SECTION
    // =================================================

    private JComponent resultsSection() {

        JPanel section = new JPanel(
                new BorderLayout(0, 12)
        );

        section.setOpaque(false);

        JLabel title = new JLabel("Semester results");
        title.setFont(font(Font.BOLD, 16));
        title.setForeground(INK);

        JLabel subtitle = new JLabel(
                "Total = CA × 40% + Final × 60%"
        );

        subtitle.setFont(font(Font.PLAIN, 12));
        subtitle.setForeground(INK_2);

        JPanel headings = new JPanel(
                new GridLayout(0, 1, 0, 3)
        );

        headings.setOpaque(false);
        headings.add(title);
        headings.add(subtitle);

        section.add(headings, BorderLayout.NORTH);
        section.add(createResultsCard(), BorderLayout.CENTER);

        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        return section;
    }

    private JComponent createResultsCard() {

        JPanel card = new JPanel(new BorderLayout());

        card.setBackground(SURFACE);
        card.setBorder(
                new LineBorder(LINE, 1, true)
        );

        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(SURFACE);
        scrollPane.getViewport().setBackground(SURFACE);

        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
        );

        card.add(scrollPane, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        return card;
    }

    private void styleTable() {

        table.setFont(font(Font.PLAIN, 12));
        table.setForeground(INK);
        table.setBackground(SURFACE);

        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setSelectionBackground(BRAND_SOFT);
        table.setSelectionForeground(INK);

        table.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        JTableHeader header = table.getTableHeader();

        header.setFont(font(Font.PLAIN, 10));
        header.setForeground(INK_2);
        header.setBackground(SURFACE_2);
        header.setPreferredSize(new Dimension(0, 38));

        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        header.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, LINE
                )
        );

        int[] widths = {
                390, 55, 85, 95, 90, 75, 70
        };

        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }
    }

    // =================================================
    // MODULE NAME RENDERER
    // =================================================

    private class ModuleRenderer
            extends DefaultTableCellRenderer {

        ModuleRenderer() {
            setOpaque(true);
            setBorder(new EmptyBorder(0, 14, 0, 5));
            setFont(font(Font.PLAIN, 12));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean selected, boolean focused,
                int row, int column
        ) {

            super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column
            );

            String text = value == null ? "" : value.toString();
            int space = text.indexOf(' ');

            if (space > 0) {
                String code = text.substring(0, space);
                String title = text.substring(space + 1);

                setText("<html><b>" + code
                        + "</b> <font color='#"
                        + String.format("%06X", INK_2.getRGB() & 0xFFFFFF)
                        + "'>" + title + "</font></html>");
            } else {
                setText(text);
            }

            setForeground(INK);
            setBackground(selected ? BRAND_SOFT : SURFACE);

            return this;
        }
    }

    // =================================================
    // NUMBER RENDERER
    // =================================================

    private class NumberRenderer
            extends DefaultTableCellRenderer {

        NumberRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 4, 0, 4));
            setFont(font(Font.PLAIN, 12));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean selected, boolean focused,
                int row, int column
        ) {

            super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column
            );

            if (value instanceof Number) {
                double number = ((Number) value).doubleValue();

                if (column == 4) {
                    setText(String.format("%.1f", number));
                    setFont(font(Font.BOLD, 12));
                } else if (column == 6) {
                    setText(String.format("%.2f", number));
                } else {
                    setText(
                            number == Math.floor(number)
                                    ? String.format("%.0f", number)
                                    : String.format("%.1f", number)
                    );
                }
            }

            setForeground(INK);
            setBackground(selected ? BRAND_SOFT : SURFACE);

            return this;
        }
    }

    // =================================================
    // GRADE BADGE RENDERER
    // =================================================

    private class GradeRenderer extends JPanel
            implements TableCellRenderer {

        private final JLabel badge = new JLabel(
                "", SwingConstants.CENTER
        );

        GradeRenderer() {

            setLayout(
                    new FlowLayout(FlowLayout.CENTER, 0, 0)
            );

            setOpaque(true);

            badge.setFont(font(Font.BOLD, 10));
            badge.setForeground(OK);
            badge.setBackground(BRAND_SOFT);
            badge.setOpaque(true);

            badge.setBorder(
                    BorderFactory.createCompoundBorder(
                            new LineBorder(LINE_2, 1, true),
                            new EmptyBorder(3, 8, 3, 8)
                    )
            );

            add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean selected, boolean focused,
                int row, int column
        ) {

            badge.setText(
                    "●  " + (value == null ? "" : value.toString())
            );

            setBackground(selected ? BRAND_SOFT : SURFACE);

            return this;
        }
    }

    // =================================================
    // GRADE SCALE SECTION
    // =================================================

    private JComponent gradeScaleSection() {

        JPanel section = new JPanel(
                new BorderLayout(0, 12)
        );

        section.setOpaque(false);

        JLabel title = new JLabel("Grade scale");
        title.setFont(font(Font.BOLD, 16));
        title.setForeground(INK);

        JLabel subtitle = new JLabel(
                "UGC Circular No. 12-2024"
        );

        subtitle.setFont(font(Font.PLAIN, 12));
        subtitle.setForeground(INK_2);

        JPanel headings = new JPanel(
                new GridLayout(0, 1, 0, 3)
        );

        headings.setOpaque(false);
        headings.add(title);
        headings.add(subtitle);

        section.add(headings, BorderLayout.NORTH);
        section.add(createGradeGrid(), BorderLayout.CENTER);

        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        return section;
    }

    private JComponent createGradeGrid() {

        JPanel grid = new JPanel(
                new GridLayout(2, 5, 0, 0)
        );

        grid.setBackground(SURFACE);
        grid.setBorder(
                new LineBorder(LINE, 1, true)
        );

        for (int i = 0; i < GRADES.length; i++) {

            String[] grade = GRADES[i];

            JPanel cell = new JPanel(
                    new GridLayout(3, 1, 0, 2)
            );

            cell.setBackground(SURFACE);

            int top = i < 5 ? 10 : 10;
            int left = 12;

            cell.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(
                                    i >= 5 ? 1 : 0,
                                    i % 5 == 0 ? 0 : 1,
                                    0, 0, LINE
                            ),
                            new EmptyBorder(top, left, 8, 8)
                    )
            );

            JLabel gradeLabel = new JLabel(grade[0]);
            gradeLabel.setFont(font(Font.BOLD, 12));
            gradeLabel.setForeground(INK);

            JLabel rangeLabel = new JLabel(grade[1]);
            rangeLabel.setFont(font(Font.PLAIN, 10));
            rangeLabel.setForeground(INK_2);

            JLabel pointLabel = new JLabel(grade[2]);
            pointLabel.setFont(font(Font.PLAIN, 10));
            pointLabel.setForeground(INK_2);

            cell.add(gradeLabel);
            cell.add(rangeLabel);
            cell.add(pointLabel);

            grid.add(cell);
        }

        return grid;
    }
}

