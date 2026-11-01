package UI.Pages.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static UI.Kit.Palette.*;

// Glass card with a header row (blue icon badge + title + accent underline) and content below.
public class SectionCard extends Card {

    public SectionCard(String title, NavIcon icon, JComponent content) {
        super(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(20, 24, 22, 24));

        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 18));
        t.setForeground(INK);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(t);
        text.add(Box.createVerticalStrut(5));
        text.add(new AccentBar(28, 3));
        text.add(Box.createVerticalGlue());

        JPanel head = new JPanel(new BorderLayout(14, 0));
        head.setOpaque(false);
        if (icon != null) {
            JPanel badge = new JPanel(new GridBagLayout());
            badge.setOpaque(false);
            badge.add(new IconBadge(icon, IconBadge.Style.SOLID, 44));
            head.add(badge, BorderLayout.WEST);
        }
        head.add(text, BorderLayout.CENTER);

        // keep the content at the top of the card
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(content, BorderLayout.NORTH);

        add(head, BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
    }
}
