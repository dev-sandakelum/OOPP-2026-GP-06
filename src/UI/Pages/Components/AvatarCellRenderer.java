package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.IntFunction;

import static UI.Kit.Palette.*;

// "Name" cell that shows a small round profile photo (or initials) in front of the text.
// photoOfModelRow maps a model row index to the user's photo filename (may return null).
public class AvatarCellRenderer extends JLabel implements TableCellRenderer {

    private static final int SIZE = 30, PAD_LEFT = 12, GAP = 10;

    private final IntFunction<String> photoOfModelRow;
    private final Color background;
    private BufferedImage photo;
    private String initials = "?";
    private boolean selected;

    /** background: colour of an unselected cell (null = transparent, for glass cards). */
    public AvatarCellRenderer(IntFunction<String> photoOfModelRow, Color background) {
        this.photoOfModelRow = photoOfModelRow;
        this.background = background;
        setOpaque(false);
        setFont(font(Font.BOLD, 14));
        setForeground(INK);
        setBorder(BorderFactory.createEmptyBorder(0, PAD_LEFT + SIZE + GAP, 0, 12));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean selected, boolean focus, int row, int col) {
        String name = value == null ? "" : value.toString();
        setText(name);
        initials = Avatar.initialsOf(name);
        photo = Avatar.photo(photoOfModelRow.apply(table.convertRowIndexToModel(row)));
        this.selected = selected;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        Color bg = selected ? BRAND_SOFT : background;
        if (bg != null) {
            g2.setColor(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        Avatar.paint(g2, photo, initials, PAD_LEFT, (getHeight() - SIZE) / 2f, SIZE);
        g2.dispose();
        super.paintComponent(g);   // the name text, offset by the border
    }
}
