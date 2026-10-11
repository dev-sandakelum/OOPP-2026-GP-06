package UI.Pages.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

// Shared look + small widgets for every admin tab (cards, buttons, tables, forms).
final class AdminUi {
    private AdminUi() { }

    // ---------- Colors ----------
    static final Color INK       = new Color(13, 31, 22);
    static final Color INK_2     = new Color(122, 135, 128);
    static final Color LINE      = new Color(229, 235, 231);
    static final Color LINE_2    = new Color(211, 220, 215);
    static final Color SURFACE   = Color.WHITE;
    static final Color SURFACE_2 = new Color(245, 248, 246);
    static final Color BRAND     = new Color(15, 61, 42);
    static final Color BRAND_LT  = new Color(38, 144, 95);
    static final Color BRAND_DK  = new Color(8, 38, 25);
    static final Color BRAND_SOFT = new Color(223, 237, 229);
    static final Color OK  = new Color(31, 122, 74);
    static final Color WARN = new Color(154, 106, 12);
    static final Color BAD  = new Color(192, 58, 43);

    static Font font(int style, float size) { return new Font("Segoe UI", style, Math.round(size)); }

    // ---------- Page frame: header on top, body below ----------
    static class Page extends JPanel {
        Page(String title, String sub, JComponent action) {
            super(new BorderLayout());
            setOpaque(false);

            JLabel t = new JLabel(title);
            t.setFont(font(Font.BOLD, 28));
            t.setForeground(INK);
            JLabel s = new JLabel(sub);
            s.setFont(font(Font.PLAIN, 14));
            s.setForeground(INK_2);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(t);
            text.add(Box.createVerticalStrut(4));
            text.add(s);

            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            head.setBorder(new EmptyBorder(24, 24, 16, 24));
            head.add(text, BorderLayout.WEST);
            if (action != null) {
                JPanel a = new JPanel(new GridBagLayout());
                a.setOpaque(false);
                a.add(action);
                head.add(a, BorderLayout.EAST);
            }
            add(head, BorderLayout.NORTH);
        }

        void setBody(JComponent body) {
            JPanel pad = new JPanel(new BorderLayout());
            pad.setOpaque(false);
            pad.setBorder(new EmptyBorder(0, 24, 24, 24));
            pad.add(body, BorderLayout.CENTER);
            add(pad, BorderLayout.CENTER);
        }

        // body that scrolls and follows the window width
        void setScrollBody(JComponent body) {
            ScrollBody sb = new ScrollBody();
            sb.setBorder(new EmptyBorder(0, 24, 24, 24));
            sb.add(body, BorderLayout.NORTH);
            JScrollPane sp = new JScrollPane(sb);
            sp.setBorder(null);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(sp, BorderLayout.CENTER);
        }
    }

    private static class ScrollBody extends JPanel implements Scrollable {
        ScrollBody() { super(new BorderLayout()); setOpaque(false); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // ---------- Card ----------
    static class Card extends JPanel {
        private final boolean dark;
        Card(boolean dark) { this.dark = dark; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape s = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 36, 36);
            if (dark) {
                g2.setPaint(new GradientPaint(0, 0, BRAND_LT.darker(), getWidth(), getHeight(), BRAND_DK));
                g2.fill(s);
            } else {
                g2.setColor(SURFACE);
                g2.fill(s);
                g2.setColor(LINE);
                g2.draw(s);
            }
            g2.dispose();
        }
    }

    // ---------- Button ----------
    static class Btn extends JButton {
        private final boolean primary;
        private boolean hover;
        Btn(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(font(Font.BOLD, 13));
            setForeground(primary ? Color.WHITE : INK);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(10, 18, 10, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape s = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            if (primary) {
                g2.setColor(hover ? BRAND_DK : BRAND);
                g2.fill(s);
            } else {
                g2.setColor(hover ? SURFACE_2 : SURFACE);
                g2.fill(s);
                g2.setColor(LINE_2);
                g2.draw(s);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- Text field ----------
    static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(font(Font.PLAIN, 14));
        f.setForeground(INK);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_2, 1, true), new EmptyBorder(8, 12, 8, 12)));
        return f;
    }

    static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(INK_2);
        return l;
    }

    // ---------- Table (flat: no grid, 44px rows, soft header, tinted selection) ----------
    static JTable table(DefaultTableModel model, Map<String, Color> badgeColors, int badgeCol) {
        JTable t = new JTable(model);
        t.setRowHeight(44);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setFont(font(Font.PLAIN, 14));
        t.setForeground(INK);
        t.setSelectionBackground(BRAND_SOFT);
        t.setSelectionForeground(INK);
        t.setOpaque(false);
        t.setAutoCreateRowSorter(true);

        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setFont(font(Font.BOLD, 12));
                l.setForeground(INK_2);
                l.setBackground(SURFACE_2);
                l.setOpaque(true);
                l.setBorder(new EmptyBorder(0, 16, 0, 16));
                return l;
            }
        });

        DefaultTableCellRenderer plain = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, s, false, r, c);
                l.setBorder(new EmptyBorder(0, 16, 0, 16));
                l.setBackground(s ? BRAND_SOFT : SURFACE);
                l.setOpaque(true);
                return l;
            }
        };
        t.setDefaultRenderer(Object.class, plain);

        if (badgeColors != null && badgeCol >= 0) {
            t.getColumnModel().getColumn(badgeCol).setCellRenderer(new TableCellRenderer() {
                private final ChipLabel chip = new ChipLabel();
                @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                    String text = String.valueOf(v);
                    chip.set(text, badgeColors.getOrDefault(text, INK_2), s ? BRAND_SOFT : SURFACE);
                    return chip;
                }
            });
        }
        return t;
    }

    // Table cell that paints a small colored chip with the text
    private static class ChipLabel extends JLabel {
        private Color col = INK_2, bg = SURFACE;
        void set(String text, Color col, Color bg) { setText(text); this.col = col; this.bg = bg; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(font(Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int w = fm.stringWidth(text) + 20, h = 24, x = 16, y = (getHeight() - h) / 2;
            g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 28));
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, 10, 10));
            g2.setColor(col);
            g2.drawString(text, x + 10, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // Table inside a rounded white card
    static JComponent tableCard(JTable t) {
        Card c = new Card(false);
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane sp = new JScrollPane(t);
        sp.setColumnHeaderView(t.getTableHeader());   // explicit, so the header shows even before the window is displayed
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        c.add(sp, BorderLayout.CENTER);
        return c;
    }

    // ---------- Simple form dialog: returns the values, or null if cancelled ----------
    static String[] form(Component parent, String title, String[] labels, String[] initial) {
        JPanel p = new JPanel(new GridLayout(labels.length * 2, 1, 0, 4));
        JTextField[] fs = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            p.add(label(labels[i]));
            fs[i] = field(22);
            if (initial != null && initial[i] != null) fs[i].setText(initial[i]);
            p.add(fs[i]);
        }
        int r = JOptionPane.showConfirmDialog(parent, p, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;
        String[] out = new String[labels.length];
        for (int i = 0; i < out.length; i++) out[i] = fs[i].getText().trim();
        return out;
    }

    static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }
}
