
package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import UI.Pages.Components.RoundedButton;

import data.EligibilityData;
import data.ResultData;
import data.ExamData;
import data.CourseOfferingData;
import data.CourseData;
import data.MedicalData;
import data.NoticeData;

import model.User;
import model.Eligibility;
import model.Result;
import model.Exam;
import model.CourseOffering;
import model.Course;
import model.Medical;
import model.Notice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static UI.Kit.Palette.*;

public class Overview extends Page {

    private final User user;

    private final EligibilityData eligibilityData = new EligibilityData();
    private final ResultData resultData = new ResultData();
    private final ExamData examData = new ExamData();
    private final CourseOfferingData offeringData = new CourseOfferingData();
    private final CourseData courseData = new CourseData();
    private final MedicalData medicalData = new MedicalData();
    private final NoticeData noticeData = new NoticeData();

    public Overview(User user) {
        super("Overview",
                "Welcome back, "
                        + ((user != null && user.getName() != null)
                        ? user.getName() : "Student")
                        + "  ·  "
                        + LocalDate.now().format(
                        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                null);

        this.user = user;

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Load actual database information
        DashboardData dashboard = loadDashboardData();

        // KPI cards
        JComponent kpis = kpiRow(dashboard);
        kpis.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(kpis);

        body.add(Box.createVerticalStrut(30));

        // Eligibility section
        JPanel eligibility = createEligibilitySection(dashboard.eligibilities);
        eligibility.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(eligibility);

        body.add(Box.createVerticalStrut(30));

        // Notices Board
        JPanel noticesBoard = createNoticesBoard(dashboard.notices);
        noticesBoard.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(noticesBoard);

        setScrollBody(body);
    }

    // =====================================================
    // Dashboard data container
    // =====================================================

    private static class DashboardData {
        List<Eligibility> eligibilities = new ArrayList<>();
        List<Notice> notices = new ArrayList<>();

        double averageAttendance = 0;
        int attendanceCount = 0;
        int attendanceEligible = 0;

        double averageCA = 0;
        int caCount = 0;

        int eligibleCount = 0;
        int pendingCount = 0;

        int medicalCount = 0;
        int approvedMedicalCount = 0;
        int pendingMedicalCount = 0;

        BigDecimal sgpa = null;
        BigDecimal cgpa = null;
    }

    // =====================================================
    // Load dashboard data
    // =====================================================

    private DashboardData loadDashboardData() {

        DashboardData data = new DashboardData();

        if (user == null) {
            return data;
        }

        int studentId = user.getUser_id();

        // 1. Eligibility records
        try {
            List<Eligibility> records =
                    eligibilityData.findByStudent(studentId);

            if (records != null) {
                data.eligibilities.addAll(records);
            }

            double attendanceTotal = 0;
            double caTotal = 0;

            for (Eligibility e : data.eligibilities) {

                if (e.getAttendancePercentage() != null) {
                    double attendance =
                            e.getAttendancePercentage().doubleValue();

                    attendanceTotal += attendance;
                    data.attendanceCount++;

                    if (attendance >= 80) {
                        data.attendanceEligible++;
                    }
                }

                if (e.getCaAverage() != null) {
                    caTotal += e.getCaAverage().doubleValue();
                    data.caCount++;
                }

                String status = e.getEligibilityStatus();

                if ("ELIGIBLE".equalsIgnoreCase(status)) {
                    data.eligibleCount++;
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    data.pendingCount++;
                }
            }

            if (data.attendanceCount > 0) {
                data.averageAttendance =
                        attendanceTotal / data.attendanceCount;
            }

            if (data.caCount > 0) {
                data.averageCA = caTotal / data.caCount;
            }

        } catch (Exception ex) {
            showLoadError("Eligibility", ex);
        }

        // 2. Medical records
        try {
            List<Medical> medicals =
                    medicalData.findByStudent(studentId);

            if (medicals != null) {
                data.medicalCount = medicals.size();

                for (Medical medical : medicals) {
                    String status = medical.getStatus();

                    if ("APPROVED".equalsIgnoreCase(status)) {
                        data.approvedMedicalCount++;
                    } else if ("PENDING".equalsIgnoreCase(status)) {
                        data.pendingMedicalCount++;
                    }
                }
            }

        } catch (Exception ex) {
            showLoadError("Medical records", ex);
        }

        // 3. Latest SGPA and CGPA
        try {
            List<Result> results =
                    resultData.findByStudent(studentId);

            if (results != null && !results.isEmpty()) {

                results.sort((a, b) -> {
                    if (a.getPublishedDate() == null) return 1;
                    if (b.getPublishedDate() == null) return -1;

                    return b.getPublishedDate()
                            .compareTo(a.getPublishedDate());
                });

                for (Result result : results) {

                    if (data.sgpa == null && result.getSgpa() != null) {
                        data.sgpa = result.getSgpa();
                    }

                    if (data.cgpa == null && result.getCgpa() != null) {
                        data.cgpa = result.getCgpa();
                    }

                    if (data.sgpa != null && data.cgpa != null) {
                        break;
                    }
                }
            }

        } catch (Exception ex) {
            showLoadError("Results", ex);
        }

        // 4. Notices for undergraduate students and everyone
        try {
            Map<Integer, Notice> noticeMap = new HashMap<>();

            List<Notice> undergraduateNotices =
                    noticeData.findByAudience("UNDERGRADUATE");

            List<Notice> allNotices =
                    noticeData.findByAudience("ALL");

            if (undergraduateNotices != null) {
                for (Notice notice : undergraduateNotices) {
                    noticeMap.put(notice.getNotice_id(), notice);
                }
            }

            if (allNotices != null) {
                for (Notice notice : allNotices) {
                    noticeMap.put(notice.getNotice_id(), notice);
                }
            }

            LocalDate today = LocalDate.now();

            for (Notice notice : noticeMap.values()) {

                if (!"ACTIVE".equalsIgnoreCase(notice.getStatus())) {
                    continue;
                }

                if (notice.getExpiry_date() != null
                        && notice.getExpiry_date().toLocalDate()
                        .isBefore(today)) {
                    continue;
                }

                data.notices.add(notice);
            }

            data.notices.sort((a, b) -> {
                if (a.getCreated_date() == null) return 1;
                if (b.getCreated_date() == null) return -1;

                return b.getCreated_date()
                        .compareTo(a.getCreated_date());
            });

            if (data.notices.size() > 3) {
                data.notices = new ArrayList<>(
                        data.notices.subList(0, 3));
            }

        } catch (Exception ex) {
            showLoadError("Notices", ex);
        }

        return data;
    }

    private void showLoadError(String section, Exception ex) {
        System.err.println("Unable to load " + section + ": "
                + ex.getMessage());
        ex.printStackTrace();
    }

    // =====================================================
    // KPI cards
    // =====================================================

    private JComponent kpiRow(DashboardData data) {

        String attendanceValue = data.attendanceCount > 0
                ? String.format(Locale.US, "%.2f%%", data.averageAttendance)
                : "N/A";

        String attendanceSub = data.attendanceCount > 0
                ? data.attendanceEligible + "/"
                + data.attendanceCount + " modules ≥ 80%"
                : "No attendance data";

        String moduleValue =
                data.attendanceEligible + "/" + data.eligibilities.size();

        String medicalSub =
                data.approvedMedicalCount + " approved · "
                        + data.pendingMedicalCount + " pending";

        String sgpaValue = data.sgpa != null
                ? data.sgpa.toPlainString() : "N/A";

        String cgpaValue = data.cgpa != null
                ? data.cgpa.toPlainString() : "N/A";

        String[][] kpis = {
                {"Attendance", attendanceValue, attendanceSub},
                {"Modules ≥ 80%", moduleValue, "Attendance threshold"},
                {"Medicals", String.valueOf(data.medicalCount), medicalSub},
                {"SGPA", sgpaValue, "Latest published result"},
                {"CGPA", cgpaValue, "Latest cumulative result"}
        };

        JPanel row = new JPanel(
                new GridLayout(1, kpis.length, 16, 0));

        row.setOpaque(false);
        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 124));

        for (int i = 0; i < kpis.length; i++) {
            row.add(kpiCard(
                    kpis[i][0],
                    kpis[i][1],
                    kpis[i][2],
                    i == 0
            ));
        }

        return row;
    }

    private JComponent kpiCard(
            String label,
            String value,
            String sub,
            boolean dark) {

        Card c = new Card(dark);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(18, 20, 18, 20));

        Color muted = dark ? ON_BRAND_2 : INK_2;

        JLabel l = new JLabel(label);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(muted);

        JLabel v = new JLabel(value);
        v.setFont(font(Font.BOLD, 30));
        v.setForeground(dark ? Color.WHITE : INK);

        JLabel s = new JLabel(sub);
        s.setFont(font(Font.PLAIN, 11));
        s.setForeground(muted);

        c.add(l);
        c.add(Box.createVerticalGlue());
        c.add(v);
        c.add(s);

        return c;
    }

    // =====================================================
    // Eligibility section
    // =====================================================

    private JPanel createEligibilitySection(
            List<Eligibility> eligibilities) {

        JPanel section = new JPanel(new BorderLayout(0, 15));
        section.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Final exam eligibility");
        title.setFont(font(Font.BOLD, 18));
        title.setForeground(INK);

        JLabel subtitle = new JLabel(
                "Your module eligibility information from the database");
        subtitle.setFont(font(Font.PLAIN, 13));
        subtitle.setForeground(INK_2);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel legend = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 15, 0));
        legend.setOpaque(false);

        legend.add(legendItem("Eligible", OK));
        legend.add(legendItem("Pending", WARN));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(legend, BorderLayout.EAST);

        Card tableCard = new Card(false);
        tableCard.setLayout(
                new BoxLayout(tableCard, BoxLayout.Y_AXIS));
        tableCard.setBorder(new EmptyBorder(10, 20, 20, 20));

        String[] cols = {
                "MODULE", "ATTENDANCE %", "CA / 100",
                "ATTENDANCE", "CA", "VERDICT"
        };

        int[] widths = {330, 130, 90, 110, 100, 110};

        JPanel headerRow = new JPanel();
        headerRow.setLayout(
                new BoxLayout(headerRow, BoxLayout.X_AXIS));
        headerRow.setOpaque(false);
        headerRow.setBorder(
                new EmptyBorder(10, 0, 15, 0));

        for (int i = 0; i < cols.length; i++) {
            JLabel label = new JLabel(cols[i]);
            label.setFont(font(Font.BOLD, 11));
            label.setForeground(INK_2);
            headerRow.add(wrapCell(label, widths[i]));
        }

        tableCard.add(headerRow);
        tableCard.add(new JSeparator(SwingConstants.HORIZONTAL));

        if (eligibilities == null || eligibilities.isEmpty()) {

            JLabel empty = new JLabel(
                    "No eligibility records available yet.",
                    SwingConstants.CENTER);

            empty.setFont(font(Font.PLAIN, 13));
            empty.setForeground(INK_2);
            empty.setBorder(new EmptyBorder(25, 0, 25, 0));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);

            tableCard.add(empty);

        } else {

            for (Eligibility eligibility : eligibilities) {
                addEligibilityRow(
                        tableCard,
                        eligibility,
                        widths
                );
            }
        }

        section.add(header, BorderLayout.NORTH);
        section.add(tableCard, BorderLayout.CENTER);

        return section;
    }

    private void addEligibilityRow(
            JPanel container,
            Eligibility eligibility,
            int[] widths) {

        String code = "Unknown";
        String name = "Unknown module";

        // Resolve offering -> course using database
        try {
            CourseOffering offering =
                    offeringData.findById(eligibility.getOfferingId());

            if (offering != null) {
                Course course =
                        courseData.findById(offering.getCourse_id());

                if (course != null) {
                    code = course.getCourse_code();
                    name = course.getCourse_name();
                }
            }

        } catch (Exception ex) {
            showLoadError("Course information", ex);
        }

        BigDecimal attendanceValue =
                eligibility.getAttendancePercentage();

        BigDecimal caValue = eligibility.getCaAverage();

        double attendance = attendanceValue != null
                ? attendanceValue.doubleValue() : 0;

        boolean hasAttendance = attendanceValue != null;
        boolean hasCA = caValue != null;

        String status = eligibility.getEligibilityStatus();

        boolean eligible =
                "ELIGIBLE".equalsIgnoreCase(status);

        boolean pending =
                "PENDING".equalsIgnoreCase(status);

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 0, 12, 0));

        // 1. Module
        JPanel modulePanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0));
        modulePanel.setOpaque(false);

        JLabel codeLabel = new JLabel(code + " ");
        codeLabel.setFont(font(Font.BOLD, 13));
        codeLabel.setForeground(INK);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(font(Font.PLAIN, 12));
        nameLabel.setForeground(INK_2);

        modulePanel.add(codeLabel);
        modulePanel.add(nameLabel);

        row.add(wrapCell(modulePanel, widths[0]));

        // 2. Attendance percentage
        JPanel attendancePanel = new JPanel();
        attendancePanel.setLayout(
                new BoxLayout(attendancePanel, BoxLayout.Y_AXIS));
        attendancePanel.setOpaque(false);

        JLabel percentageLabel = new JLabel(
                hasAttendance
                        ? String.format(Locale.US, "%.2f%%", attendance)
                        : "N/A"
        );

        percentageLabel.setFont(font(Font.BOLD, 12));
        percentageLabel.setForeground(INK);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(hasAttendance
                ? (int) Math.round(attendance) : 0);
        bar.setPreferredSize(new Dimension(95, 6));
        bar.setMaximumSize(new Dimension(95, 6));
        bar.setForeground(hasAttendance && attendance >= 80
                ? OK : WARN);
        bar.setBackground(BRAND_SOFT);
        bar.setBorderPainted(false);

        attendancePanel.add(percentageLabel);
        attendancePanel.add(Box.createVerticalStrut(3));
        attendancePanel.add(bar);

        row.add(wrapCell(attendancePanel, widths[1]));

        // 3. CA average
        JLabel caLabel = new JLabel(
                hasCA ? caValue.toPlainString() : "N/A");

        caLabel.setFont(font(Font.PLAIN, 13));
        caLabel.setForeground(INK);

        row.add(wrapCell(caLabel, widths[2]));

        // 4. Attendance status
        JPanel attendanceChip = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0));
        attendanceChip.setOpaque(false);

        if (!hasAttendance) {
            attendanceChip.add(new Chip("N/A", WARN));
        } else if (attendance >= 80) {
            attendanceChip.add(new Chip("● Eligible", OK));
        } else {
            attendanceChip.add(new Chip("● Below 80%", WARN));
        }

        row.add(wrapCell(attendanceChip, widths[3]));

        // 5. CA status
        JPanel caChip = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0));
        caChip.setOpaque(false);

        if (!hasCA) {
            caChip.add(new Chip("● Pending", WARN));
        } else if (caValue.compareTo(
                new BigDecimal("40")) >= 0) {
            caChip.add(new Chip("● CA ≥ 40", OK));
        } else {
            caChip.add(new Chip("● Below 40", WARN));
        }

        row.add(wrapCell(caChip, widths[4]));

        // 6. Final eligibility status from database
        JPanel verdictPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0));
        verdictPanel.setOpaque(false);

        if (eligible) {
            verdictPanel.add(new Chip("● Eligible", OK));
        } else if (pending) {
            verdictPanel.add(new Chip("● Pending", WARN));
        } else {
            verdictPanel.add(new Chip(
                    status != null ? status : "Unknown", WARN));
        }

        row.add(wrapCell(verdictPanel, widths[5]));

        container.add(row);
        container.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    // =====================================================
    // Table helper methods
    // =====================================================

    private JComponent wrapCell(
            JComponent content,
            int width) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(content, BorderLayout.WEST);

        Dimension dimension = new Dimension(width, 40);

        panel.setPreferredSize(dimension);
        panel.setMinimumSize(dimension);
        panel.setMaximumSize(
                new Dimension(width, Integer.MAX_VALUE));

        return panel;
    }

    private JComponent legendItem(
            String text,
            Color color) {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0));
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

    // =====================================================
    // Notice board
    // =====================================================

    private JPanel createNoticesBoard(List<Notice> notices) {

        JPanel section = new JPanel(new BorderLayout(0, 16));
        section.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Notice board");
        title.setFont(font(Font.BOLD, 18));
        title.setForeground(INK);

        JLabel subtitle = new JLabel(
                "Latest faculty announcements");
        subtitle.setFont(font(Font.PLAIN, 13));
        subtitle.setForeground(INK_2);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        RoundedButton viewButton =
                new RoundedButton("View all", false);

        viewButton.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Select 'Notices' from the sidebar to view all announcements.",
                        "Notice board",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(titlePanel, BorderLayout.WEST);
        heading.add(viewButton, BorderLayout.EAST);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(
                new BoxLayout(list, BoxLayout.Y_AXIS));

        if (notices == null || notices.isEmpty()) {

            JLabel empty = new JLabel(
                    "No active announcements available.");

            empty.setFont(font(Font.PLAIN, 13));
            empty.setForeground(INK_2);
            empty.setBorder(new EmptyBorder(20, 5, 20, 5));

            list.add(empty);

        } else {

            for (Notice notice : notices) {

                java.sql.Timestamp timestamp =
                        notice.getCreated_date();

                String day = "--";
                String month = "--";
                String footer = "Published";

                if (timestamp != null) {
                    LocalDate date =
                            timestamp.toLocalDateTime().toLocalDate();

                    day = String.format("%02d", date.getDayOfMonth());

                    month = date.format(
                                    DateTimeFormatter.ofPattern("MMM"))
                            .toUpperCase(Locale.ROOT);

                    footer = "Published · "
                            + date.format(
                            DateTimeFormatter.ofPattern("d MMM yyyy"));
                }

                list.add(noticeCard(
                        day,
                        month,
                        "ANNOUNCEMENT",
                        notice.getTitle(),
                        notice.getDescription(),
                        footer
                ));

                list.add(Box.createVerticalStrut(12));
            }
        }

        section.add(heading, BorderLayout.NORTH);
        section.add(list, BorderLayout.CENTER);

        return section;
    }

    private JComponent noticeCard(
            String day,
            String month,
            String tag,
            String title,
            String desc,
            String footer) {

        Card card = new Card(false);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel dateBlock = new JPanel(new GridLayout(2, 1));
        dateBlock.setOpaque(false);

        JLabel dayLabel = new JLabel(
                day, SwingConstants.CENTER);
        dayLabel.setFont(font(Font.BOLD, 22));
        dayLabel.setForeground(INK);

        JLabel monthLabel = new JLabel(
                month, SwingConstants.CENTER);
        monthLabel.setFont(font(Font.PLAIN, 11));
        monthLabel.setForeground(Color.GRAY);

        dateBlock.add(dayLabel);
        dateBlock.add(monthLabel);

        JPanel dateContainer = new JPanel(new BorderLayout());
        dateContainer.setOpaque(false);

        dateContainer.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 0, 1, LINE),
                        new EmptyBorder(0, 5, 0, 20)
                )
        );

        dateContainer.add(dateBlock, BorderLayout.NORTH);

        JPanel contentBlock = new JPanel();
        contentBlock.setLayout(
                new BoxLayout(contentBlock, BoxLayout.Y_AXIS));
        contentBlock.setOpaque(false);

        JPanel topRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        topRow.add(new Chip(tag, OK));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.BOLD, 15));
        titleLabel.setForeground(INK);

        topRow.add(titleLabel);

        JTextArea descriptionArea = new JTextArea(
                desc != null ? desc : "");

        descriptionArea.setFont(font(Font.PLAIN, 13));
        descriptionArea.setForeground(INK_2);
        descriptionArea.setOpaque(false);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(
                new EmptyBorder(10, 0, 10, 0));
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel footerLabel = new JLabel(footer);
        footerLabel.setFont(font(Font.PLAIN, 11));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentBlock.add(topRow);
        contentBlock.add(descriptionArea);
        contentBlock.add(footerLabel);

        card.add(dateContainer, BorderLayout.WEST);
        card.add(contentBlock, BorderLayout.CENTER);

        return card;
    }
}
