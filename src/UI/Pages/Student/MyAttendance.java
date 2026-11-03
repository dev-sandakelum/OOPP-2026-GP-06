
package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;

import data.AttendanceData;
import data.EnrollmentData;
import data.SessionData;
import data.CourseOfferingData;
import data.CourseData;

import model.User;
import model.Attendance;
import model.Enrollment;
import model.Session;
import model.CourseOffering;
import model.Course;

import Exception.DataAccessException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

import static UI.Kit.Palette.*;

public class MyAttendance extends Page {

    private final User user;

    private final AttendanceData attendanceData = new AttendanceData();
    private final EnrollmentData enrollmentData = new EnrollmentData();
    private final SessionData sessionData = new SessionData();
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();

    private JLabel overallValue;
    private JLabel eligibleValue;
    private JLabel medicalValue;

    private JPanel tableCard;

    private int totalAttended = 0;
    private int totalCounted = 0;
    private int medicalCount = 0;
    private int eligibleModules = 0;
    private int totalModules = 0;

    public MyAttendance(User user) {
        super("My Attendance", "View Attendance", null);

        this.user = user;

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JComponent kpis = kpiRow();
        kpis.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(kpis);

        body.add(Box.createVerticalStrut(25));

        JComponent register = registerSection();
        register.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(register);

        setScrollBody(body);

        loadAttendance();
    }

    // ---------------- KPI CARDS ----------------

    private JComponent kpiRow() {

        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));

        overallValue = new JLabel("0.0%");
        eligibleValue = new JLabel("0/0");
        medicalValue = new JLabel("0");

        row.add(kpiCard(
                "Overall attendance",
                overallValue,
                "Based on recorded sessions",
                true
        ));

        row.add(kpiCard(
                "Modules ≥ 80%",
                eligibleValue,
                "Eligibility threshold",
                false
        ));

        row.add(kpiCard(
                "Medical records",
                medicalValue,
                "Recorded medical sessions",
                false
        ));

        return row;
    }

    private JComponent kpiCard(
            String label,
            JLabel valueLabel,
            String sub,
            boolean dark) {

        Card card = new Card(dark);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        Color muted = dark ? ON_BRAND_2 : INK_2;

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(font(Font.BOLD, 13));
        titleLabel.setForeground(muted);

        valueLabel.setFont(font(Font.BOLD, 34));
        valueLabel.setForeground(dark ? Color.WHITE : INK);

        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(font(Font.PLAIN, 12));
        subLabel.setForeground(muted);

        card.add(titleLabel);
        card.add(Box.createVerticalGlue());
        card.add(valueLabel);
        card.add(subLabel);

        return card;
    }

    // ---------------- REGISTER SECTION ----------------

    private JComponent registerSection() {

        JPanel section = new JPanel(new BorderLayout(0, 15));
        section.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("My register");
        title.setFont(font(Font.BOLD, 18));
        title.setForeground(INK);

        JLabel subtitle = new JLabel(
                "Attendance records for your enrolled modules"
        );
        subtitle.setFont(font(Font.PLAIN, 13));
        subtitle.setForeground(INK_2);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel legend = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 15, 0)
        );
        legend.setOpaque(false);

        legend.add(legendItem("Present", OK));
        legend.add(legendItem("Late", WARN));
        legend.add(legendItem("Absent", new Color(245, 200, 200)));
        legend.add(legendItem("Medical", new Color(110, 145, 210)));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(legend, BorderLayout.EAST);

        tableCard = new Card(false);
        tableCard.setLayout(new BoxLayout(tableCard, BoxLayout.Y_AXIS));
        tableCard.setBorder(new EmptyBorder(10, 20, 20, 20));

        addTableHeader();

        section.add(header, BorderLayout.NORTH);
        section.add(tableCard, BorderLayout.CENTER);

        return section;
    }

    private void addTableHeader() {

        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.X_AXIS));
        headerRow.setOpaque(false);
        headerRow.setBorder(new EmptyBorder(10, 0, 15, 0));

        String[] cols = {
                "MODULE",
                "THEORY",
                "PRACTICAL",
                "COMBINED",
                "STATUS"
        };

        int[] widths = {240, 200, 200, 100, 100};

        for (int i = 0; i < cols.length; i++) {

            JLabel label = new JLabel(cols[i]);
            label.setFont(font(Font.BOLD, 11));
            label.setForeground(INK_2);

            headerRow.add(wrapCell(label, widths[i]));
        }

        tableCard.add(headerRow);
        tableCard.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    // ---------------- DATABASE LOADING ----------------

    private void loadAttendance() {

        resetTotals();

        if (user == null) {
            showEmptyMessage("Student information is not available.");
            return;
        }

        try {

            int studentId = user.getUser_id();

            List<Enrollment> enrollments =
                    enrollmentData.findByStudent(studentId);

            if (enrollments == null || enrollments.isEmpty()) {
                updateKpis();
                showEmptyMessage("You have no enrolled courses.");
                return;
            }

            Set<Integer> processedOfferings = new HashSet<>();

            for (Enrollment enrollment : enrollments) {

                if (enrollment == null) {
                    continue;
                }

                if (!"ENROLLED".equalsIgnoreCase(
                        enrollment.getEnrollment_status())) {
                    continue;
                }

                int offeringId = enrollment.getOffering_id();

                if (!processedOfferings.add(offeringId)) {
                    continue;
                }

                loadOfferingAttendance(offeringId);
            }

            updateKpis();

            if (totalModules == 0) {
                showEmptyMessage("No enrolled modules found.");
            }

        } catch (DataAccessException e) {

            showError("Unable to load attendance data.\n" + e.getMessage());

        } catch (Exception e) {

            showError("An unexpected error occurred.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadOfferingAttendance(int offeringId) {

        CourseOffering offering =
                offeringData.findById(offeringId);

        if (offering == null) {
            return;
        }

        Course course =
                courseData.findById(offering.getCourse_id());

        if (course == null) {
            return;
        }

        List<Session> sessions =
                sessionData.findByOffering(offeringId);

        List<Attendance> records =
                attendanceData.findByStudentAndOffering(
                        user.getUser_id(),
                        offeringId
                );

        if (sessions == null) {
            sessions = Collections.emptyList();
        }

        if (records == null) {
            records = Collections.emptyList();
        }

        Map<Integer, Attendance> attendanceMap = new HashMap<>();

        for (Attendance attendance : records) {

            if (attendance != null) {
                attendanceMap.put(
                        attendance.getSessionId(),
                        attendance
                );
            }
        }

        ModuleStats stats = new ModuleStats();

        for (Session session : sessions) {

            if (session == null) {
                continue;
            }

            Attendance attendance =
                    attendanceMap.get(session.getSessionId());

            // Ignore sessions without a recorded attendance record.
            if (attendance == null) {
                continue;
            }

            String status = attendance.getAttendStatus();

            if (status == null) {
                continue;
            }

            status = status.trim().toUpperCase();

            String type = session.getSessionType();

            if (type == null) {
                continue;
            }

            type = type.trim().toUpperCase();

            boolean theory = type.equals("LECTURE")
                    || type.equals("THEORY");

            boolean practical = type.equals("PRACTICAL");

            if (!theory && !practical) {
                continue;
            }

            if (status.equals("MEDICAL")) {

                medicalCount++;

                if (theory) {
                    stats.theoryMedical++;
                } else {
                    stats.practicalMedical++;
                }

                continue;
            }

            if (!status.equals("PRESENT")
                    && !status.equals("ABSENT")
                    && !status.equals("LATE")) {
                continue;
            }

            if (theory) {

                stats.theoryTotal++;

                if (status.equals("PRESENT")
                        || status.equals("LATE")) {
                    stats.theoryAttended++;
                }

                stats.theoryPattern.append(
                        getPatternCharacter(status)
                );

            } else {

                stats.practicalTotal++;

                if (status.equals("PRESENT")
                        || status.equals("LATE")) {
                    stats.practicalAttended++;
                }

                stats.practicalPattern.append(
                        getPatternCharacter(status)
                );
            }
        }

        int moduleTotal =
                stats.theoryTotal + stats.practicalTotal;

        if (moduleTotal == 0
                && stats.theoryMedical + stats.practicalMedical == 0) {
            return;
        }

        totalModules++;

        int moduleAttended =
                stats.theoryAttended + stats.practicalAttended;

        totalAttended += moduleAttended;
        totalCounted += moduleTotal;

        double percentage = moduleTotal == 0
                ? 0
                : (moduleAttended * 100.0 / moduleTotal);

        if (percentage >= 80.0 && moduleTotal > 0) {
            eligibleModules++;
        }

        String theoryText = getAttendanceText(
                stats.theoryAttended,
                stats.theoryTotal
        );

        String practicalText = getAttendanceText(
                stats.practicalAttended,
                stats.practicalTotal
        );

        addTableRow(
                course.getCourse_code(),
                course.getCourse_name(),
                stats.theoryPattern.toString(),
                theoryText,
                stats.practicalPattern.toString(),
                practicalText,
                percentage,
                moduleTotal
        );
    }

    // ---------------- CALCULATIONS ----------------

    private char getPatternCharacter(String status) {

        switch (status) {

            case "PRESENT":
                return 'P';

            case "LATE":
                return 'L';

            case "ABSENT":
                return 'A';

            default:
                return '?';
        }
    }

    private String getAttendanceText(int attended, int total) {

        if (total == 0) {
            return "No records";
        }

        double percentage = attended * 100.0 / total;

        return attended + "/" + total
                + " · " + String.format(Locale.US, "%.1f", percentage)
                + "%";
    }

    private void updateKpis() {

        double overall = totalCounted == 0
                ? 0
                : totalAttended * 100.0 / totalCounted;

        overallValue.setText(
                String.format(Locale.US, "%.1f%%", overall)
        );

        eligibleValue.setText(
                eligibleModules + "/" + totalModules
        );

        medicalValue.setText(String.valueOf(medicalCount));
    }

    private void resetTotals() {

        totalAttended = 0;
        totalCounted = 0;
        medicalCount = 0;
        eligibleModules = 0;
        totalModules = 0;
    }

    // ---------------- TABLE ROW ----------------

    private void addTableRow(
            String code,
            String name,
            String theoryDots,
            String theoryText,
            String practicalDots,
            String practicalText,
            double combined,
            int totalSessions) {

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Module
        JPanel modulePanel = new JPanel();
        modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.Y_AXIS));
        modulePanel.setOpaque(false);

        JLabel codeLabel = new JLabel(code);
        codeLabel.setFont(font(Font.BOLD, 13));
        codeLabel.setForeground(INK);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(font(Font.PLAIN, 12));
        nameLabel.setForeground(INK_2);

        modulePanel.add(codeLabel);
        modulePanel.add(nameLabel);

        row.add(wrapCell(modulePanel, 240));

        // Theory
        row.add(wrapCell(
                createDotsPanel(theoryDots, theoryText), 200
        ));

        // Practical
        row.add(wrapCell(
                createDotsPanel(practicalDots, practicalText), 200
        ));

        // Combined
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BoxLayout(
                combinedPanel, BoxLayout.Y_AXIS
        ));
        combinedPanel.setOpaque(false);

        JLabel percentageLabel = new JLabel(
                String.format(Locale.US, "%.1f%%", combined)
        );

        percentageLabel.setFont(font(Font.BOLD, 13));
        percentageLabel.setForeground(
                combined >= 80 ? OK : WARN
        );

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.round(combined));
        bar.setPreferredSize(new Dimension(80, 6));
        bar.setMaximumSize(new Dimension(80, 6));
        bar.setForeground(combined >= 80 ? OK : WARN);
        bar.setBackground(BRAND_SOFT);
        bar.setBorderPainted(false);

        combinedPanel.add(Box.createVerticalStrut(5));
        combinedPanel.add(percentageLabel);
        combinedPanel.add(Box.createVerticalStrut(5));
        combinedPanel.add(bar);

        row.add(wrapCell(combinedPanel, 100));

        // Status
        JPanel statusPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 10)
        );
        statusPanel.setOpaque(false);

        String status = totalSessions == 0
                ? "No records"
                : combined >= 80 ? "Eligible" : "Below 80%";

        Color statusColor = totalSessions == 0
                ? INK_2
                : combined >= 80 ? OK : WARN;

        statusPanel.add(new Chip(status, statusColor));

        row.add(wrapCell(statusPanel, 100));

        tableCard.add(row);
        tableCard.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    // ---------------- DOTS ----------------

    private JPanel createDotsPanel(String pattern, String text) {

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);

        if (pattern != null && !pattern.isEmpty()) {
            panel.add(new DotDrawer(pattern), BorderLayout.NORTH);
        } else {
            panel.add(new JLabel(" "), BorderLayout.NORTH);
        }

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(font(Font.PLAIN, 12));
        textLabel.setForeground(INK_2);

        panel.add(textLabel, BorderLayout.CENTER);

        return panel;
    }

    private JComponent legendItem(String text, Color color) {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0)
        );
        panel.setOpaque(false);

        JLabel dot = new JLabel("■");
        dot.setForeground(color);
        dot.setFont(font(Font.PLAIN, 10));

        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 12));
        label.setForeground(INK_2);

        panel.add(dot);
        panel.add(label);

        return panel;
    }

    private JComponent wrapCell(JComponent content, int width) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(content, BorderLayout.WEST);

        Dimension dimension = new Dimension(width, 40);

        panel.setPreferredSize(dimension);
        panel.setMinimumSize(dimension);
        panel.setMaximumSize(
                new Dimension(width, Integer.MAX_VALUE)
        );

        return panel;
    }

    // ---------------- EMPTY / ERROR MESSAGES ----------------

    private void showEmptyMessage(String message) {

        JLabel label = new JLabel(message);
        label.setFont(font(Font.PLAIN, 14));
        label.setForeground(INK_2);
        label.setBorder(new EmptyBorder(25, 5, 25, 5));

        tableCard.add(label);
        tableCard.revalidate();
        tableCard.repaint();
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Attendance Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // ---------------- MODULE STATISTICS ----------------

    private static class ModuleStats {

        int theoryTotal = 0;
        int theoryAttended = 0;

        int practicalTotal = 0;
        int practicalAttended = 0;

        int theoryMedical = 0;
        int practicalMedical = 0;

        StringBuilder theoryPattern = new StringBuilder();
        StringBuilder practicalPattern = new StringBuilder();
    }

    // ---------------- CUSTOM DOT DRAWER ----------------

    private static class DotDrawer extends JComponent {

        private final String pattern;

        public DotDrawer(String pattern) {

            this.pattern = pattern;

            setPreferredSize(new Dimension(185, 12));
            setMinimumSize(new Dimension(185, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int x = 0;

            for (char c : pattern.toCharArray()) {

                if (c == 'P') {
                    g2.setColor(OK);
                } else if (c == 'L') {
                    g2.setColor(WARN);
                } else if (c == 'A') {
                    g2.setColor(new Color(245, 200, 200));
                } else {
                    continue;
                }

                g2.fillOval(x, 0, 8, 8);
                x += 12;
            }

            g2.dispose();
        }
    }
}
