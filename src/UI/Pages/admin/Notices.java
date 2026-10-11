package UI.Pages.admin;

import UI.Pages.admin.AdminUi.Btn;
import UI.Pages.admin.AdminUi.Card;
import UI.Pages.admin.AdminUi.Page;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static UI.Pages.admin.AdminUi.*;

// Admin "Notices" tab: post and delete notices.
public class Notices extends Page {

    private static class Notice {
        String title, body, audience, date;
        Notice(String title, String body, String audience, String date) {
            this.title = title; this.body = body; this.audience = audience; this.date = date;
        }
    }

    // ---------- Placeholder data (swap for MockData / services later) ----------
    private final List<Notice> notices = new ArrayList<>(List.of(
            new Notice("Mid-semester examinations", "Mid-semester exams begin next Monday. Check the timetable for your venue.", "Everyone", "2026-10-05"),
            new Notice("Medical submission deadline", "Medical certificates must be submitted within 7 days of the absence.", "Undergraduates", "2026-10-02"),
            new Notice("Lab maintenance", "Computer Lab 2 is closed on Friday for maintenance.", "Staff", "2026-09-29"),
            new Notice("Assignment 2 released", "Assignment 2 for all level 1 modules is now available.", "Undergraduates", "2026-09-25"),
            new Notice("Staff meeting", "Faculty staff meeting on Thursday at 3 PM in the main board room.", "Staff", "2026-09-20")
    ));
    // ---------- end placeholder data ----------

    private final JPanel list = new JPanel();

    public Notices() {
        super("Notices", "Post announcements for staff and undergraduates", null);

        Btn post = new Btn("+ Post notice", true);
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
        for (Notice n : notices) {
            list.add(noticeCard(n));
            list.add(Box.createVerticalStrut(12));
        }
        list.revalidate();
        list.repaint();
    }

    private JComponent noticeCard(Notice n) {
        Card c = new Card(false);
        c.setLayout(new BorderLayout(16, 0));
        c.setBorder(new EmptyBorder(16, 20, 16, 20));
        c.setAlignmentX(LEFT_ALIGNMENT);

        JLabel t = new JLabel(n.title);
        t.setFont(font(Font.BOLD, 16));
        t.setForeground(INK);

        JLabel chip = new JLabel(n.audience, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BRAND_SOFT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(font(Font.BOLD, 11));
        chip.setForeground(BRAND);
        chip.setBorder(new EmptyBorder(3, 10, 3, 10));

        JLabel date = new JLabel(n.date);
        date.setFont(font(Font.PLAIN, 12));
        date.setForeground(INK_2);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        meta.setOpaque(false);
        meta.add(chip);
        meta.add(date);

        JTextArea body = new JTextArea(n.body);
        body.setFont(font(Font.PLAIN, 14));
        body.setForeground(INK);
        body.setOpaque(false);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setBorder(null);

        JPanel head = new JPanel(new BorderLayout(0, 6));
        head.setOpaque(false);
        head.add(t, BorderLayout.NORTH);
        head.add(meta, BorderLayout.CENTER);

        JPanel text = new JPanel(new BorderLayout(0, 8));
        text.setOpaque(false);
        text.add(head, BorderLayout.NORTH);
        text.add(body, BorderLayout.CENTER);

        Btn del = new Btn("Delete", false);
        del.addActionListener(e -> {
            if (confirm(this, "Delete \"" + n.title + "\"?")) { notices.remove(n); refresh(); }
        });
        JPanel side = new JPanel(new BorderLayout());
        side.setOpaque(false);
        side.add(del, BorderLayout.NORTH);

        c.add(text, BorderLayout.CENTER);
        c.add(side, BorderLayout.EAST);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 40));
        return c;
    }

    private void postNotice() {
        String[] v = form(this, "Post notice", new String[]{"Title", "Message", "Audience (Everyone / Staff / Undergraduates)"},
                new String[]{"", "", "Everyone"});
        if (v == null) return;
        if (v[0].isEmpty() || v[1].isEmpty()) { info(this, "Title and message are required."); return; }
        notices.add(0, new Notice(v[0], v[1], v[2].isEmpty() ? "Everyone" : v[2], LocalDate.now().toString()));
        refresh();
    }
}
