package UI.Pages.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static UI.Kit.Palette.*;

// Page frame: title + accent underline + subtitle (+ optional action) on top, body below.
// Extend it for every tab, then call setBody(...) or setScrollBody(...).
public class Page extends JPanel {

    public Page(String title, String sub, JComponent action) {
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
        text.add(new AccentBar(56, 4));
        text.add(Box.createVerticalStrut(10));
        text.add(s);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(26, 28, 18, 28));
        head.add(text, BorderLayout.WEST);
        if (action != null) {
            JPanel a = new JPanel(new GridBagLayout());
            a.setOpaque(false);
            a.add(action);
            head.add(a, BorderLayout.EAST);
        }
        add(head, BorderLayout.NORTH);
    }

    // fixed body that fills the space (tables)
    public void setBody(JComponent body) {
        JPanel pad = new JPanel(new BorderLayout());
        pad.setOpaque(false);
        pad.setBorder(new EmptyBorder(0, 28, 24, 28));
        pad.add(body, BorderLayout.CENTER);
        add(pad, BorderLayout.CENTER);
    }

    // body that scrolls and follows the window width (cards, forms)
    public void setScrollBody(JComponent body) {
        ScrollBody sb = new ScrollBody();
        sb.setBorder(new EmptyBorder(0, 28, 24, 28));
        sb.add(body, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(sb);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        GlassScrollBarUI.install(sp);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp, BorderLayout.CENTER);
    }
}
