package UI.Pages.Officer;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.Sidebar;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

public class Overview extends Page {

    // ---------- Medicals Table Columns ----------
    private static final String[] MEDICAL_COLS = {
            "SUBMITTED", "UNDERGRADUATE", "LEAVE PERIOD", "REASON", "STATUS"
    };

    // ---------- Timetable Columns ----------
    private static final String[] TIMETABLE_COLS = {
            "TIME", "MODULE", "TYPE", "VENUE", "LECTURER"
    };

    private static final String[][] TIMETABLE_DATA = {
            {"08.30 – 10.30", "ICT2131", "T", "LH-1", "Dr. Perera"},
            {"10.45 – 12.45", "ICT2132", "P", "Lab-201", "Dr. Fernando"},
            {"13.30 – 15.30", "ICT2134", "T", "LH-2", "Dr. Bandara"}
    };

    public Overview(User user) {
        super("Overview",
                "Welcome back, " + ((user != null && user.getName() != null) ? user.getName() : "Technical Officer")
                        + "  ·  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                null);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // 1. KPI Cards Row
        body.add(kpiRow());
        body.add(Box.createVerticalStrut(22));

        // 2. Medicals Awaiting Approval Section (Redirects to tab index 2: Medicals)
        body.add(sectionHeader(
                "Medicals awaiting approval",
                "Approve or reject from the Medicals section",
                "Open medicals",
                2
        ));
        body.add(Box.createVerticalStrut(10));
        body.add(createMedicalsTableCard());
        body.add(Box.createVerticalStrut(22));

        // 3. Today's Timetable Section (Redirects to tab index 4: Timetables)
        body.add(sectionHeader(
                "Today — Mon",
                "Department timetable",
                "Full week",
                4
        ));
        body.add(Box.createVerticalStrut(10));
        body.add(createTimetableCard());
        body.add(Box.createVerticalStrut(22));

        // 4. Notice Board Section (Redirects to tab index 3: Notices)
        body.add(sectionHeader(
                "Notice board",
                "Latest faculty announcements",
                "View all",
                3
        ));
        body.add(Box.createVerticalStrut(10));
        body.add(createNoticesCard());
        body.add(Box.createVerticalStrut(16));

        setScrollBody(body);
    }

    // ---------- Navigation Helper ----------
    private void navigateToTab(int tabIndex) {
        // Traverses up to the window to find the Sidebar component and triggers tab selection
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            Sidebar sidebar = findSidebar(window);
            if (sidebar != null) {
                sidebar.setSelectedIndex(tabIndex);
            }
        }
    }

    private Sidebar findSidebar(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof Sidebar) {
                return (Sidebar) comp;
            }
            if (comp instanceof Container) {
                Sidebar found = findSidebar((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    // ---------- Section Header Helper ----------
    private JComponent sectionHeader(String title, String subtitle, String actionText, int targetIndex) {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(font(Font.BOLD, 17));
        titleLbl.setForeground(INK);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(font(Font.PLAIN, 12));
        subLbl.setForeground(INK_2);

        left.add(titleLbl);
        left.add(Box.createVerticalStrut(2));
        left.add(subLbl);

        RoundedButton actionBtn = new RoundedButton(actionText, false);
        actionBtn.addActionListener(e -> navigateToTab(targetIndex));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(left, BorderLayout.WEST);
        header.add(actionBtn, BorderLayout.EAST);
        return header;
    }

    // ---------- KPI Cards Row ----------
    private JComponent kpiRow() {
        String[][] kpis = {
                {"Modules", "6", "6 modules × 15 sessions"},
                {"Registers", "90", "theory & practical"},
                {"Undergraduates", "20", "2 repeat · 2 batch-missed"},
                {"Pending medicals", "3", "awaiting your decision"}
        };

        JPanel row = new JPanel(new GridLayout(1, kpis.length, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        Color muted = dark ? ON_BRAND_2 : INK_2;

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

    // ---------- Medicals Card (From Medicals Data) ----------
    private JComponent createMedicalsTableCard() {
        ReadOnlyTableModel model = new ReadOnlyTableModel(MEDICAL_COLS);

        // Uses the first pending medical records from the Medicals page
        model.addRow(new String[]{"18 Sep", "K. D. Fernando (FOT/2023/044)", "10 Sep – 17 Sep", "Severe flu & fever", "Pending"});
        model.addRow(new String[]{"19 Sep", "M. R. Perera (FOT/2023/088)", "12 Sep – 14 Sep", "Dental surgery", "Pending"});
        model.addRow(new String[]{"16 Sep", "W. A. N. Silva (FOT/2023/061)", "24 Aug – 15 Sep", "Migraine — clinic observation", "Pending"});

        JTable table = table(model, Map.of(
                "Pending", WARN,
                "Approved", OK,
                "Rejected", BAD
        ), 4);

        table.setRowHeight(44);
        TableColumnModel colModel = table.getColumnModel();

        colModel.getColumn(0).setPreferredWidth(100);
        colModel.getColumn(1).setPreferredWidth(280);
        colModel.getColumn(2).setPreferredWidth(160);
        colModel.getColumn(3).setPreferredWidth(260);
        colModel.getColumn(4).setPreferredWidth(110);

        JComponent card = tableCard(table);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    // ---------- Timetable Card ----------
    private JComponent createTimetableCard() {
        ReadOnlyTableModel model = new ReadOnlyTableModel(TIMETABLE_COLS);
        for (String[] r : TIMETABLE_DATA) {
            model.addRow(r);
        }

        JTable table = table(model, Map.of(
                "T", INK_2,
                "P", BRAND
        ), 2);

        table.setRowHeight(44);
        TableColumnModel colModel = table.getColumnModel();

        colModel.getColumn(0).setPreferredWidth(130);
        colModel.getColumn(1).setPreferredWidth(130);
        colModel.getColumn(2).setPreferredWidth(70);
        colModel.getColumn(3).setPreferredWidth(130);
        colModel.getColumn(4).setPreferredWidth(180);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        colModel.getColumn(2).setCellRenderer(centerRender);

        JComponent card = tableCard(table);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    // ---------- Notice Board Card ----------
    private JComponent createNoticesCard() {
        Card card = new Card(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(noticeItem(
                "28", "SEP", "EXAMINATION",
                "Final examination timetable — Level II Semester I",
                "The final examination timetable has been published. Examinations begin on 26 October 2026. " +
                        "Check your eligibility (attendance ≥ 80% and CA ≥ 40%) before the exam. Hall tickets will be issued from 20 October.",
                "posted by Administration · 28 Sep 2026"
        ));

        JSeparator sep = new JSeparator();
        sep.setForeground(LINE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(Box.createVerticalStrut(12));
        card.add(sep);
        card.add(Box.createVerticalStrut(12));

        card.add(noticeItem(
                "24", "SEP", "ATTENDANCE",
                "Attendance eligibility list published for all modules",
                "Session-by-session attendance for all 15 theory and practical sessions is now finalized. View the register to resolve any discrepancies.",
                "posted by Technical Officer · 24 Sep 2026"
        ));

        return card;
    }

    private JComponent noticeItem(String day, String month, String category, String title, String description, String meta) {
        JPanel item = new JPanel(new BorderLayout(16, 0));
        item.setOpaque(false);

        // Date Box
        JPanel dateBox = new JPanel();
        dateBox.setOpaque(false);
        dateBox.setLayout(new BoxLayout(dateBox, BoxLayout.Y_AXIS));
        dateBox.setPreferredSize(new Dimension(46, 50));

        JLabel dayLbl = new JLabel(day, SwingConstants.CENTER);
        dayLbl.setFont(font(Font.BOLD, 20));
        dayLbl.setForeground(INK);
        dayLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel monthLbl = new JLabel(month, SwingConstants.CENTER);
        monthLbl.setFont(font(Font.BOLD, 10));
        monthLbl.setForeground(INK_2);
        monthLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        dateBox.add(dayLbl);
        dateBox.add(monthLbl);

        // Content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);

        JLabel tag = new JLabel(category);
        tag.setFont(font(Font.BOLD, 9));
        tag.setForeground(BRAND);
        tag.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BRAND, 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(font(Font.BOLD, 14));
        titleLbl.setForeground(INK);

        topRow.add(tag);
        topRow.add(titleLbl);

        JLabel descLbl = new JLabel("<html>" + description + "</html>");
        descLbl.setFont(font(Font.PLAIN, 12));
        descLbl.setForeground(INK_2);

        JLabel metaLbl = new JLabel(meta);
        metaLbl.setFont(font(Font.PLAIN, 11));
        metaLbl.setForeground(INK_2);

        content.add(topRow);
        content.add(Box.createVerticalStrut(6));
        content.add(descLbl);
        content.add(Box.createVerticalStrut(6));
        content.add(metaLbl);

        item.add(dateBox, BorderLayout.WEST);
        item.add(content, BorderLayout.CENTER);
        return item;
    }
}