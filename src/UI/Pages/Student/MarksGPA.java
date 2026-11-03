
package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;

import data.ResultData;
import data.ExamData;
import data.CourseOfferingData;
import data.CourseData;

import model.User;
import model.Result;
import model.Exam;
import model.CourseOffering;
import model.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static UI.Kit.Palette.*;

public class MarksGPA extends Page {

    private static final String[] COLS = {
            "MODULE", "CR", "CA / 100",
            "FINAL / 100", "TOTAL", "GRADE", "GP"
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

    private final ResultData resultData = new ResultData();
    private final ExamData examData = new ExamData();
    private final CourseOfferingData offeringData =
            new CourseOfferingData();
    private final CourseData courseData = new CourseData();

    private final ReadOnlyTableModel model =
            new ReadOnlyTableModel(COLS);

    private final JTable table;

    private final JLabel caValue = new JLabel("0");
    private final JLabel finalValue = new JLabel("0");
    private final JLabel sgpaValue = new JLabel("N/A");
    private final JLabel cgpaValue = new JLabel("N/A");

    private final JLabel caSub = new JLabel("CA marks recorded");
    private final JLabel finalSub = new JLabel("Final marks recorded");
    private final JLabel sgpaSub = new JLabel("From published results");
    private final JLabel cgpaSub = new JLabel("From published results");

    private final JLabel emptyLabel =
            new JLabel("No published results available yet.",
                    SwingConstants.CENTER);

    public MarksGPA(User user) {

        super(
                "Marks & GPA",
                "View your semester results and grade point average",
                null
        );

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

        body.add(kpiRow(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.add(resultsSection());
        content.add(Box.createVerticalStrut(26));
        content.add(gradeScaleSection());

        body.add(content, BorderLayout.CENTER);

        setScrollBody(body);

        // Load results belonging to the logged-in student.
        loadResults(user);
    }

    // =================================================
    // LOAD RESULTS FROM DATABASE
    // =================================================

    private void loadResults(User user) {

        model.setRowCount(0);

        if (user == null) {
            showError("Student information is unavailable.");
            return;
        }

        try {

            int studentId = user.getUser_id();

            List<Result> results =
                    resultData.findByStudent(studentId);

            if (results == null || results.isEmpty()) {
                updateKpis(0, 0, null, null);
                showEmptyMessage();
                return;
            }

            // Sort latest published results first.
            results.sort(
                    Comparator.comparing(
                            Result::getPublishedDate,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    )
            );

            int caCount = 0;
            int finalCount = 0;

            BigDecimal latestSGPA = null;
            BigDecimal latestCGPA = null;

            boolean firstSGPAFound = false;
            boolean firstCGPAFound = false;

            for (Result result : results) {

                if (result == null) {
                    continue;
                }

                // Display only published results.
                if (result.getPublishedDate() == null
                        || result.getResultStatus() == null
                        || result.getResultStatus().trim().isEmpty()) {
                    continue;
                }

                BigDecimal ca = result.getCaResult();
                BigDecimal finalMark = result.getFinalExamMark();
                BigDecimal total = result.getTotalMark();

                if (ca != null) {
                    caCount++;
                }

                if (finalMark != null) {
                    finalCount++;
                }

                if (!firstSGPAFound && result.getSgpa() != null) {
                    latestSGPA = result.getSgpa();
                    firstSGPAFound = true;
                }

                if (!firstCGPAFound && result.getCgpa() != null) {
                    latestCGPA = result.getCgpa();
                    firstCGPAFound = true;
                }

                // Find the exam belonging to this result.
                Exam exam =
                        examData.findById(result.getExamId());

                if (exam == null) {
                    continue;
                }

                // Find the course offering.
                CourseOffering offering =
                        offeringData.findById(exam.getOfferingId());

                if (offering == null) {
                    continue;
                }

                // Find the actual course.
                Course course =
                        courseData.findById(offering.getCourse_id());

                if (course == null) {
                    continue;
                }

                String courseCode =
                        safeText(course.getCourse_code());

                String courseName =
                        safeText(course.getCourse_name());

                String moduleName =
                        courseCode + " " + courseName;

                double credits = course.getCredit();

                String grade = "";
                Object gradePoint = "";

                if (total != null) {
                    grade = calculateGrade(total);
                    gradePoint = calculateGradePoint(total);
                }

                Object[] row = {
                        moduleName,
                        credits,
                        ca,
                        finalMark,
                        total,
                        grade,
                        gradePoint
                };

                model.addRow(row);
            }

            updateKpis(
                    caCount,
                    finalCount,
                    latestSGPA,
                    latestCGPA
            );

            if (model.getRowCount() == 0) {
                showEmptyMessage();
            } else {
                emptyLabel.setVisible(false);
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not load your marks and GPA.\n"
                            + "Please check the database connection."
            );
        }
    }

    // =================================================
    // GRADE CALCULATION
    // =================================================

    private String calculateGrade(BigDecimal total) {

        if (total == null) {
            return "";
        }

        if (total.compareTo(new BigDecimal("85")) >= 0) {
            return "A+";
        }

        if (total.compareTo(new BigDecimal("70")) >= 0) {
            return "A";
        }

        if (total.compareTo(new BigDecimal("65")) >= 0) {
            return "A-";
        }

        if (total.compareTo(new BigDecimal("60")) >= 0) {
            return "B+";
        }

        if (total.compareTo(new BigDecimal("55")) >= 0) {
            return "B";
        }

        if (total.compareTo(new BigDecimal("50")) >= 0) {
            return "B-";
        }

        if (total.compareTo(new BigDecimal("45")) >= 0) {
            return "C+";
        }

        if (total.compareTo(new BigDecimal("40")) >= 0) {
            return "C";
        }

        if (total.compareTo(new BigDecimal("35")) >= 0) {
            return "C-";
        }

        return "F";
    }

    private String calculateGradePoint(BigDecimal total) {

        String grade = calculateGrade(total);

        for (String[] item : GRADES) {

            if (item[0].equals(grade)) {
                return item[2];
            }
        }

        return "";
    }

    // =================================================
    // UPDATE KPI CARDS
    // =================================================

    private void updateKpis(
            int caCount,
            int finalCount,
            BigDecimal sgpa,
            BigDecimal cgpa
    ) {

        caValue.setText(String.valueOf(caCount));
        finalValue.setText(String.valueOf(finalCount));

        sgpaValue.setText(
                sgpa == null ? "N/A" : formatGpa(sgpa)
        );

        cgpaValue.setText(
                cgpa == null ? "N/A" : formatGpa(cgpa)
        );
    }

    private String formatGpa(BigDecimal value) {
        return value.setScale(
                2,
                java.math.RoundingMode.HALF_UP
        ).toPlainString();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void showEmptyMessage() {

        emptyLabel.setFont(font(Font.PLAIN, 13));
        emptyLabel.setForeground(INK_2);
        emptyLabel.setBorder(
                new EmptyBorder(25, 10, 25, 10)
        );

        model.setRowCount(0);
    }

    private void showError(String message) {

        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        this,
                        message,
                        "Marks & GPA",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );
    }

    // =================================================
    // KPI CARDS
    // =================================================

    private JComponent kpiRow() {

        JPanel row = new JPanel(
                new GridLayout(1, 4, 14, 0)
        );

        row.setOpaque(false);

        row.add(kpiCard(
                "CA recorded",
                caValue,
                caSub,
                true
        ));

        row.add(kpiCard(
                "Finals sat",
                finalValue,
                finalSub,
                false
        ));

        row.add(kpiCard(
                "SGPA",
                sgpaValue,
                sgpaSub,
                false
        ));

        row.add(kpiCard(
                "CGPA",
                cgpaValue,
                cgpaSub,
                false
        ));

        return row;
    }

    private JComponent kpiCard(
            String label,
            JLabel valueLabel,
            JLabel subLabel,
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

        Color muted = dark ? ON_BRAND_2 : INK_2;

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(font(Font.BOLD, 13));
        titleLabel.setForeground(muted);

        valueLabel.setFont(font(Font.BOLD, 34));
        valueLabel.setForeground(
                dark ? Color.WHITE : INK
        );

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
    // RESULTS SECTION
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
                "Marks and total values retrieved from published results"
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

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scrollPane.setBackground(SURFACE);
        scrollPane.getViewport().setBackground(SURFACE);

        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(SURFACE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Empty state message appears when there are no results.
        tableContainer.add(emptyLabel, BorderLayout.SOUTH);
        emptyLabel.setVisible(false);

        card.add(tableContainer, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        return card;
    }

    // =================================================
    // TABLE STYLE
    // =================================================

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
                JTable table,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column
        ) {

            super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column
            );

            String text = value == null ? "" : value.toString();
            int space = text.indexOf(' ');

            if (space > 0) {

                String code = text.substring(0, space);
                String title = text.substring(space + 1);

                setText(
                        "<html><b>" + code
                                + "</b> <font color='#"
                                + String.format(
                                "%06X",
                                INK_2.getRGB() & 0xFFFFFF
                        )
                                + "'>" + title
                                + "</font></html>"
                );

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
                JTable table,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column
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

            } else if (value == null) {
                setText("-");
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
                JTable table,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column
        ) {

            badge.setText(
                    value == null || value.toString().isEmpty()
                            ? "-"
                            : "●  " + value
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
                "Grade and grade points"
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

            cell.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(
                                    i >= 5 ? 1 : 0,
                                    i % 5 == 0 ? 0 : 1,
                                    0, 0, LINE
                            ),
                            new EmptyBorder(10, 12, 8, 8)
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
