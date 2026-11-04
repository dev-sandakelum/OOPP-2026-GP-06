package UI.Pages.admin;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import UI.Pages.Components.RoundedButton;
import data.NoticeData;
import model.Notice;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

/**
 * Admin "Notices" tab: post and delete notices.
 */
public class Notices extends Page {

    private final NoticeData noticeData = new NoticeData();
    private final JPanel list = new JPanel();
    private final User currentUser;

    public Notices(User currentUser) {
        super("Notices", "Post announcements for staff and undergraduates", null);
        this.currentUser = currentUser;

        RoundedButton post = new RoundedButton("+ Post notice", true);
        post.addActionListener(e -> postNotice());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false);
        top.add(post);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(top,  BorderLayout.NORTH);
        body.add(list, BorderLayout.CENTER);
        setScrollBody(body);

        refresh();
    }

    // -----------------------------------------------------------------
    //  Render
    // -----------------------------------------------------------------

    private void refresh() {
        list.removeAll();
        
        try {
            List<Notice> notices = noticeData.findAll();
            if (notices.isEmpty()) {
                JLabel empty = new JLabel("No notices yet.");
                empty.setFont(font(Font.PLAIN, 14));
                empty.setForeground(INK_2);
                list.add(empty);
            } else {
                for (Notice n : notices) {
                    list.add(noticeCard(n));
                    list.add(Box.createVerticalStrut(12));
                }
            }
        } catch (Exception ex) {
            JLabel error = new JLabel("Could not load notices: " + ex.getMessage());
            error.setFont(font(Font.PLAIN, 14));
            error.setForeground(BAD);
            list.add(error);
        }
        
        list.revalidate();
        list.repaint();
    }

    private JComponent noticeCard(Notice n) {
        JLabel title = new JLabel(n.getTitle());
        title.setFont(font(Font.BOLD, 16));
        title.setForeground(INK);

        String dateStr = n.getCreated_date() != null 
            ? n.getCreated_date().toString().substring(0, 10) 
            : "";
        JLabel date = new JLabel(dateStr);
        date.setFont(font(Font.PLAIN, 12));
        date.setForeground(INK_2);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        meta.setOpaque(false);
        meta.add(new Chip(n.getAudience() != null ? n.getAudience() : "Everyone", BRAND));
        meta.add(date);

        JPanel head = new JPanel(new BorderLayout(0, 6));
        head.setOpaque(false);
        head.add(title, BorderLayout.NORTH);
        head.add(meta,  BorderLayout.CENTER);

        JTextArea message = new JTextArea(n.getDescription());
        message.setFont(font(Font.PLAIN, 14));
        message.setForeground(INK);
        message.setOpaque(false);
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setBorder(null);

        JPanel text = new JPanel(new BorderLayout(0, 8));
        text.setOpaque(false);
        text.add(head,    BorderLayout.NORTH);
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

    // -----------------------------------------------------------------
    //  Actions
    // -----------------------------------------------------------------

    private void postNotice() {
        String[] labels = {"Title", "Message",
                "Audience (Everyone / Staff / Undergraduates)"};
        String[] v = form(this, "Post notice",
                labels, new String[]{"", "", "Everyone"});
        if (v == null) return;

        if (v[0].isEmpty() || v[1].isEmpty()) {
            info(this, "Title and message are required.");
            return;
        }

        String audience = v[2].isEmpty() ? "Everyone" : v[2];
        
        Notice notice = new Notice();
        notice.setTitle(v[0]);
        notice.setDescription(v[1]);
        notice.setAudience(audience);
        notice.setStatus("ACTIVE");
        if (currentUser != null) {
            notice.setCreated_by(currentUser.getUser_id());
        }
        // Expiry date can be set to null or a future date
        notice.setExpiry_date(null);

        try {
            noticeData.insert(notice);
            refresh();
        } catch (Exception ex) {
            info(this, "Could not post notice: " + ex.getMessage());
        }
    }

    private void deleteNotice(Notice n) {
        if (confirm(this, "Delete \"" + n.getTitle() + "\"?")) {
            try {
                noticeData.delete(n.getNotice_id());
                refresh();
            } catch (Exception ex) {
                info(this, "Could not delete notice: " + ex.getMessage());
            }
        }
    }
}
