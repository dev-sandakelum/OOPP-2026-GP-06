package UI.Pages.Student;

import UI.Pages.Components.*;
import UI.Pages.Components.Chip;
import javax.swing.table.DefaultTableModel;
import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import data.UserData;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

        body.add(Box.createVerticalStrut(24));

        // Eligibility section
        JPanel eligibility = createEligibilitySection();
        eligibility.setAlignmentX(Component.LEFT_ALIGNMENT);
        eligibility.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 380)
        );
        body.add(eligibility);

        body.add(Box.createVerticalStrut(24));

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

    // ---------- Final Exam Eligibility Table Section ----------
    private JPanel createEligibilitySection() {
        JPanel section = new JPanel(new BorderLayout(0, 15));
        section.setOpaque(false);

        // Header: Title + Subtitle
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        JLabel title = new JLabel("Final exam eligibility");
        title.setFont(font(Font.BOLD, 20));
        title.setForeground(INK);

        JLabel subtitle = new JLabel("Attendance ≥ 80% combined · CA ≥ 40% per module");
        subtitle.setFont(font(Font.PLAIN, 13));
        subtitle.setForeground(INK_2);

        titles.add(title);
        titles.add(subtitle);

        header.add(titles, BorderLayout.WEST);

        // Sample Data
        String[] columns = {"MODULE", "COMBINED", "CA / 100", "ATTENDANCE", "CA", "VERDICT"};

        Color fgGreen = new Color(46, 125, 50);

        Object[][] data = {
                {"ICT2131 Programming with Java", 90, "76", new Chip("Eligible", fgGreen), new Chip("CA ≥ 40", fgGreen), new Chip("Eligible", fgGreen)},
                {"ICT2132 Object Oriented Programming Practicum", 93, "82", new Chip("Eligible", fgGreen), new Chip("CA ≥ 40", fgGreen), new Chip("Eligible", fgGreen)},
                {"ICT2133 Database Management Systems", 93, "75", new Chip("Eligible", fgGreen), new Chip("CA ≥ 40", fgGreen), new Chip("Eligible", fgGreen)},
                {"ICT2134 Data Structures & Algorithms", 93, "75", new Chip("Eligible", fgGreen), new Chip("CA ≥ 40", fgGreen), new Chip("Eligible", fgGreen)},
                {"ICT2135 Software Engineering", 90, "73", new Chip("Eligible", fgGreen), new Chip("CA ≥ 40", fgGreen), new Chip("Eligible", fgGreen)},
                {"ICT2136 Computer Networks", 90, "73", new Chip("Eligible", fgGreen), new Chip("CA ≥ 40", fgGreen), new Chip("Eligible", fgGreen)}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);

        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);

        TableColumnModel colModel = table.getColumnModel();


        ChipRenderer chipRenderer = new ChipRenderer(new java.util.HashMap<>());
        colModel.getColumn(3).setCellRenderer(chipRenderer);
        colModel.getColumn(4).setCellRenderer(chipRenderer);
        colModel.getColumn(5).setCellRenderer(chipRenderer);

       //Create combined progress bar
        colModel.getColumn(1).setCellRenderer((t, value, isSelected, hasFocus, row, column) -> {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(true);
            p.setBackground(isSelected ? t.getSelectionBackground() : t.getBackground());
            p.setBorder(new EmptyBorder(8, 5, 8, 20));

            if (value instanceof Number) {
                int val = ((Number) value).intValue();
                JLabel lbl = new JLabel(val + "%");
                lbl.setFont(font(Font.BOLD, 13));
                lbl.setForeground(INK);

                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue(val);
                bar.setForeground(new Color(27, 94, 32)); // Dark green
                bar.setBackground(new Color(224, 224, 224));
                bar.setBorderPainted(false);
                bar.setPreferredSize(new Dimension(80, 6));

                p.add(lbl, BorderLayout.NORTH);
                p.add(bar, BorderLayout.SOUTH);
            }
            return p;
        });

        colModel.getColumn(0).setPreferredWidth(250);


        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 320));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        section.add(header, BorderLayout.NORTH);
        section.add(scroll, BorderLayout.CENTER);

        return section;
    }


    private final java.util.List<String[]> notices = new ArrayList<>(List.of(
            new String[]{ "Examination\n",
                    "Mid-semester examinations",
                    "Mid-semester exams begin next Monday. Check the timetable for your venue"
                            + "\n"
                             +"posted by Administration · 24 Sep 2026"},

            new String[]{"Attendance\n",
                    "Attendance eligibility list published for all modules",
                    "Session-by-session attendance for all 15 theory and practical sessions is now finalised. View the batch summary or your individual register to verify eligibility"
                            +"\n"
                            + "\nposted by Administration · 24 Sep 2026"},

            new String[]{"Medicals\n", "Medical submission deadline — 30 September 2026", "Undergraduates are reminded that medicals for the semester must reach the Technical Officer on duty before 30 September. Late or unsealed submissions will be rejected.\n" +
                    "\n" +
                    "\nposted by Administration · 20 Sep 2026","2026.10.26"}
    ));
    // ---------- end placeholder data ----------

    private final JPanel list = new JPanel();


    private JPanel createNoticesBoard() {

        JPanel section = new JPanel(new BorderLayout(0, 16));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Heading
        JLabel title = new JLabel("Notices Board");
        title.setFont(font(Font.BOLD, 22));
        title.setForeground(INK);

        JLabel subtitle = new JLabel("Latest faculty announcements");
        subtitle.setFont(font(Font.PLAIN, 13));
        subtitle.setForeground(INK_2);

        RoundedButton viewButton = new RoundedButton("View all", false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(titlePanel, BorderLayout.WEST);
        heading.add(viewButton, BorderLayout.EAST);

        // Notices list
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        section.add(heading, BorderLayout.NORTH);
        section.add(list, BorderLayout.CENTER);

        refresh();

        viewButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "All faculty notices",
                    "Notices Board",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        return section;
    }

    private void refresh() {

        list.removeAll();

        for (String[] notice : notices) {
            list.add(noticeCard(notice));
            list.add(Box.createVerticalStrut(12));
        }

        list.revalidate();
        list.repaint();
    }

    private JComponent noticeCard(String[] notice) {

        JPanel card = new Card(false);
        card.setLayout(new BorderLayout(15, 0));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Date - currently stored inside the notice description
        JLabel date = new JLabel(
                "<html><center>24 Sep<br>2026</center></html>"
        );
        date.setFont(font(Font.BOLD, 13));
        date.setForeground(BRAND);
        date.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setOpaque(false);
        datePanel.setPreferredSize(new Dimension(65, 55));
        datePanel.add(date, BorderLayout.CENTER);

        // Notice title
        JLabel noticeTitle = new JLabel(notice[1]);
        noticeTitle.setFont(font(Font.BOLD, 15));
        noticeTitle.setForeground(INK);

        // Notice description
        JTextArea description = new JTextArea(notice[2]);
        description.setFont(font(Font.PLAIN, 13));
        description.setForeground(INK_2);
        description.setOpaque(false);
        description.setEditable(false);
        description.setFocusable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBorder(null);

        // Notice category
        Chip category = new Chip(notice[0], BRAND);

        JPanel heading = new JPanel(new BorderLayout(10, 0));
        heading.setOpaque(false);
        heading.add(category, BorderLayout.WEST);
        heading.add(noticeTitle, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 9));
        content.setOpaque(false);
        content.add(heading, BorderLayout.NORTH);
        content.add(description, BorderLayout.CENTER);

        card.add(datePanel, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);

        return card;
    }


}

