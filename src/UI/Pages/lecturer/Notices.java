package UI.Pages.lecturer;

import UI.Pages.Components.Card;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static UI.Kit.Palette.*;

public class Notices extends LecturerUI.Page {

    private static final Color PRIMARY = new Color(0x1F4D36);
    private static final Color BORDER  = new Color(0xE0E8E2);
    private static final Color SOFT    = new Color(0xE1EDE3);

    /** One notice (matches the columns of the notices table). */
    public static final class Notice {
        public final String title, course, date, category, status, body;
        public Notice(String title, String course, String date, String category, String status, String body) {
            this.title = title; this.course = course; this.date = date;
            this.category = category; this.status = status; this.body = body;
        }
    }

    private final List<Notice> all = new ArrayList<>();

    // list side
    private final JPanel listPanel = new JPanel();
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> courseFilter = new JComboBox<>();
    private final JComboBox<String> statusFilter =
            new JComboBox<>(new String[]{"All statuses", "Published", "Draft"});

    // compose side
    private final JTextField titleField = new JTextField();
    private final JComboBox<String> courseBox = new JComboBox<>();
    private final JComboBox<String> categoryBox =
            new JComboBox<>(new String[]{"General", "Lecture", "Assignment", "Exam"});
    private final JTextArea messageArea = new JTextArea(7, 20);

    public Notices() {
        super("Notices", "Notices for your modules", null);

        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.NORTH;
        gc.gridy = 0;
        gc.weighty = 1;

        gc.gridx = 0; gc.weightx = 1.2; gc.insets = new Insets(0, 0, 0, 8);
        body.add(buildListSide(), gc);

        gc.gridx = 1; gc.weightx = 1.0; gc.insets = new Insets(0, 8, 0, 0);
        JPanel right = new JPanel(new BorderLayout());     // keeps the form at its natural height
        right.setOpaque(false);
        right.add(buildComposeCard(), BorderLayout.NORTH);
        body.add(right, gc);

        setScrollBody(body);

        loadCourses();
        loadNotices();
    }

    /* ===================================================================== */
    /*  DATABASE                                                              */
    /* ===================================================================== */

    /**
     * TODO (DATABASE): load the lecturer's modules here (used by both course drop-downs).
     *
     *   SELECT course_code FROM courses WHERE lecturer_id = ?;
     */
    private void loadCourses() {
        List<String> codes = List.of("CS3012", "CS3022", "CS2042", "CS4013", "CS4091");   // mock data

        courseFilter.removeAllItems();
        courseFilter.addItem("All modules");
        courseBox.removeAllItems();
        courseBox.addItem("All my modules");
        for (String c : codes) { courseFilter.addItem(c); courseBox.addItem(c); }
    }

    /**
     * TODO (DATABASE): load the notices here.
     *
     * Get the notices for the logged-in lecturer's modules from MySQL, for example:
     *
     *   SELECT n.title, c.course_code, n.posted_date, n.category, n.status, n.message
     *   FROM notices n
     *   LEFT JOIN courses c ON c.course_id = n.course_id
     *   WHERE n.lecturer_id = ?
     *   ORDER BY n.posted_date DESC;
     *
     * Convert each row to a Notice (course = "All modules" when course_id is NULL)
     * and call setNotices(list).
     *
     * (The mock data below is only for design preview. Delete it when the DB code is ready.)
     */
    private void loadNotices() {
        List<Notice> list = new ArrayList<>();
        list.add(new Notice("CS3012 – Assignment 2 deadline extended", "CS3012", "25 Oct 2026", "Assignment", "Published",
                "The deadline for Assignment 2 is extended to 2 November, 11:59 PM. Submissions via the LMS only."));
        list.add(new Notice("Lab session rescheduled to Thursday", "CS3022", "22 Oct 2026", "Lecture", "Published",
                "Next week's lab moves to Thursday 14:00 in Lab 3."));
        list.add(new Notice("Mid-term exam timetable released", "All modules", "20 Oct 2026", "Exam", "Published",
                "Mid-term timetables are now available. Please check your hall allocation."));
        list.add(new Notice("ML project proposal guidelines", "CS4013", "—", "General", "Draft",
                "Proposal should be no more than 4 pages, including references."));
        list.add(new Notice("Guest lecture: Cloud Databases", "CS3012", "18 Oct 2026", "Lecture", "Published",
                "Join us in LT-2 on Friday at 3 PM."));
        setNotices(list);
    }

    /**
     * TODO (DATABASE): save the new notice here.
     *
     *   INSERT INTO notices (lecturer_id, course_id, title, category, message, status, posted_date)
     *   VALUES (?, ?, ?, ?, ?, ?, CURDATE());
     *
     * status is "Published" or "Draft". After the insert succeeds, the notice is added to the list below.
     */
    private void saveNotice(String status) {
        String title = titleField.getText().trim();
        String message = messageArea.getText().trim();
        if (title.isEmpty() || message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title and a message.",
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String course = (String) courseBox.getSelectedItem();
        if (course != null && course.startsWith("All")) course = "All modules";
        String date = status.equals("Draft") ? "—"
                : LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));

        Notice n = new Notice(title, course, date, (String) categoryBox.getSelectedItem(), status, message);

        // ---- DATABASE INSERT GOES HERE (see the comment above) ----

        all.add(0, n);
        refreshList();
        titleField.setText("");
        messageArea.setText("");
    }

    /** Fill the list with notices from the database. */
    public void setNotices(List<Notice> notices) {
        all.clear();
        all.addAll(notices);
        refreshList();
    }

    /* ===================================================================== */
    /*  LEFT: LIST                                                            */
    /* ===================================================================== */

    private JComponent buildListSide() {
        JPanel side = new JPanel(new BorderLayout(0, 14));
        side.setOpaque(false);

        // toolbar
        searchField.setFont(font(Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 12, 8, 12)));
        searchField.putClientProperty("JTextField.placeholderText", "Search notices");   // shows with FlatLaf
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refreshList(); }
            public void removeUpdate(DocumentEvent e)  { refreshList(); }
            public void changedUpdate(DocumentEvent e) { refreshList(); }
        });
        courseFilter.setFont(font(Font.PLAIN, 12));
        statusFilter.setFont(font(Font.PLAIN, 12));
        courseFilter.addActionListener(e -> refreshList());
        statusFilter.addActionListener(e -> refreshList());

        JPanel combos = new JPanel(new GridLayout(1, 2, 8, 0));
        combos.setOpaque(false);
        combos.add(courseFilter);
        combos.add(statusFilter);

        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.add(searchField, BorderLayout.CENTER);
        bar.add(combos, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout());     // keeps cards at natural height
        top.setOpaque(false);
        top.add(listPanel, BorderLayout.NORTH);

        side.add(bar, BorderLayout.NORTH);
        side.add(top, BorderLayout.CENTER);
        return side;
    }

    private void refreshList() {
        String q = searchField.getText().trim().toLowerCase();
        String course = (String) courseFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();

        listPanel.removeAll();
        int shown = 0;
        for (Notice n : all) {
            if (!q.isEmpty() && !(n.title.toLowerCase().contains(q) || n.body.toLowerCase().contains(q))) continue;
            if (course != null && !course.startsWith("All") && !course.equals(n.course)) continue;
            if (status != null && !status.startsWith("All") && !status.equals(n.status)) continue;
            listPanel.add(noticeCard(n));
            shown++;
        }
        if (shown == 0) {
            JLabel empty = new JLabel("No notices found");
            empty.setFont(font(Font.PLAIN, 13));
            empty.setForeground(INK_2);
            empty.setBorder(new EmptyBorder(30, 0, 0, 0));
            listPanel.add(empty);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JComponent noticeCard(Notice n) {
        Card card = new Card(false);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // title + status
        JLabel title = new JLabel(n.title);
        title.setFont(font(Font.BOLD, 14));
        title.setForeground(INK);
        JPanel head = new JPanel(new BorderLayout(10, 0));
        head.setOpaque(false);
        head.add(title, BorderLayout.CENTER);
        head.add(new Pill(n.status, n.status.equals("Published") ? new Color(0xE1F1E8) : new Color(0xF3F6F3),
                n.status.equals("Published") ? new Color(0x2E7D5B) : new Color(0x6B7C72)), BorderLayout.EAST);

        // message (fixed width so the HTML wraps correctly)
        JLabel msg = new JLabel("<html><div style='width:330px'>" + escape(n.body) + "</div></html>");
        msg.setFont(font(Font.PLAIN, 12));
        msg.setForeground(INK_2);

        // category + course · date
        JLabel meta = new JLabel(n.course + "  ·  " + n.date);
        meta.setFont(font(Font.PLAIN, 11));
        meta.setForeground(INK_2);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        foot.setOpaque(false);
        foot.add(new Pill(n.category, new Color(0xE3EDF8), new Color(0x3A6EA5)));
        foot.add(meta);

        card.add(head, BorderLayout.NORTH);
        card.add(msg, BorderLayout.CENTER);
        card.add(foot, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());     // spacing between cards
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    /* ===================================================================== */
    /*  RIGHT: COMPOSE                                                        */
    /* ===================================================================== */

    private JComponent buildComposeCard() {
        Card card = new Card(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 20, 20));

        JLabel head = new JLabel("Compose Notice");
        head.setFont(font(Font.BOLD, 15));
        head.setForeground(INK);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(head);
        card.add(Box.createVerticalStrut(14));

        styleInput(titleField);
        card.add(field("Title", titleField));

        courseBox.setFont(font(Font.PLAIN, 12));
        categoryBox.setFont(font(Font.PLAIN, 12));
        JPanel two = new JPanel(new GridLayout(1, 2, 12, 0));
        two.setOpaque(false);
        two.add(field("Module", courseBox));
        two.add(field("Category", categoryBox));
        two.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(two);

        messageArea.setFont(font(Font.PLAIN, 12));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane sp = new JScrollPane(messageArea);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        card.add(field("Message", sp));

        JButton draft = button("Save Draft", false);
        JButton publish = button("Publish", true);
        draft.addActionListener(e -> saveNotice("Draft"));
        publish.addActionListener(e -> saveNotice("Published"));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        btns.add(draft);
        btns.add(publish);
        card.add(Box.createVerticalStrut(4));
        card.add(btns);
        return card;
    }

    private JComponent field(String label, JComponent input) {
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(font(Font.BOLD, 11));
        l.setForeground(INK_2);

        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 14, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private void styleInput(JTextField f) {
        f.setFont(font(Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
    }

    private JButton button(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(font(Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setOpaque(true);
        if (primary) { b.setBackground(PRIMARY); b.setForeground(Color.WHITE); b.setBorderPainted(false); }
        else {
            b.setBackground(Color.WHITE); b.setForeground(INK);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 17, 8, 17)));
        }
        return b;
    }

    /* ===================================================================== */

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Small rounded tag (status / category). */
    private static class Pill extends JLabel {
        private final Color bg;
        Pill(String text, Color bg, Color fg) {
            super(text, SwingConstants.CENTER);
            this.bg = bg;
            setOpaque(false);
            setForeground(fg);
            setFont(font(Font.BOLD, 11));
            setBorder(new EmptyBorder(3, 11, 3, 11));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}