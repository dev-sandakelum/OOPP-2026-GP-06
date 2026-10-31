package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;

import static UI.Kit.Palette.*;

// Rounded glass card with a soft shadow. Frosted white, or royal-blue gradient when dark = true.
public class Card extends JPanel {

    // room kept inside the bounds for the shadow
    public static final int INSET_X = 2, INSET_TOP = 1, INSET_BOTTOM = 7;

    private final boolean dark;

    public Card(boolean dark) {
        this.dark = dark;
        setOpaque(false);
    }

    public boolean isDark() { return dark; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        float x = INSET_X, y = INSET_TOP;
        float w = getWidth() - INSET_X * 2, h = getHeight() - INSET_TOP - INSET_BOTTOM;
        Glass.shadow(g2, x, y, w, h, RADIUS_CARD);
        if (dark) Glass.brand(g2, x, y, w, h, RADIUS_CARD);
        else      Glass.panel(g2, x, y, w, h, RADIUS_CARD, GLASS);
        g2.dispose();
    }
}
