package UI.Pages.admin;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import UI.Pages.Components.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Admin "Notices" tab: post and delete notices.
// A notice is a String[]: {title, message, audience, date}.
public class Notices extends Page {

    // ---------- Placeholder data (swap for services later) ----------
    private final List<String[]> notices = new ArrayList<>(List.of(
            new String[]{"Mid-semester examinations", "Mid-semester exams begin next Monday. Check the timetable for your venue.", "Everyone", "2026-10-05"},
            new String[]{"Medical submission deadline", "Medical certificates must be submitted within 7 days of the absence.", "Undergraduates", "2026-10-02"},
            new String[]{"Lab maintenance", "Computer Lab 2 is closed on Friday for maintenance.", "Staff", "2026-09-29"},
            new String[]{"Assignment 2 released", "Assignment 2 for all level 1 modules is now available.", "Undergraduates", "2026-09-25"},
            new String[]{"Staff meeting", "Faculty staff meeting on Thursday at 3 PM in the main board room.", "Staff", "2026-09-20"}
    ));
    // ---------- end placeholder data ----------

    private final JPanel list = new JPanel();

    public Notices() {
        super("Notices", "Post announcements for staff and undergraduates", null);

        RoundedButton post = new RoundedButton("+ Post notice", true);
        post.addActionListener(e -> postNotice());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false);
        top.add(post);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(list, BorderLayout.CENTER);
        setScrollBody(body);
        refresh();
    }

    private void refresh() {
        list.removeAll();
        if (notices.isEmpty()) {
            JLabel empty = new JLabel("No notices yet.");
            empty.setFont(font(Font.PLAIN, 14));
            empty.setForeground(INK_2);
            list.add(empty);
        }
        for (String[] n : notices) {
            list.add(noticeCard(n));
            list.add(Box.createVerticalStrut(12));
        }
        list.revalidate();
        list.repaint();
    }

    private JComponent noticeCard(String[] n) {
        JLabel title = new JLabel(n[0]);
        title.setFont(font(Font.BOLD, 16));
        title.setForeground(INK);

        JLabel date = new JLabel(n[3]);
        date.setFont(font(Font.PLAIN, 12));
        date.setForeground(INK_2);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        meta.setOpaque(false);
        meta.add(new Chip(n[2], BRAND));
        meta.add(date);

        JPanel head = new JPanel(new BorderLayout(0, 6));
        head.setOpaque(false);
        head.add(title, BorderLayout.NORTH);
        head.add(meta, BorderLayout.CENTER);

        JTextArea message = new JTextArea(n[1]);
        message.setFont(font(Font.PLAIN, 14));
        message.setForeground(INK);
        message.setOpaque(false);
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setBorder(null);

        JPanel text = new JPanel(new BorderLayout(0, 8));
        text.setOpaque(false);
        text.add(head, BorderLayout.NORTH);
        text.add(message, BorderLayout.CENTER);

        RoundedButton del = new RoundedButton("Delete", false);
        del.addActionListener(e -> deleteNotice(n));
        JPanel side = new JPanel(new BorderLayout());
        side.setOpaque(false);
        side.add(del, BorderLayout.NORTH);

        Card card = new Card(false);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.add(text, BorderLayout.CENTER);
        card.add(side, BorderLayout.EAST);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height + 40));
        return card;
    }

    private void deleteNotice(String[] n) {
        if (confirm(this, "Delete \"" + n[0] + "\"?")) {
            notices.remove(n);
            refresh();
        }
    }

    private void postNotice() {
        String[] labels = {"Title", "Message", "Audience (Everyone / Staff / Undergraduates)"};
        String[] v = form(this, "Post notice", labels, new String[]{"", "", "Everyone"});
        if (v == null) return;
        if (v[0].isEmpty() || v[1].isEmpty()) {
            info(this, "Title and message are required.");
            return;
        }
        String audience = v[2].isEmpty() ? "Everyone" : v[2];
        notices.add(0, new String[]{v[0], v[1], audience, LocalDate.now().toString()});
        refresh();
    }
}
