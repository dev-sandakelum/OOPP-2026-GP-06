package UI.Pages.lecturer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

// Shared look + small widgets for every admin tab (cards, buttons, tables, forms).
final class LecturerUI {
    private LecturerUI() { }

    // ---------- Colors (aliases of the shared theme in UI.Kit.Palette) ----------
    static final Color INK       = UI.Kit.Palette.INK;
    static final Color INK_2     = UI.Kit.Palette.INK_2;
    static final Color LINE      = UI.Kit.Palette.LINE;
    static final Color LINE_2    = UI.Kit.Palette.LINE_2;
    static final Color SURFACE   = UI.Kit.Palette.SURFACE;
    static final Color SURFACE_2 = UI.Kit.Palette.SURFACE_2;
    static final Color BRAND     = UI.Kit.Palette.BRAND;
    static final Color BRAND_LT  = UI.Kit.Palette.BRAND_LT;
    static final Color BRAND_DK  = UI.Kit.Palette.BRAND_DK;
    static final Color BRAND_SOFT = UI.Kit.Palette.BRAND_SOFT;
    static final Color OK  = UI.Kit.Palette.OK;
    static final Color WARN = UI.Kit.Palette.WARN;
    static final Color BAD  = UI.Kit.Palette.BAD;

    static Font font(int style, float size) { return UI.Kit.Palette.font(style, size); }

    // ---------- Page frame: header on top, body below ----------
    static class Page extends JPanel {
        Page(String title, String sub, JComponent action) {
            super(new BorderLayout());
            setOpaque(false);

            JLabel t = new JLabel(title);
            t.setFont(font(Font.BOLD, 30));
            t.setForeground(INK);
            t.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel s = new JLabel(sub);
            s.setFont(font(Font.PLAIN, 15));
            s.setForeground(INK_2);
            s.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(t);
            text.add(Box.createVerticalStrut(8));
            text.add(new UI.Pages.Components.AccentBar(56, 4));
            text.add(Box.createVerticalStrut(10));
            text.add(s);

            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            head.setBorder(new EmptyBorder(26, 24, 18, 24));
            head.add(text, BorderLayout.WEST);
            if (action != null) {
                JPanel a = new JPanel(new GridBagLayout());
                a.setOpaque(false);
                a.add(action);
                head.add(a, BorderLayout.EAST);
            }
            add(head, BorderLayout.NORTH);
        }

        Page() {
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
            UI.Pages.Components.GlassScrollBarUI.install(sp);
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

    // ---------- Card (glass card from the shared components) ----------
    static class Card extends UI.Pages.Components.Card {
        Card(boolean dark) { super(dark); }
    }

    // ---------- Button (rounded button from the shared components) ----------
    static class Btn extends UI.Pages.Components.RoundedButton {
        Btn(String text, boolean primary) { super(text, primary); }
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
        sp.setBorder(BorderFactory.createEmptyBorder());   // non-UIResource: JTable will not re-apply the LAF border
        UI.Pages.Components.GlassScrollBarUI.install(sp);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        c.add(sp, BorderLayout.CENTER);
        return c;
    }

    // ---------- Dialogs: same glass overlay pop-ups as the rest of the app ----------
    static String[] form(Component parent, String title, String[] labels, String[] initial) {
        return UI.Pages.Components.Ui.form(parent, title, labels, initial);
    }

    static boolean confirm(Component parent, String message) {
        return UI.Pages.Components.Ui.confirm(parent, message);
    }

    static void info(Component parent, String message) {
        UI.Pages.Components.Ui.info(parent, message);
    }
}
