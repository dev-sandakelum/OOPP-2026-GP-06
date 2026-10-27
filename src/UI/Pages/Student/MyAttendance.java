package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static UI.Kit.Palette.*;

public class MyAttendance extends Page {

    public MyAttendance(User user) {
        super("My Attendance", "View Attendance", null);

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
    }

    private JComponent kpiRow() {
        String[][] kpis = {
                {"Overall attendance", "91.7%", "combined average"},
                {"Modules ≥ 80%", "6/6", "eligibility threshold"},
                {"Medical sessions", "7", "2 approved record(s)"}
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
        Card card = new Card(dark);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        Color muted = dark ? new Color(190, 215, 200) : INK_2;

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(font(Font.BOLD, 13));
        titleLabel.setForeground(muted);

        JLabel valueLabel = new JLabel(value);
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

        JLabel subtitle = new JLabel("15 theory & practical sessions per module");
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

        // Table Header
        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.X_AXIS));
        headerRow.setOpaque(false);
        headerRow.setBorder(new EmptyBorder(10, 0, 15, 0));

        String[] cols = {"MODULE", "THEORY (15)", "PRACTICAL (15)", "COMBINED", "STATUS"};
        int[] widths = {240, 200, 200, 100, 100}; // Fixed widths for alignment

        for (int i = 0; i < cols.length; i++) {
            JLabel l = new JLabel(cols[i]);
            l.setFont(font(Font.BOLD, 11));
            l.setForeground(INK_2);
            headerRow.add(wrapCell(l, widths[i]));
        }

        tableCard.add(headerRow);
        tableCard.add(new JSeparator(SwingConstants.HORIZONTAL));

        // Rows Data
        addTableRow(tableCard, "ICT2131", "Programming with Java",
                "PPPPPPPPPPPPMMP", "13/15 · 86.7%",
                "PPPPPPPPPPPPPPA", "14/15 · 93.3%", 90, "Eligible", widths);

        addTableRow(tableCard, "ICT2132", "Object Oriented Programming Practicum",
                "PPPPPPPPPPPPMPP", "14/15 · 93.3%",
                "PPPPPPPPPPPAPPP", "14/15 · 93.3%", 93, "Eligible", widths);

        addTableRow(tableCard, "ICT2133", "Database Management Systems",
                "PPPPPPPPPPPPMPP", "14/15 · 93.3%",
                "PPPPPPPPPPPAPPP", "14/15 · 93.3%", 93, "Eligible", widths);

        addTableRow(tableCard, "ICT2134", "Data Structures & Algorithms",
                "PPPPPPPPPPPPPPM", "14/15 · 93.3%",
                "-", "—", 93, "Eligible", widths);

        addTableRow(tableCard, "ICT2135", "Software Engineering",
                "PPPPPPMPPPPPPPP", "14/15 · 93.3%",
                "PPPPAPPPPPPPPMM", "13/15 · 86.7%", 90, "Eligible", widths);

        section.add(header, BorderLayout.NORTH);
        section.add(tableCard, BorderLayout.CENTER);

        return section;
    }

    private void addTableRow(JPanel container, String code, String name,
                             String theoryDots, String theoryText,
                             String pracDots, String pracText,
                             int combined, String status, int[] widths) {

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(15, 0, 15, 0));

        // 1. Module
        JPanel modPanel = new JPanel();
        modPanel.setLayout(new BoxLayout(modPanel, BoxLayout.Y_AXIS));
        modPanel.setOpaque(false);
        JLabel codeLbl = new JLabel(code);
        codeLbl.setFont(font(Font.BOLD, 13));
        codeLbl.setForeground(INK);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(font(Font.PLAIN, 12));
        nameLbl.setForeground(INK_2);
        modPanel.add(codeLbl);
        modPanel.add(nameLbl);
        row.add(wrapCell(modPanel, widths[0]));

        // 2. Theory
        row.add(wrapCell(createDotsPanel(theoryDots, theoryText), widths[1]));

        // 3. Practical
        row.add(wrapCell(createDotsPanel(pracDots, pracText), widths[2]));

        // 4. Combined
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.Y_AXIS));
        combinedPanel.setOpaque(false);
        JLabel pctLbl = new JLabel(combined + "%");
        pctLbl.setFont(font(Font.BOLD, 13));
        pctLbl.setForeground(new Color(46, 125, 50));
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(combined);
        bar.setPreferredSize(new Dimension(80, 6));
        bar.setMaximumSize(new Dimension(80, 6));
        bar.setForeground(new Color(20, 80, 50));
        bar.setBackground(new Color(230, 240, 230));
        bar.setBorderPainted(false);
        combinedPanel.add(Box.createVerticalStrut(5));
        combinedPanel.add(pctLbl);
        combinedPanel.add(Box.createVerticalStrut(5));
        combinedPanel.add(bar);
        row.add(wrapCell(combinedPanel, widths[3]));

        // 5. Status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        statusPanel.setOpaque(false);
        statusPanel.add(new Chip("● " + status, new Color(46, 125, 50)));
        row.add(wrapCell(statusPanel, widths[4]));

        container.add(row);
        container.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    // This helper method locks the width of each column perfectly
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

    private JPanel createDotsPanel(String pattern, String text) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);

        if (!pattern.equals("-")) {
            p.add(new DotDrawer(pattern), BorderLayout.NORTH);
        } else {
            p.add(new JLabel(" "), BorderLayout.NORTH);
        }

        JLabel tLbl = new JLabel(text);
        tLbl.setFont(font(Font.PLAIN, 12));
        tLbl.setForeground(INK_2);
        p.add(tLbl, BorderLayout.CENTER);

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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = 0;
            for (char c : pattern.toCharArray()) {
                if (c == 'P') g2.setColor(new Color(46, 125, 50));
                else if (c == 'M') g2.setColor(new Color(245, 176, 65));
                else if (c == 'A') g2.setColor(new Color(245, 200, 200));
                else continue;

                g2.fillOval(x, 0, 8, 8);
                x += 12;
            }
            g2.dispose();
        }
    }
}