
package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import data.NoticeData;
import Exception.DataAccessException;
import model.Notice;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static UI.Kit.Palette.*;

public class Notices extends Page {

    private final NoticeData noticeData = new NoticeData();

    private final JPanel list = new JPanel();
    private final JPanel filterPanel =
            new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));

    private final JLabel emptyLabel = new JLabel(
            "No announcements available at the moment.",
            SwingConstants.CENTER
    );

    private final List<Notice> notices = new ArrayList<>();

    public Notices(User user) {
        super("Notices", "Latest faculty announcements and updates", null);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel heading = new JLabel("ALL ANNOUNCEMENTS");
        heading.setFont(font(Font.BOLD, 13));
        heading.setForeground(BRAND);
        filterPanel.add(heading);

        emptyLabel.setFont(font(Font.PLAIN, 13));
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setBorder(new EmptyBorder(30, 10, 30, 10));

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 0, 0, 0));

        body.add(filterPanel, BorderLayout.NORTH);
        body.add(list, BorderLayout.CENTER);

        setScrollBody(body);

        loadNotices();
    }

    // Load notices intended for undergraduate students and everyone.
    private void loadNotices() {

        list.removeAll();
        notices.clear();

        try {
            List<Notice> undergraduateNotices =
                    noticeData.findByAudience("UNDERGRADUATE");

            List<Notice> allNotices =
                    noticeData.findByAudience("ALL");

            if (undergraduateNotices != null) {
                notices.addAll(undergraduateNotices);
            }

            if (allNotices != null) {
                notices.addAll(allNotices);
            }

            LocalDate today = LocalDate.now();

            // Display only active and non-expired notices.
            notices.removeIf(notice -> {
                if (notice == null) {
                    return true;
                }

                if (notice.getStatus() == null ||
                        !notice.getStatus().equalsIgnoreCase("ACTIVE")) {
                    return true;
                }

                return notice.getExpiry_date() != null &&
                        notice.getExpiry_date().toLocalDate().isBefore(today);
            });

            // Latest notices first.
            notices.sort(
                    Comparator.comparing(
                            Notice::getCreated_date,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );

            refresh();

        } catch (DataAccessException e) {

            e.printStackTrace();

            showEmptyMessage("Unable to load announcements. Please try again.");

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load notices from the database.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception e) {

            e.printStackTrace();

            showEmptyMessage("Something went wrong while loading announcements.");
        }
    }

    private void refresh() {

        list.removeAll();

        if (notices.isEmpty()) {
            showEmptyMessage("No announcements available at the moment.");
            return;
        }

        emptyLabel.setVisible(false);

        for (Notice notice : notices) {
            list.add(noticeCard(notice));
            list.add(Box.createVerticalStrut(15));
        }

        list.revalidate();
        list.repaint();
    }

    private void showEmptyMessage(String message) {

        list.removeAll();

        emptyLabel.setText(message);
        emptyLabel.setVisible(true);

        list.add(emptyLabel);

        list.revalidate();
        list.repaint();
    }

    private JComponent noticeCard(Notice notice) {

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 25, 20, 25));

        Timestamp createdDate = notice.getCreated_date();

        LocalDate date = createdDate != null
                ? createdDate.toLocalDateTime().toLocalDate()
                : LocalDate.now();

        String day = String.format("%02d", date.getDayOfMonth());

        String month = date.getMonth()
                .name()
                .substring(0, 3)
                .toUpperCase();

        // Date block
        JPanel dateBlock = new JPanel(new GridLayout(2, 1));
        dateBlock.setOpaque(false);

        JLabel dayLbl = new JLabel(day, SwingConstants.CENTER);
        dayLbl.setFont(font(Font.BOLD, 22));
        dayLbl.setForeground(BRAND);

        JLabel monthLbl = new JLabel(month, SwingConstants.CENTER);
        monthLbl.setFont(font(Font.PLAIN, 11));
        monthLbl.setForeground(Color.GRAY);

        dateBlock.add(dayLbl);
        dateBlock.add(monthLbl);

        JPanel dateContainer = new JPanel(new BorderLayout());
        dateContainer.setOpaque(false);

        dateContainer.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 0, 1, LINE
                        ),
                        new EmptyBorder(0, 5, 0, 25)
                )
        );

        dateContainer.add(dateBlock, BorderLayout.NORTH);

        // Notice information
        JPanel infoBlock = new JPanel();
        infoBlock.setLayout(new BoxLayout(infoBlock, BoxLayout.Y_AXIS));
        infoBlock.setOpaque(false);
        infoBlock.setBorder(new EmptyBorder(0, 20, 0, 0));

        JPanel titleRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 0)
        );

        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Generic label because the database has no category column.
        titleRow.add(new Chip("ANNOUNCEMENT", OK));

        JLabel titleLbl = new JLabel(
                notice.getTitle() != null
                        ? notice.getTitle()
                        : "Untitled Notice"
        );

        titleLbl.setFont(font(Font.BOLD, 15));
        titleLbl.setForeground(Color.BLACK);

        titleRow.add(titleLbl);

        JTextArea msgArea = new JTextArea(
                notice.getDescription() != null
                        ? notice.getDescription()
                        : ""
        );

        msgArea.setFont(font(Font.PLAIN, 13));
        msgArea.setForeground(Color.DARK_GRAY);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setEditable(false);
        msgArea.setFocusable(false);
        msgArea.setOpaque(false);
        msgArea.setBorder(new EmptyBorder(10, 5, 12, 0));
        msgArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        String formattedDate =
                date.format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        JLabel footerLbl = new JLabel(
                "Published · " + formattedDate
        );

        footerLbl.setFont(font(Font.PLAIN, 11));
        footerLbl.setForeground(Color.GRAY);
        footerLbl.setBorder(new EmptyBorder(0, 5, 0, 0));
        footerLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoBlock.add(titleRow);
        infoBlock.add(msgArea);
        infoBlock.add(footerLbl);

        card.add(dateContainer, BorderLayout.WEST);
        card.add(infoBlock, BorderLayout.CENTER);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        return card;
    }
}
