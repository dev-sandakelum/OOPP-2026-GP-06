package UI.Kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.geom.Ellipse2D;

import static UI.Kit.Palette.*;

// Installs the AFMS look into UIManager so the stock Swing widgets the pages do not paint
// themselves (combo boxes, JOptionPane dialogs, tool tips) match the glass theme.
// Call once from Main before any window is created.
public final class Theme {
    private Theme() { }

    private static boolean installed;

    public static void install() {
        if (installed) return;
        installed = true;

        // ---------- Combo boxes (every JComboBox in the app) ----------
        UIManager.put("ComboBoxUI", GlassComboBoxUI.class.getName());
        UIManager.put("ComboBox.font", new FontUIResource(font(Font.PLAIN, 13)));
        UIManager.put("ComboBox.foreground", new ColorUIResource(INK));
        UIManager.put("ComboBox.background", new ColorUIResource(SURFACE));
        UIManager.put("ComboBox.disabledForeground", new ColorUIResource(INK_2));
        UIManager.put("ComboBox.disabledBackground", new ColorUIResource(SURFACE_2));
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(INK));
        UIManager.put("ComboBox.selectionBackground", new ColorUIResource(BRAND_SOFT));
        UIManager.put("ComboBox.border", new BorderUIResource(new EmptyBorder(2, 6, 2, 2)));
        UIManager.put("ComboBox.padding", new Insets(0, 0, 0, 0));

        // ---------- Option panes (Ui.info / confirm / form and plain JOptionPane calls) ----------
        UIManager.put("OptionPaneUI", GlassOptionPaneUI.class.getName());
        UIManager.put("OptionPane.background", new ColorUIResource(BG_1));
        UIManager.put("OptionPane.foreground", new ColorUIResource(INK));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(INK));
        UIManager.put("OptionPane.messageFont", new FontUIResource(font(Font.PLAIN, 14)));
        UIManager.put("OptionPane.buttonFont", new FontUIResource(font(Font.BOLD, 13)));
        UIManager.put("OptionPane.font", new FontUIResource(font(Font.PLAIN, 14)));
        UIManager.put("OptionPane.border", new BorderUIResource(new EmptyBorder(20, 24, 18, 24)));
        UIManager.put("OptionPane.messageAreaBorder", new BorderUIResource(new EmptyBorder(0, 0, 0, 0)));
        UIManager.put("OptionPane.buttonAreaBorder", new BorderUIResource(new EmptyBorder(16, 0, 0, 0)));
        UIManager.put("OptionPane.buttonPadding", 8);
        UIManager.put("OptionPane.sameSizeButtons", Boolean.TRUE);
        UIManager.put("OptionPane.buttonOrientation", SwingConstants.RIGHT);
        UIManager.put("OptionPane.minimumSize", new Dimension(320, 120));
        UIManager.put("OptionPane.informationIcon", new BadgeIcon("i", BRAND));
        UIManager.put("OptionPane.questionIcon",    new BadgeIcon("?", BRAND));
        UIManager.put("OptionPane.warningIcon",     new BadgeIcon("!", WARN));
        UIManager.put("OptionPane.errorIcon",       new BadgeIcon("!", BAD));

        // ---------- Tool tips ----------
        UIManager.put("ToolTip.font", new FontUIResource(font(Font.PLAIN, 12)));
        UIManager.put("ToolTip.foreground", new ColorUIResource(INK));
        UIManager.put("ToolTip.background", new ColorUIResource(SURFACE));
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_2, 1), new EmptyBorder(6, 10, 6, 10))));
    }

    // Round coloured badge with a single glyph, used as the dialog icon.
    static final class BadgeIcon implements Icon {
        private static final int SIZE = 40;
        private final String glyph;
        private final Color color;

        BadgeIcon(String glyph, Color color) {
            this.glyph = glyph;
            this.color = color;
        }

        @Override public int getIconWidth()  { return SIZE; }
        @Override public int getIconHeight() { return SIZE; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = Glass.aa(g);
            g2.setColor(Glass.alpha(color, 36));
            g2.fill(new Ellipse2D.Float(x, y, SIZE, SIZE));
            g2.setColor(Glass.alpha(color, 90));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new Ellipse2D.Float(x + 0.5f, y + 0.5f, SIZE - 1, SIZE - 1));
            g2.setColor(color);
            g2.setFont(font(Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (SIZE - fm.stringWidth(glyph)) / 2;
            int ty = y + (SIZE - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(glyph, tx, ty);
            g2.dispose();
        }
    }
}
