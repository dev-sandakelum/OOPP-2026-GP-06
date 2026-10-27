package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import UI.Pages.Components.RoundedButton;
import data.UserData;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static UI.Kit.Palette.*;

public class Overview extends Page {
    private final UserData userData = new UserData();

    public Overview(User user) {
        super("Overview",
                "Welcome back, "
                        + ((user != null && user.getName() != null)
                        ? user.getName() : "Administrator")
                        + "  ·  "
                        + LocalDate.now().format(
                        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                null);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // KPI cards
        JComponent kpis = kpiRow();
        kpis.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(kpis);

        body.add(Box.createVerticalStrut(30));

        // Eligibility section
        JPanel eligibility = createEligibilitySection();
        eligibility.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(eligibility);

        body.add(Box.createVerticalStrut(30));

        // Notices Board
        JPanel noticesBoard = createNoticesBoard();
        noticesBoard.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(noticesBoard);

        setScrollBody(body);
    }

    // ---------- KPI cards ----------
    private JComponent kpiRow() {
        String[][] kpis = {
                {"Attendance", "91.7%", "combined average"},
                {"Modules ≥ 80%", "6/6", "eligibility threshold"},
                {"Medicals", "2", "2 approved · 0 pending"},
                {"SGPA", "3.80", "18 credits this semester"},
                {"CGPA", "3.48", "incl. Level 1 · 22 credits"}
        };
        JPanel row = new JPanel(new GridLayout(1, kpis.length, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));
        for (int i = 0; i < kpis.length; i++) {
            row.add(kpiCard(kpis[i][0], kpis[i][1], kpis[i][2], i == 0));
        }
        return row;
    }

    private JComponent kpiCard(String label, String value, String sub, boolean dark) {
        Card c = new Card(dark);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(18, 20, 18, 20));
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

    // ---------- Final Exam Eligibility Table Section ----------
    private JPanel createEligibilitySection() {
        JPanel section = new JPanel(new BorderLayout(0, 15));
        section.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Final exam eligibility");
        title.setFont(font(Font.BOLD, 18));
        title.setForeground(INK);

        JLabel subtitle = new JLabel("Attendance ≥ 80% combined · CA ≥ 40% per module");
        subtitle.setFont(font(Font.PLAIN, 13));
        subtitle.setForeground(INK_2);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        legend.setOpaque(false);
        legend.add(legendItem("present", new Color(46, 125, 50)));
        legend.add(legendItem("medical leave", new Color(245, 176, 65)));
        legend.add(legendItem("absent", new Color(245, 200, 200)));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(legend, BorderLayout.EAST);

        Card tableCard = new Card(false);
        tableCard.setLayout(new BoxLayout(tableCard, BoxLayout.Y_AXIS));
        tableCard.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Table Header with Perfectly Balanced Column Widths
        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.X_AXIS));
        headerRow.setOpaque(false);
        headerRow.setBorder(new EmptyBorder(10, 0, 15, 0));

        String[] cols = {"MODULE", "COMBINED", "CA / 100", "ATTENDANCE", "CA", "VERDICT"};
        // Module column width increased to 360 so names don't cut off; others balanced nicely
        int[] widths = {330, 130, 80, 110, 100, 110};

        for (int i = 0; i < cols.length; i++) {
            JLabel l = new JLabel(cols[i]);
            l.setFont(font(Font.BOLD, 11));
            l.setForeground(INK_2);
            headerRow.add(wrapCell(l, widths[i]));
        }

        tableCard.add(headerRow);
        tableCard.add(new JSeparator(SwingConstants.HORIZONTAL));

        addEligibilityRow(tableCard, "ICT2131", "Programming with Java", 90, "76", widths);
        addEligibilityRow(tableCard, "ICT2132", "Object Oriented Programming Practicum", 93, "82", widths);
        addEligibilityRow(tableCard, "ICT2133", "Database Management Systems", 93, "75", widths);
        addEligibilityRow(tableCard, "ICT2134", "Data Structures & Algorithms", 93, "75", widths);
        addEligibilityRow(tableCard, "ICT2135", "Software Engineering", 90, "73", widths);
        addEligibilityRow(tableCard, "ICT2136", "Computer Networks", 90, "73", widths);

        section.add(header, BorderLayout.NORTH);
        section.add(tableCard, BorderLayout.CENTER);

        return section;
    }

    private void addEligibilityRow(JPanel container, String code, String name, int combined, String caScore, int[] widths) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 0, 12, 0));

        // 1. Module
        JPanel modPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        modPanel.setOpaque(false);
        JLabel codeLbl = new JLabel(code + " ");
        codeLbl.setFont(font(Font.BOLD, 13));
        codeLbl.setForeground(INK);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(font(Font.PLAIN, 12));
        nameLbl.setForeground(INK_2);
        modPanel.add(codeLbl);
        modPanel.add(nameLbl);
        row.add(wrapCell(modPanel, widths[0]));

        // 2. Combined
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.Y_AXIS));
        combinedPanel.setOpaque(false);
        JLabel pctLbl = new JLabel(combined + (combined == 93 ? ".3%" : "%"));
        pctLbl.setFont(font(Font.BOLD, 12));
        pctLbl.setForeground(INK);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(combined);
        bar.setPreferredSize(new Dimension(95, 6));
        bar.setMaximumSize(new Dimension(95, 6));
        bar.setForeground(new Color(20, 80, 50));
        bar.setBackground(new Color(230, 240, 230));
        bar.setBorderPainted(false);
        combinedPanel.add(pctLbl);
        combinedPanel.add(Box.createVerticalStrut(3));
        combinedPanel.add(bar);
        row.add(wrapCell(combinedPanel, widths[1]));

        // 3. CA / 100
        JLabel caLbl = new JLabel(caScore);
        caLbl.setFont(font(Font.PLAIN, 13));
        caLbl.setForeground(INK);
        row.add(wrapCell(caLbl, widths[2]));

        // 4. Attendance
        JPanel attPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        attPanel.setOpaque(false);
        attPanel.add(new Chip("● Eligible", new Color(46, 125, 50)));
        row.add(wrapCell(attPanel, widths[3]));

        // 5. CA
        JPanel caChipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        caChipPanel.setOpaque(false);
        caChipPanel.add(new Chip("● CA ≥ 40", new Color(46, 125, 50)));
        row.add(wrapCell(caChipPanel, widths[4]));

        // 6. Verdict
        JPanel verdictPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        verdictPanel.setOpaque(false);
        verdictPanel.add(new Chip("● Eligible", new Color(46, 125, 50)));
        row.add(wrapCell(verdictPanel, widths[5]));

        container.add(row);
        container.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    // Helper method to lock column widths
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

    private JComponent legendItem(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("■");
        dot.setForeground(color);
        dot.setFont(font(Font.PLAIN, 10));
        JLabel lbl = new JLabel(text);
        lbl.setFont(font(Font.PLAIN, 12));
        lbl.setForeground(INK_2);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ---------- Notice Board Section ----------
    private JPanel createNoticesBoard() {
        JPanel section = new JPanel(new BorderLayout(0, 16));
        section.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Notice board");
        title.setFont(font(Font.BOLD, 18));
        title.setForeground(INK);
        JLabel subtitle = new JLabel("Latest faculty announcements");
        subtitle.setFont(font(Font.PLAIN, 13));
        subtitle.setForeground(INK_2);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        RoundedButton viewButton = new RoundedButton("View all", false);

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(titlePanel, BorderLayout.WEST);
        heading.add(viewButton, BorderLayout.EAST);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        String[][] notices = {
                {"28", "SEP", "EXAMINATION", "Final examination timetable — Level II Semester I",
                        "The final examination timetable has been published. Examinations begin on 26 October 2026. Check your eligibility (attendance ≥ 80% and CA ≥ 40%) before the exam. Hall tickets will be issued from 20 October.",
                        "posted by Administration · 28 Sep 2026"},

                {"24", "SEP", "ATTENDANCE", "Attendance eligibility list published for all modules",
                        "Session-by-session attendance for all 15 theory and practical sessions is now finalised. View the batch summary or your individual register to verify eligibility.",
                        "posted by Administration · 24 Sep 2026"},

                {"20", "SEP", "MEDICALS", "Medical submission deadline — 30 September 2026",
                        "Undergraduates are reminded that medicals for the semester must reach the Technical Officer on duty before 30 September. Late or unsealed submissions will be rejected.",
                        "posted by Administration · 20 Sep 2026"}
        };

        for (String[] n : notices) {
            list.add(noticeCard(n[0], n[1], n[2], n[3], n[4], n[5]));
            list.add(Box.createVerticalStrut(12));
        }

        section.add(heading, BorderLayout.NORTH);
        section.add(list, BorderLayout.CENTER);

        return section;
    }

    private JComponent noticeCard(String day, String month, String tag, String title, String desc, String footer) {
        Card card = new Card(false);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel dateBlock = new JPanel(new GridLayout(2, 1));
        dateBlock.setOpaque(false);
        JLabel dayLbl = new JLabel(day, SwingConstants.CENTER);
        dayLbl.setFont(font(Font.BOLD, 22));
        dayLbl.setForeground(new Color(2, 48, 71));
        JLabel monthLbl = new JLabel(month, SwingConstants.CENTER);
        monthLbl.setFont(font(Font.PLAIN, 11));
        monthLbl.setForeground(Color.GRAY);
        dateBlock.add(dayLbl);
        dateBlock.add(monthLbl);

        JPanel dateContainer = new JPanel(new BorderLayout());
        dateContainer.setOpaque(false);
        dateContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(235, 235, 235)),
                new EmptyBorder(0, 5, 0, 20)
        ));
        dateContainer.add(dateBlock, BorderLayout.NORTH);

        JPanel contentBlock = new JPanel();
        contentBlock.setLayout(new BoxLayout(contentBlock, BoxLayout.Y_AXIS));
        contentBlock.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRow.add(new Chip(tag, new Color(46, 125, 50)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(font(Font.BOLD, 15));
        titleLbl.setForeground(INK);
        topRow.add(titleLbl);

        JTextArea descArea = new JTextArea(desc);
        descArea.setFont(font(Font.PLAIN, 13));
        descArea.setForeground(INK_2);
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(new EmptyBorder(10, 0, 10, 0));
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel footerLbl = new JLabel(footer);
        footerLbl.setFont(font(Font.PLAIN, 11));
        footerLbl.setForeground(Color.GRAY);
        footerLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentBlock.add(topRow);
        contentBlock.add(descArea);
        contentBlock.add(footerLbl);

        card.add(dateContainer, BorderLayout.WEST);
        card.add(contentBlock, BorderLayout.CENTER);

        return card;
    }
}