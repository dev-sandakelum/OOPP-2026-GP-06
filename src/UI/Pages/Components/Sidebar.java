package UI.Pages.Components;

import UI.Kit.Assets;
import UI.Kit.Backdrop;
import UI.Kit.FrameData;
import UI.Kit.Glass;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import static UI.Kit.Palette.*;

// Reusable frosted-glass sidebar: logo + role label + nav items + (optional photo) + account card.
// Drop it into BorderLayout.WEST of any page.
public class Sidebar extends JPanel implements FrameData {

    private static final int RADIUS = RADIUS_PANEL;
    private static final Insets GAP = new Insets(12, 12, 12, 6);   // gaps around the glass card

    private final User user;
    private final List<NavItem> items = new ArrayList<>();
    private int selectedIndex = -1;
    private IntConsumer onSelect = i -> { };
    private Runnable onLogout = () -> { };

    public Sidebar(User user, String roleLabel, String[] tabs, NavIcon[] icons, Color pageBg) {
        super(new BorderLayout());
        this.user = user;
        setOpaque(true);
        setBackground(pageBg);
        setBorder(new EmptyBorder(GAP));
        setPreferredSize(new Dimension(sp_width + 18, 0));

        GlassCard card = new GlassCard(items);
        card.add(logoSection(), BorderLayout.NORTH);
        card.add(navSection(roleLabel, tabs, icons), BorderLayout.CENTER);
        card.add(accountSection(roleLabel), BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        setSelectedIndex(0, false);
    }

    // ---------- Public API ----------

    // index of the selected nav item (0-based, same order as the tabs array)
    public int getSelectedIndex() { return selectedIndex; }

    public User getUser() { return user; }

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

    // ---------- Painting ----------
    @Override
    protected void paintComponent(Graphics g) {
        Backdrop.paint(g, this);
        Graphics2D g2 = Glass.aa(g);
        Insets in = getInsets();
        Glass.shadow(g2, in.left, in.top,
                getWidth() - in.left - in.right, getHeight() - in.top - in.bottom, RADIUS);
        g2.dispose();
    }

    // ---------- Logo ----------
    private JPanel logoSection() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(22, 22, 6, 18));

        JComponent badge = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = Glass.aa(g);
                int s = Math.min(getWidth(), getHeight());
                BufferedImage logo = Assets.image(Assets.APP_ICON);
                if (logo != null) {
                    // app logo on a white rounded tile (the PNG has an off-white background)
                    RoundRectangle2D tile = new RoundRectangle2D.Float(0, 0, s, s, 16, 16);
                    g2.setColor(SURFACE);
                    g2.fill(tile);
                    Shape old = g2.getClip();
                    g2.clip(tile);
                    int inset = Math.round(s * 0.08f);
                    g2.drawImage(logo, inset, inset, s - inset * 2, s - inset * 2, null);
                    g2.setClip(old);
                    g2.setColor(LINE_2);
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, s - 1, s - 1, 16, 16));
                } else {
                    Glass.brand(g2, 0, 0, s, s, 8);
                    g2.setColor(ON_BRAND);
                    g2.setFont(font(Font.BOLD, 15));
                    FontMetrics fm = g2.getFontMetrics();
                    String txt = "UoR";
                    g2.drawString(txt, (s - fm.stringWidth(txt)) / 2,
                            (s - fm.getHeight()) / 2 + fm.getAscent());
                }
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(54, 54));
        badge.setMinimumSize(badge.getPreferredSize());
        badge.setMaximumSize(badge.getPreferredSize());

        JLabel title = new JLabel("AFMS");
        title.setFont(font(Font.BOLD, 22));
        title.setForeground(INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = tracked("FACULTY OF TECH.", 10f, LABEL_COL);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(title);
        text.add(sub);
        text.add(Box.createVerticalGlue());

        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        row.add(badge, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);

        p.add(row);
        p.add(Box.createVerticalStrut(10));
        p.add(new AccentBar(64, 3));
        return p;
    }

    // ---------- Nav ----------
    private JPanel navSection(String roleLabel, String[] tabs, NavIcon[] icons) {
        JPanel np = new JPanel();
        np.setLayout(new BoxLayout(np, BoxLayout.Y_AXIS));
        np.setOpaque(false);
        np.setBorder(new EmptyBorder(10, 16, 14, 16));

        JLabel role = tracked(roleLabel, 11f, LABEL_COL);
        role.setBorder(new EmptyBorder(8, 8, 0, 0));
        np.add(role);
        np.add(Box.createVerticalStrut(10));

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
    private JPanel accountSection(String fallbackSub) {
        String name = (user != null && user.getName() != null) ? user.getName() : "Unknown user";
        String sub  = (user != null && user.getEmail() != null && !user.getEmail().isBlank())
                ? user.getEmail() : fallbackSub;

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 14, 14, 14));

        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = Glass.aa(g);
                float w = getWidth() - 4, h = getHeight() - 6;
                Glass.shadow(g2, 2, 1, w, h, 20);
                Glass.panel(g2, 2, 1, w, h, 20, GLASS_STRONG);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 14, 14));
        card.setPreferredSize(new Dimension(0, 84));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(font(Font.BOLD, 14));
        nameLbl.setForeground(INK);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(font(Font.PLAIN, 12));
        subLbl.setForeground(INK_2);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(nameLbl);
        text.add(subLbl);
        text.add(Box.createVerticalGlue());

        JPanel avatarWrap = new JPanel(new GridBagLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.add(new Avatar(name, user != null ? user.getPhoto() : null, 46));

        JPanel logoutWrap = new JPanel(new GridBagLayout());
        logoutWrap.setOpaque(false);
        logoutWrap.add(new LogoutButton(() -> onLogout.run()));

        card.add(avatarWrap, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        card.add(logoutWrap, BorderLayout.EAST);

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
        a.put(TextAttribute.TRACKING, 0.14f);
        l.setFont(new Font(a));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ---------- Inner components ----------

    // White rounded box with a blue logout icon.
    private static class LogoutButton extends JComponent {
        private boolean hover;
        LogoutButton(Runnable action) {
            setPreferredSize(new Dimension(38, 38));
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
            Graphics2D g2 = Glass.aa(g);
            int s = Math.min(getWidth(), getHeight());
            int y = (getHeight() - s) / 2;
            g2.setColor(hover ? BRAND_SOFT : SURFACE);
            g2.fill(new RoundRectangle2D.Float(0, y, s, s, 12, 12));
            g2.setColor(LINE_2);
            g2.draw(new RoundRectangle2D.Float(0.5f, y + 0.5f, s - 1, s - 1, 12, 12));
            NavIcon.LOGOUT.draw(g2, (s - NavIcon.SIZE) / 2, y + (s - NavIcon.SIZE) / 2, BRAND);
            g2.dispose();
        }
    }

    // Frosted glass card. Paints the faculty photo (if present) in the free space below the nav,
    // fading in from transparent so it never sits behind the nav labels.
    private static class GlassCard extends JPanel {
        private static final int FADE_ABOVE = 50;   // fade starts this far above the last nav item
        private static final int FADE_LEN   = 110;  // and is fully opaque this far below the start

        private final List<NavItem> navItems;
        private BufferedImage layer;        // photo with the alpha mask applied, cached per size
        private int layerW, layerH, layerFade;

        GlassCard(List<NavItem> navItems) {
            super(new BorderLayout());
            this.navItems = navItems;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = Glass.aa(g);
            int w = getWidth(), h = getHeight();
            Glass.panel(g2, 0, 0, w, h, RADIUS, GLASS);
            g2.clip(new RoundRectangle2D.Float(0, 0, w, h, RADIUS * 2, RADIUS * 2));

            BufferedImage photo = Assets.image(Assets.SIDEBAR_PHOTO);
            if (photo != null) {
                BufferedImage img = maskedPhoto(photo, w, h, navBottom());
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
                g2.drawImage(img, 0, h - img.getHeight(), null);
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setColor(GLASS_LINE);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, RADIUS * 2, RADIUS * 2));
            } else {
                // fallback: soft blue wash at the bottom (replace by adding assets/sidebar_building.png)
                int top = Math.round(h * 0.50f);
                g2.setPaint(new GradientPaint(0, top, Glass.alpha(BRAND_LT, 0),
                                              0, h, Glass.alpha(BRAND_LT, 55)));
                g2.fillRect(0, top, w, h - top);
            }
            g2.dispose();
        }

        // bottom edge of the lowest nav item, in this card's coordinates
        private int navBottom() {
            int bottom = 0;
            for (NavItem it : navItems) {
                if (!it.isShowing()) continue;
                bottom = Math.max(bottom, SwingUtilities.convertPoint(it, 0, it.getHeight(), this).y);
            }
            return bottom;
        }

        // Photo fitted to the card width, anchored at the bottom, with its top faded out.
        private BufferedImage maskedPhoto(BufferedImage photo, int w, int h, int navBottom) {
            float scale = w / (float) photo.getWidth();
            int dw = w, dh = Math.min(h, Math.round(photo.getHeight() * scale));
            int imgTop = h - dh;
            int fadeStart = Math.max(0, navBottom - FADE_ABOVE - imgTop);
            if (layer != null && layerW == w && layerH == dh && layerFade == fadeStart) return layer;

            BufferedImage out = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_ARGB);
            Graphics2D lg = Glass.aa(out.getGraphics());
            lg.drawImage(photo, 0, 0, dw, dh, null);
            lg.setComposite(AlphaComposite.DstIn);
            lg.setPaint(new GradientPaint(0, fadeStart, new Color(0, 0, 0, 0),
                                          0, fadeStart + FADE_LEN, new Color(0, 0, 0, 255)));
            lg.fillRect(0, 0, dw, dh);
            lg.dispose();

            layer = out;
            layerW = w;
            layerH = dh;
            layerFade = fadeStart;
            return out;
        }
    }
}
