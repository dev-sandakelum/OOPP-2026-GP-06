package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static UI.Kit.Palette.*;

public class Notices extends Page {

    // ---------- Notice Data ----------
    private final List<String[]> notices = new ArrayList<>(List.of(
            new String[]{"EXAMINATION", "Final examination timetable — Level II Semester I", "The final examination timetable has been published. Examinations begin on 26 October 2026. Check your eligibility (attendance ≥ 80% and CA ≥ 40%) before the exam. Hall tickets will be issued from 20 October.", "2026-09-28", "Administration"},
            new String[]{"ATTENDANCE", "Attendance eligibility list published for all modules", "Session-by-session attendance for all 15 theory and practical sessions is now finalised. View the batch summary or your individual register to verify eligibility.", "2026-09-24", "Administration"},
            new String[]{"MEDICALS", "Medical submission deadline — 30 September 2026", "Undergraduates are reminded that medicals for the semester must reach the Technical Officer on duty before 30 September. Late or unsealed submissions will be rejected.", "2026-09-20", "Administration"},
            new String[]{"TIMETABLE", "ICT2132 practical — Lab 203 rescheduled (this week only)", "Tuesday ICT2132 practical moves to Lab 203, 13.30-15.30, due to maintenance in Lab 201. The register will be taken at the new venue.", "2026-09-14", "Administration"},
            new String[]{"GENERAL", "Library study-leave hours extended", "The faculty library will remain open until 21.00 on weekdays during study leave, starting 10 October.", "2026-09-08", "Administration"}
    ));

    private final JPanel list = new JPanel();
    private final JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    private String currentFilter = "All";
    private final String[] filters = {"All", "Examination", "Attendance", "Medicals", "Timetable", "General"};
    private final List<JLabel> filterLabels = new ArrayList<>();

    public Notices(User user) {
        super("Notices", "Latest faculty announcements and updates", null);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        buildFilters();

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 0, 0, 0));
        body.add(filterPanel, BorderLayout.NORTH);
        body.add(list, BorderLayout.CENTER);

        setScrollBody(body);
        refresh();
    }

    private void buildFilters() {
        filterPanel.removeAll();
        filterLabels.clear();
        for (String f : filters) {
            JLabel lbl = new JLabel(" " + f + " ");
            lbl.setFont(font(Font.PLAIN, 13));
            lbl.setOpaque(true);
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            updateFilterUI(lbl, f.equals(currentFilter));

            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    currentFilter = f;
                    for (JLabel label : filterLabels) {
                        updateFilterUI(label, label.getText().trim().equals(currentFilter));
                    }
                    refresh();
                }
            });
            filterLabels.add(lbl);
            filterPanel.add(lbl);
        }
    }

    private void updateFilterUI(JLabel lbl, boolean selected) {
        if (selected) {
            lbl.setBackground(new Color(20, 60, 50));
            lbl.setForeground(Color.WHITE);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(20, 60, 50), 1, true),
                    BorderFactory.createEmptyBorder(6, 14, 6, 14)
            ));
        } else {
            lbl.setBackground(Color.WHITE);
            lbl.setForeground(Color.DARK_GRAY);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                    BorderFactory.createEmptyBorder(6, 14, 6, 14)
            ));
        }
    }

    private void refresh() {
        list.removeAll();
        for (String[] n : notices) {
            if (currentFilter.equals("All") || n[0].equalsIgnoreCase(currentFilter)) {
                list.add(noticeCard(n));
                list.add(Box.createVerticalStrut(15));
            }
        }
        list.revalidate();
        list.repaint();
    }

    private JComponent noticeCard(String[] n) {
        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 25, 20, 25));

        LocalDate date = LocalDate.parse(n[3]);
        String day = String.format("%02d", date.getDayOfMonth());
        String month = date.getMonth().name().substring(0, 3).toUpperCase();

        JPanel dateBlock = new JPanel(new GridLayout(2, 1));
        dateBlock.setOpaque(false);

        JLabel dayLbl = new JLabel(day, SwingConstants.CENTER);
        dayLbl.setFont(font(Font.BOLD, 22));
        dayLbl.setForeground(new Color(20, 60, 50));

        JLabel monthLbl = new JLabel(month, SwingConstants.CENTER);
        monthLbl.setFont(font(Font.PLAIN, 11));
        monthLbl.setForeground(Color.GRAY);

        dateBlock.add(dayLbl);
        dateBlock.add(monthLbl);

        JPanel dateContainer = new JPanel(new BorderLayout());
        dateContainer.setOpaque(false);
        dateContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(235, 235, 235)),
                new EmptyBorder(0, 5, 0, 25)
        ));
        dateContainer.add(dateBlock, BorderLayout.NORTH);

        JPanel infoBlock = new JPanel();
        infoBlock.setLayout(new BoxLayout(infoBlock, BoxLayout.Y_AXIS));
        infoBlock.setOpaque(false);
        infoBlock.setBorder(new EmptyBorder(0, 20, 0, 0));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleRow.add(new Chip(n[0], new Color(46, 125, 50)));

        JLabel titleLbl = new JLabel(n[1]);
        titleLbl.setFont(font(Font.BOLD, 15));
        titleLbl.setForeground(Color.BLACK);
        titleRow.add(titleLbl);

        JTextArea msgArea = new JTextArea(n[2]);
        msgArea.setFont(font(Font.PLAIN, 13));
        msgArea.setForeground(Color.DARK_GRAY);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setEditable(false);
        msgArea.setOpaque(false);
        msgArea.setBorder(new EmptyBorder(10, 5, 12, 0));
        msgArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        String formattedDate = date.format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        JLabel footerLbl = new JLabel("posted by " + n[4] + " · " + formattedDate);
        footerLbl.setFont(font(Font.PLAIN, 11));
        footerLbl.setForeground(Color.GRAY);
        footerLbl.setBorder(new EmptyBorder(0, 5, 0, 0));
        footerLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoBlock.add(titleRow);
        infoBlock.add(msgArea);
        infoBlock.add(footerLbl);

        card.add(dateContainer, BorderLayout.WEST);
        card.add(infoBlock, BorderLayout.CENTER);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        return card;
    }
}