package UI.Pages.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static UI.Kit.Palette.*;

// KPI tile: label across the top, big number + caption below, icon badge beside the number.
// dark = true gives the royal-blue "hero" tile (first card in a row).
public class KpiCard extends Card {

    public KpiCard(String label, String value, String sub, NavIcon icon, boolean dark) {
        super(dark);
        setLayout(new BorderLayout(0, 6));
        setBorder(new EmptyBorder(18, 22, 20, 20));
        setPreferredSize(new Dimension(0, 136));

        Color main  = dark ? ON_BRAND   : INK;
        Color muted = dark ? ON_BRAND_2 : INK_2;

        JLabel l = new JLabel(label);
        l.setFont(font(Font.BOLD, 15));
        l.setForeground(main);

        JLabel v = new JLabel(value);
        v.setFont(font(Font.BOLD, 38));
        v.setForeground(main);

        JLabel s = new JLabel(sub);
        s.setFont(font(Font.PLAIN, 13));
        s.setForeground(muted);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(v);
        text.add(s);

        JPanel lower = new JPanel(new BorderLayout(10, 0));
        lower.setOpaque(false);
        lower.add(text, BorderLayout.CENTER);
        if (icon != null) {
            JPanel badge = new JPanel(new GridBagLayout());
            badge.setOpaque(false);
            badge.add(new IconBadge(icon, dark ? IconBadge.Style.ON_DARK : IconBadge.Style.SOFT, 56));
            lower.add(badge, BorderLayout.EAST);
        }

        add(l, BorderLayout.NORTH);
        add(lower, BorderLayout.CENTER);
    }
}
