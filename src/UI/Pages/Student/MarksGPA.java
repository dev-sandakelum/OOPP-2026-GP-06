
package UI.Pages.Student;

import UI.Kit.Glass;
import UI.Pages.Components.AccentBar;
import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.HeaderRenderer;
import UI.Pages.Components.IconBadge;
import UI.Pages.Components.NavIcon;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;

import data.ResultData;
import data.ExamData;
import data.CourseOfferingData;
import data.CourseData;
import data.SemesterData;

import model.User;
import model.Result;
import model.Exam;
import model.CourseOffering;
import model.Course;
import model.Semester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static UI.Kit.Palette.*;

// Student "Marks & GPA" page: KPI row, one glass card per semester with that
// semester's results, then the grade scale.
public class MarksGPA extends Page {

    private static final String[] COLS = {
            "MODULE", "CR", "CA / 100",
            "FINAL / 100", "TOTAL", "GRADE", "GP"
    };

    // column indexes
    private static final int COL_MODULE = 0, COL_TOTAL = 4, COL_GRADE = 5, COL_GP = 6;

    private static final int ROW_HEIGHT = 44;

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
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final SemesterData semesterData = new SemesterData();

    private final JLabel caValue = new JLabel("0");
    private final JLabel finalValue = new JLabel("0");
    private final JLabel sgpaValue = new JLabel("N/A");
    private final JLabel cgpaValue = new JLabel("N/A");

    private final JLabel caSub = new JLabel("CA marks recorded");
    private final JLabel finalSub = new JLabel("Final marks recorded");
    private final JLabel sgpaSub = new JLabel("Latest semester");
    private final JLabel cgpaSub = new JLabel("From published results");

    /** Holds one card per semester (filled by loadResults). */
    private final JPanel semestersPanel = new JPanel();

    public MarksGPA(User user) {

        super(
                "Marks & GPA",
                "View your semester results and grade point average",
                null
        );

        semestersPanel.setOpaque(false);
        semestersPanel.setLayout(new BoxLayout(semestersPanel, BoxLayout.Y_AXIS));
        semestersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(kpiRow(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(sectionHeading("Semester results",
                "Published results, grouped by semester (latest first)"));
        content.add(Box.createVerticalStrut(12));
        content.add(semestersPanel);
        content.add(Box.createVerticalStrut(26));
        content.add(sectionHeading("Grade scale", "Grade and grade points"));
        content.add(Box.createVerticalStrut(12));
        content.add(createGradeGrid());

        body.add(content, BorderLayout.CENTER);

        setScrollBody(body);

        // Load results belonging to the logged-in student.
        loadResults(user);
    }

    // =================================================
    // LOAD RESULTS FROM DATABASE
    // =================================================

    /** Results of one semester. */
    private static final class SemesterGroup {
        final Semester semester;            // null = semester unknown
        final List<Object[]> rows = new ArrayList<>();
        BigDecimal sgpa;
        double credits;

        SemesterGroup(Semester semester) {
            this.semester = semester;
        }
    }

    private void loadResults(User user) {

        semestersPanel.removeAll();

        if (user == null) {
            showEmptyMessage();
            showError("Student information is unavailable.");
            return;
        }

        try {

            List<Result> results = resultData.findByStudent(user.getUser_id());

            if (results == null || results.isEmpty()) {
                updateKpis(0, 0, null, null);
                showEmptyMessage();
                return;
            }

            // Latest published results first.
            results.sort(
                    Comparator.comparing(
                            Result::getPublishedDate,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );

            int caCount = 0;
            int finalCount = 0;

            BigDecimal latestSGPA = null;
            BigDecimal latestCGPA = null;

            // semester_id -> group (0 = unknown semester)
            Map<Integer, SemesterGroup> groups = new LinkedHashMap<>();
            Map<Integer, Semester> semesterCache = new HashMap<>();

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

                if (ca != null) caCount++;
                if (finalMark != null) finalCount++;

                if (latestSGPA == null && result.getSgpa() != null) latestSGPA = result.getSgpa();
                if (latestCGPA == null && result.getCgpa() != null) latestCGPA = result.getCgpa();

                Exam exam = examData.findById(result.getExamId());
                if (exam == null) continue;

                CourseOffering offering = offeringData.findById(exam.getOfferingId());
                if (offering == null) continue;

                Course course = courseData.findById(offering.getCourse_id());
                if (course == null) continue;

                int semesterId = offering.getSemester_id();
                Semester semester = semesterCache.computeIfAbsent(semesterId, this::loadSemester);
                SemesterGroup group = groups.computeIfAbsent(
                        semester == null ? 0 : semesterId, id -> new SemesterGroup(semester));

                String grade = total == null ? "" : calculateGrade(total);
                Object gradePoint = total == null ? "" : calculateGradePoint(total);

                group.rows.add(new Object[]{
                        safeText(course.getCourse_code()) + " " + safeText(course.getCourse_name()),
                        course.getCredit(),
                        ca,
                        finalMark,
                        total,
                        grade,
                        gradePoint
                });
                group.credits += course.getCredit();
                if (group.sgpa == null && result.getSgpa() != null) group.sgpa = result.getSgpa();
            }

            updateKpis(caCount, finalCount, latestSGPA, latestCGPA);

            if (groups.isEmpty()) {
                showEmptyMessage();
                return;
            }

            // Latest semester first; unknown semester last.
            List<SemesterGroup> ordered = new ArrayList<>(groups.values());
            ordered.sort(Comparator.comparing(
                    (SemesterGroup g) -> g.semester,
                    Comparator.nullsLast(
                            Comparator.comparing(Semester::getStart_date,
                                            Comparator.nullsLast(Comparator.naturalOrder()))
                                    .thenComparing(Semester::getSemester_number)
                                    .reversed())));

            for (int i = 0; i < ordered.size(); i++) {
                if (i > 0) semestersPanel.add(Box.createVerticalStrut(16));
                semestersPanel.add(semesterCard(ordered.get(i)));
            }

        } catch (Exception e) {

            e.printStackTrace();
            showEmptyMessage();

            showError(
                    "Could not load your marks and GPA.\n"
                            + "Please check the database connection."
            );
        } finally {
            semestersPanel.revalidate();
            semestersPanel.repaint();
        }
    }

    private Semester loadSemester(int semesterId) {
        try {
            return semesterData.findById(semesterId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =================================================
    // GRADE CALCULATION
    // =================================================

    private String calculateGrade(BigDecimal total) {

        if (total == null) return "";

        if (total.compareTo(new BigDecimal("85")) >= 0) return "A+";
        if (total.compareTo(new BigDecimal("70")) >= 0) return "A";
        if (total.compareTo(new BigDecimal("65")) >= 0) return "A-";
        if (total.compareTo(new BigDecimal("60")) >= 0) return "B+";
        if (total.compareTo(new BigDecimal("55")) >= 0) return "B";
        if (total.compareTo(new BigDecimal("50")) >= 0) return "B-";
        if (total.compareTo(new BigDecimal("45")) >= 0) return "C+";
        if (total.compareTo(new BigDecimal("40")) >= 0) return "C";
        if (total.compareTo(new BigDecimal("35")) >= 0) return "C-";

        return "F";
    }

    private String calculateGradePoint(BigDecimal total) {

        String grade = calculateGrade(total);

        for (String[] item : GRADES) {
            if (item[0].equals(grade)) return item[2];
        }

        return "";
    }

    /** A = green, B = blue, C = amber, F = red. */
    private static Color gradeColor(String grade) {
        if (grade == null || grade.isEmpty()) return INK_2;
        return switch (grade.charAt(0)) {
            case 'A' -> OK;
            case 'B' -> BRAND;
            case 'C' -> WARN;
            default  -> BAD;
        };
    }

    // =================================================
    // UPDATE KPI CARDS
    // =================================================

    private void updateKpis(int caCount, int finalCount, BigDecimal sgpa, BigDecimal cgpa) {
        caValue.setText(String.valueOf(caCount));
        finalValue.setText(String.valueOf(finalCount));
        sgpaValue.setText(sgpa == null ? "N/A" : formatGpa(sgpa));
        cgpaValue.setText(cgpa == null ? "N/A" : formatGpa(cgpa));
    }

    private String formatGpa(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatCredits(double credits) {
        return credits == Math.floor(credits)
                ? String.format("%.0f", credits)
                : String.format("%.1f", credits);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void showEmptyMessage() {
        semestersPanel.removeAll();

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(28, 24, 32, 24));

        JLabel label = new JLabel("No published results available yet.", SwingConstants.CENTER);
        label.setFont(font(Font.PLAIN, 13));
        label.setForeground(INK_2);
        card.add(label, BorderLayout.CENTER);

        semestersPanel.add(fullWidth(card));
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

        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        row.add(kpiCard("CA recorded", caValue, caSub, true));
        row.add(kpiCard("Finals sat", finalValue, finalSub, false));
        row.add(kpiCard("SGPA", sgpaValue, sgpaSub, false));
        row.add(kpiCard("CGPA", cgpaValue, cgpaSub, false));

        return row;
    }

    private JComponent kpiCard(String label, JLabel valueLabel, JLabel subLabel, boolean dark) {

        Card card = new Card(dark);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setPreferredSize(new Dimension(0, 124));

        Color muted = dark ? ON_BRAND_2 : INK_2;

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(font(Font.BOLD, 13));
        titleLabel.setForeground(muted);

        valueLabel.setFont(font(Font.BOLD, 34));
        valueLabel.setForeground(dark ? ON_BRAND : INK);

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
    // SECTION HEADINGS
    // =================================================

    private JComponent sectionHeading(String title, String subtitle) {

        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 16));
        t.setForeground(INK);

        JLabel s = new JLabel(subtitle);
        s.setFont(font(Font.PLAIN, 12));
        s.setForeground(INK_2);

        JPanel headings = new JPanel(new GridLayout(0, 1, 0, 3));
        headings.setOpaque(false);
        headings.add(t);
        headings.add(s);

        return fullWidth(headings);
    }

    /** Lets a component stretch to the full width inside a vertical BoxLayout. */
    private static JComponent fullWidth(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    // =================================================
    // ONE SEMESTER CARD
    // =================================================

    private JComponent semesterCard(SemesterGroup group) {

        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 22, 20, 22));

        card.add(semesterHeader(group), BorderLayout.NORTH);
        card.add(semesterTable(group), BorderLayout.CENTER);

        return fullWidth(card);
    }

    private JComponent semesterHeader(SemesterGroup group) {

        Semester s = group.semester;

        String title = s == null ? "Other results" : "Semester " + s.getSemester_number();

        StringBuilder sub = new StringBuilder();
        if (s != null) {
            sub.append(safeText(s.getAcademic_year()));
            String status = safeText(s.getStatus()).trim();
            if (!status.isEmpty()) {
                sub.append("  ·  ")
                   .append(status.substring(0, 1).toUpperCase())
                   .append(status.substring(1).toLowerCase());
            }
        } else {
            sub.append("Semester details unavailable");
        }

        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 17));
        t.setForeground(INK);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        AccentBar bar = new AccentBar(28, 3);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(sub.toString());
        subLabel.setFont(font(Font.PLAIN, 12));
        subLabel.setForeground(INK_2);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(t);
        text.add(Box.createVerticalStrut(5));
        text.add(bar);
        text.add(Box.createVerticalStrut(6));
        text.add(subLabel);

        JPanel badge = new JPanel(new GridBagLayout());
        badge.setOpaque(false);
        badge.add(new IconBadge(NavIcon.GRADES, IconBadge.Style.SOLID, 42));

        // right side: module count, credits, SGPA
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stats.setOpaque(false);
        int modules = group.rows.size();
        stats.add(new Chip(modules + (modules == 1 ? " module" : " modules"), INK_2));
        stats.add(new Chip(formatCredits(group.credits) + " credits", INK_2));
        stats.add(new Chip("SGPA  " + (group.sgpa == null ? "N/A" : formatGpa(group.sgpa)), BRAND, true));

        JPanel statsWrap = new JPanel(new GridBagLayout());   // vertically centre the chips
        statsWrap.setOpaque(false);
        statsWrap.add(stats);

        JPanel head = new JPanel(new BorderLayout(14, 0));
        head.setOpaque(false);
        head.add(badge, BorderLayout.WEST);
        head.add(text, BorderLayout.CENTER);
        head.add(statsWrap, BorderLayout.EAST);
        return head;
    }

    private JComponent semesterTable(SemesterGroup group) {

        ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
        group.rows.sort(Comparator.comparing(r -> String.valueOf(r[COL_MODULE])));
        for (Object[] row : group.rows) model.addRow(row);

        JTable table = new JTable(model);
        table.setFont(font(Font.PLAIN, 13));
        table.setForeground(INK);
        table.setOpaque(false);
        table.setRowHeight(ROW_HEIGHT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(LINE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(BRAND_SOFT);
        table.setSelectionForeground(INK);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new CenteredHeaderRenderer());

        int[] widths = {390, 55, 85, 95, 90, 85, 70};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(
                    i == COL_MODULE ? new ModuleRenderer()
                  : i == COL_GRADE  ? new GradeChipRenderer()
                  :                   new NumberRenderer());
        }

        // no scroll pane: the card grows with its rows and the page scrolls instead
        JPanel box = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {   // soft rounded panel behind the table
                Graphics2D g2 = Glass.aa(g);
                g2.setColor(Glass.alpha(SURFACE, 150));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(LINE);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(1, 1, 1, 1));
        box.add(header, BorderLayout.NORTH);
        box.add(table, BorderLayout.CENTER);
        return box;
    }

    // =================================================
    // TABLE RENDERERS
    // =================================================

    /** Shared header style; every column except MODULE is centred. */
    private static class CenteredHeaderRenderer extends HeaderRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean selected, boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, col);
            l.setFont(font(Font.BOLD, 11));
            l.setHorizontalAlignment(col == COL_MODULE ? SwingConstants.LEFT : SwingConstants.CENTER);
            return l;
        }
    }

    /** Transparent cell on the glass card, soft blue when selected. */
    private static class GlassCell extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean selected, boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setForeground(INK);
            setBackground(BRAND_SOFT);
            setOpaque(selected);
            return this;
        }
    }

    /** "ICT1123 Discrete Mathematics": bold code + muted name. */
    private static class ModuleRenderer extends GlassCell {

        private static final String MUTED_HEX = String.format("%06X", INK_2.getRGB() & 0xFFFFFF);

        ModuleRenderer() {
            setBorder(new EmptyBorder(0, 16, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean selected, boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            setBorder(new EmptyBorder(0, 16, 0, 8));

            String text = value == null ? "" : value.toString();
            int space = text.indexOf(' ');
            if (space > 0) {
                setText("<html><b>" + text.substring(0, space) + "</b>&nbsp; <font color='#"
                        + MUTED_HEX + "'>" + text.substring(space + 1) + "</font></html>");
            } else {
                setText(text);
            }
            return this;
        }
    }

    private static class NumberRenderer extends GlassCell {

        NumberRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean selected, boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            setBorder(new EmptyBorder(0, 4, 0, 4));
            setFont(font(column == COL_TOTAL ? Font.BOLD : Font.PLAIN, 13));

            if (value instanceof Number n) {
                double number = n.doubleValue();
                if (column == COL_TOTAL) {
                    setText(String.format("%.1f", number));
                } else if (column == COL_GP) {
                    setText(String.format("%.2f", number));
                } else {
                    setText(number == Math.floor(number)
                            ? String.format("%.0f", number)
                            : String.format("%.1f", number));
                }
            } else if (value == null || value.toString().isEmpty()) {
                setText("-");
            }
            return this;
        }
    }

    /** Grade shown as a tinted pill with a dot, coloured by grade letter. */
    private static class GradeChipRenderer extends JLabel implements TableCellRenderer {

        private Color color = INK_2;
        private boolean selected;

        GradeChipRenderer() {
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean selected, boolean focused, int row, int column) {
            String grade = value == null ? "" : value.toString();
            setText(grade);
            color = gradeColor(grade);
            this.selected = selected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Glass.aa(g);
            if (selected) {
                g2.setColor(BRAND_SOFT);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            String text = getText();
            g2.setFont(font(Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();

            if (text.isEmpty()) {           // no grade yet
                g2.setColor(INK_2);
                g2.drawString("-", (getWidth() - fm.stringWidth("-")) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
                return;
            }

            int dot = 6, gap = 6, padX = 10, h = 24;
            int w = padX + dot + gap + fm.stringWidth(text) + padX;
            int x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;

            g2.setColor(Glass.alpha(color, 34));
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, 12, 12));
            g2.setColor(color);
            g2.fill(new Ellipse2D.Float(x + padX, y + (h - dot) / 2f, dot, dot));
            g2.drawString(text, x + padX + dot + gap, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // =================================================
    // GRADE SCALE
    // =================================================

    private JComponent createGradeGrid() {

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 16, 16, 16));

        JPanel grid = new JPanel(new GridLayout(2, 5, 10, 10));
        grid.setOpaque(false);

        for (String[] grade : GRADES) {

            Color c = gradeColor(grade[0]);

            JPanel cell = new JPanel(new GridLayout(3, 1, 0, 2)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = Glass.aa(g);
                    g2.setColor(Glass.alpha(c, 22));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                    g2.dispose();
                }
            };
            cell.setOpaque(false);
            cell.setBorder(new EmptyBorder(10, 14, 10, 10));

            JLabel gradeLabel = new JLabel(grade[0]);
            gradeLabel.setFont(font(Font.BOLD, 15));
            gradeLabel.setForeground(c);

            JLabel rangeLabel = new JLabel(grade[1] + " marks");
            rangeLabel.setFont(font(Font.PLAIN, 11));
            rangeLabel.setForeground(INK_2);

            JLabel pointLabel = new JLabel(grade[2] + " GP");
            pointLabel.setFont(font(Font.PLAIN, 11));
            pointLabel.setForeground(INK);

            cell.add(gradeLabel);
            cell.add(rangeLabel);
            cell.add(pointLabel);

            grid.add(cell);
        }

        card.add(grid, BorderLayout.CENTER);
        return fullWidth(card);
    }
}
