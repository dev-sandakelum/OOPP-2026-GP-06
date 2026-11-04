package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

// Small painted dot / square used in place of the "●" / "■" characters,
// which the app font (Rubik) cannot draw (they showed up as boxes).
// Use it as a JLabel icon: label.setIcon(ShapeIcon.dot(OK, 7)).
public class ShapeIcon implements Icon {

    private final Color color;
    private final int size;
    private final boolean round;

    private ShapeIcon(Color color, int size, boolean round) {
        this.color = color;
        this.size = size;
        this.round = round;
    }

    /** Filled circle. */
    public static ShapeIcon dot(Color color, int size)    { return new ShapeIcon(color, size, true); }

    /** Filled square with slightly rounded corners. */
    public static ShapeIcon square(Color color, int size) { return new ShapeIcon(color, size, false); }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = Glass.aa(g);
        // follow the label's text colour when no colour was given
        g2.setColor(color != null ? color : c.getForeground());
        if (round) g2.fill(new Ellipse2D.Float(x, y, size, size));
        else       g2.fill(new RoundRectangle2D.Float(x, y, size, size, 3, 3));
        g2.dispose();
    }

    @Override public int getIconWidth()  { return size; }
    @Override public int getIconHeight() { return size; }
}
