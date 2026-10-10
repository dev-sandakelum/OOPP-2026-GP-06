package UI.Pages.Components;

import UI.Kit.FrameData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

// Reusable rounded sidebar: logo + role label + nav items + account card.
// Drop it into BorderLayout.WEST of any page.
public class Sidebar extends JPanel implements FrameData {

    private static final int RADIUS = 28;
    private static final Color DARK       = new Color(15, 61, 42);
    private static final Color DARK_DEEP  = new Color(8, 38, 25);
    private static final Color GREEN_GLOW = new Color(38, 144, 95);

    private final List<NavItem> items = new ArrayList<>();
    private int selectedIndex = -1;
    private IntConsumer onSelect = i -> { };
    private Runnable onLogout = () -> { };

    public Sidebar(String roleLabel, String[] tabs, NavIcon[] icons,
                   String userName, String userSub, Color pageBg) {
        super(new BorderLayout());
        setBackground(pageBg);
        setBorder(new EmptyBorder(12, 12, 12, 6));          // the invisible gaps
        setPreferredSize(new Dimension(sp_width + 18, 0));

        RoundedCard card = new RoundedCard(new BorderLayout(), SIDEBAR_BG, pageBg, RADIUS);
        card.add(logoSection(), BorderLayout.NORTH);
        card.add(navSection(roleLabel, tabs, icons), BorderLayout.CENTER);
        card.add(accountSection(userName, userSub), BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        setSelectedIndex(0, false);
    }

    // ---------- Public API ----------

    // index of the selected nav item (0-based, same order as the tabs array)
    public int getSelectedIndex() { return selectedIndex; }

    public void setSelectedIndex(int index) { setSelectedIndex(index, true); }

    // called with the new index whenever the user picks a tab
    public void setOnSelect(IntConsumer listener) { this.onSelect = listener; }

    // called when the logout button is clicked
    public void setOnLogout(Runnable action) { this.onLogout = action; }

    private void setSelectedIndex(int index, boolean notify) {
        if (index < 0 || index >= items.size()) return;
        selectedIndex = index;
        for (int i = 0; i < items.size(); i++) items.get(i).setSelected(i == index);
        if (notify) onSelect.accept(index);
    }

    // ---------- Logo ----------
    private JPanel logoSection() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(SIDEBAR_BG);
        p.setBorder(new EmptyBorder(18, 18, 6, 14));
        p.setPreferredSize(new Dimension(sp_width, 78));

        JComponent badge = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(DARK);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String s = "UoR";
                g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(44, 44));

        JLabel title = new JLabel("FOTIMS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(DARK);
        JLabel sub = tracked("FACULTY OF TECH.", 10f, LABEL_COL);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(title);
        text.add(sub);
        text.add(Box.createVerticalGlue());

        JPanel badgeWrap = new JPanel(new GridBagLayout());   // keeps the badge 44x44
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        p.add(badgeWrap, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    // ---------- Nav ----------
    private JPanel navSection(String roleLabel, String[] tabs, NavIcon[] icons) {
        JPanel np = new JPanel();
        np.setLayout(new BoxLayout(np, BoxLayout.Y_AXIS));
        np.setBackground(SIDEBAR_BG);
        np.setBorder(new EmptyBorder(10, 14, 14, 14));

        JLabel role = tracked(roleLabel, 11f, LABEL_COL);
        role.setBorder(new EmptyBorder(8, 14, 0, 0));          // lines up with the icons
        np.add(role);
        np.add(Box.createVerticalStrut(8));

        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            NavItem item = new NavItem(tabs[i], icons[i]);
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { setSelectedIndex(index, true); }
            });
            items.add(item);
            np.add(item);
            np.add(Box.createVerticalStrut(6));
        }
        np.add(Box.createVerticalGlue());
        return np;
    }

    // ---------- Account ----------
    private JPanel accountSection(String name, String sub) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(SIDEBAR_BG);
        wrap.setBorder(new EmptyBorder(0, 14, 14, 14));

        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, GREEN_GLOW.darker(), getWidth(), getHeight(), DARK_DEEP));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 22, 22));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setPreferredSize(new Dimension(0, 78));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLbl.setForeground(Color.WHITE);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(190, 215, 200));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(nameLbl);
        text.add(subLbl);
        text.add(Box.createVerticalGlue());

        card.add(new Avatar(initials(name)), BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        card.add(new LogoutButton(() -> onLogout.run()), BorderLayout.EAST);

        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    // ---------- Helpers ----------
    private static JLabel tracked(String text, float size, Color color) {
        JLabel l = new JLabel(text);
        Map<TextAttribute, Object> a = new HashMap<>();
        a.put(TextAttribute.FAMILY, "Segoe UI");
        a.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        a.put(TextAttribute.SIZE, size);
        a.put(TextAttribute.TRACKING, 0.12f);
        l.setFont(new Font(a));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "?";
        String s = parts[0].substring(0, 1);
        if (parts.length > 1) s += parts[parts.length - 1].substring(0, 1);
        return s.toUpperCase();
    }

    // ---------- Inner components ----------
    private static class Avatar extends JComponent {
        private final String text;
        Avatar(String text) {
            this.text = text;
            setPreferredSize(new Dimension(44, 44));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight()) - 2;
            int y = (getHeight() - d) / 2;
            g2.setColor(new Color(120, 175, 145));
            g2.fill(new Ellipse2D.Float(1, y, d, d));
            g2.setColor(new Color(255, 255, 255, 110));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Float(1, y, d, d));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    private static class LogoutButton extends JComponent {
        private boolean hover;
        LogoutButton(Runnable action) {
            setPreferredSize(new Dimension(36, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Log out");
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { action.run(); }
            });
        }
        @Override public Dimension getMaximumSize() { return getPreferredSize(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight());
            int y = (getHeight() - s) / 2;
            g2.setColor(new Color(255, 255, 255, hover ? 55 : 28));
            g2.fill(new RoundRectangle2D.Float(0, y, s, s, 12, 12));
            NavIcon.LOGOUT.draw(g2, (s - NavIcon.SIZE) / 2, y + (s - NavIcon.SIZE) / 2, Color.WHITE);
            g2.dispose();
        }
    }

    // Rounded card that hides its children's square corners
    private static class RoundedCard extends JPanel {
        private final Color fill, mask;
        private final int radius;

        RoundedCard(LayoutManager lm, Color fill, Color mask, int radius) {
            super(lm);
            this.fill = fill;
            this.mask = mask;
            this.radius = radius;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius * 2, radius * 2));
            g2.dispose();
        }

        @Override protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Area corners = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
            corners.subtract(new Area(new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(), radius * 2, radius * 2)));
            g2.setColor(mask);
            g2.fill(corners);
            g2.dispose();
        }
    }
}