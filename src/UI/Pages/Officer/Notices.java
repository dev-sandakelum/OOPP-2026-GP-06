package UI.Pages.Officer;

import Exception.DataAccessException;
import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import UI.Pages.Components.RoundedButton;
import data.TechnicalOfficerData;
import model.Notice;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Officer "Notices" tab: notices for staff / everyone plus the officer's own ones.
// Officers can post notices and delete the ones they posted.
public class Notices extends Page {

    private static final List<String> AUDIENCES = List.of("ALL", "STAFF", "UNDERGRADUATE");

    private final TechnicalOfficerData officerData = new TechnicalOfficerData();
    private final int officerId;

    private final JPanel list = new JPanel();

    public Notices(User user) {
        super("Notices", "Announcements for staff and everyone", null);
        this.officerId = user == null ? -1 : user.getUser_id();

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
        List<Notice> notices = new ArrayList<>();
        try {
            notices = officerData.getNotices(officerId);
        } catch (DataAccessException ex) {
            info(this, "Failed to load notices: " + ex.getMessage());
        }

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
        JLabel title = new JLabel(n.getTitle());
        title.setFont(font(Font.BOLD, 16));
        title.setForeground(INK);

        String when = n.getCreated_date() == null ? ""
                : n.getCreated_date().toLocalDateTime().toLocalDate().toString();
        JLabel date = new JLabel(when);
        date.setFont(font(Font.PLAIN, 12));
        date.setForeground(INK_2);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        meta.setOpaque(false);
        meta.add(new Chip(n.getAudience() == null ? "" : n.getAudience(), BRAND));
        meta.add(date);

        JPanel head = new JPanel(new BorderLayout(0, 6));
        head.setOpaque(false);
        head.add(title, BorderLayout.NORTH);
        head.add(meta, BorderLayout.CENTER);

        JTextArea message = new JTextArea(n.getDescription() == null ? "" : n.getDescription());
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

        Card card = new Card(false);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.add(text, BorderLayout.CENTER);

        // delete button only on notices this officer posted
        if (n.getCreated_by() != null && n.getCreated_by() == officerId) {
            RoundedButton del = new RoundedButton("Delete", false);
            del.addActionListener(e -> deleteNotice(n));
            JPanel side = new JPanel(new BorderLayout());
            side.setOpaque(false);
            side.add(del, BorderLayout.NORTH);
            card.add(side, BorderLayout.EAST);
        }
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height + 40));
        return card;
    }

    private void deleteNotice(Notice n) {
        if (!confirm(this, "Delete \"" + n.getTitle() + "\"?")) return;
        try {
            if (!officerData.deleteNotice(n.getNotice_id(), officerId)) {
                info(this, "Nothing was deleted - you can only delete notices you posted.");
                return;
            }
        } catch (DataAccessException ex) {
            info(this, "Failed to delete the notice: " + ex.getMessage());
            return;
        }
        refresh();
    }

    private void postNotice() {
        String[] labels = {"Title", "Message", "Audience (ALL / STAFF / UNDERGRADUATE)"};
        String[] v = form(this, "Post notice", labels, new String[]{"", "", "ALL"});
        if (v == null) return;
        if (v[0].isEmpty() || v[1].isEmpty()) {
            info(this, "Title and message are required.");
            return;
        }
        String audience = v[2].isEmpty() ? "ALL" : v[2].toUpperCase();
        if (!AUDIENCES.contains(audience)) {
            info(this, "Audience must be ALL, STAFF or UNDERGRADUATE.");
            return;
        }

        Notice n = new Notice();
        n.setCreated_by(officerId);
        n.setTitle(v[0]);
        n.setDescription(v[1]);
        n.setAudience(audience);
        n.setStatus("ACTIVE");
        try {
            officerData.insertNotice(n);
        } catch (DataAccessException ex) {
            info(this, "Failed to post the notice: " + ex.getMessage());
            return;
        }
        refresh();
    }
}