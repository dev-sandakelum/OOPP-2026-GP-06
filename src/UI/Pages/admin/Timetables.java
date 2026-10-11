package UI.Pages.admin;

import UI.Pages.admin.AdminUi.Card;
import UI.Pages.admin.AdminUi.Page;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import static UI.Pages.admin.AdminUi.*;

// Admin "Timetables" tab: weekly grid (5 days x 4 slots). Click a cell to edit it.
public class Timetables extends Page {

    private static final String[] DAYS  = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final String[] SLOTS = {"8:00 - 10:00", "10:00 - 12:00", "1:00 - 3:00", "3:00 - 5:00"};

    // ---------- Placeholder data (swap for MockData / services later) ----------
    // [slot][day] = {course code, room}; empty code = free slot
    private final String[][][] grid = new String[SLOTS.length][DAYS.length][];
    {
        for (String[][] row : grid) for (int d = 0; d < row.length; d++) row[d] = new String[]{"", ""};
        grid[0][0] = new String[]{"TICT1113", "Lab 1"};
        grid[1][0] = new String[]{"TICT1142", "Hall A"};
        grid[0][1] = new String[]{"TICT1123", "Lab 2"};
        grid[2][1] = new String[]{"TICT1152", "Hall B"};
        grid[1][2] = new String[]{"TICT1133", "Lab 1"};
        grid[3][2] = new String[]{"TICT1162", "Hall A"};
        grid[0][3] = new String[]{"TICT1113", "Hall A"};
        grid[2][4] = new String[]{"TICT1123", "Lab 2"};
    }
    // ---------- end placeholder data ----------

    private final JPanel gridPanel = new JPanel(new GridBagLayout());

    public Timetables() {
        super("Timetables", "Click a slot to assign or clear a course", null);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        gridPanel.setOpaque(false);
        card.add(gridPanel, BorderLayout.CENTER);

        setScrollBody(card);
        rebuild();
    }

    private void rebuild() {
        gridPanel.removeAll();
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(5, 5, 5, 5);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; gc.weighty = 0;
        gridPanel.add(Box.createHorizontalStrut(110), gc);
        for (int d = 0; d < DAYS.length; d++) {
            gc.gridx = d + 1; gc.gridy = 0; gc.weightx = 1;
            JLabel l = new JLabel(DAYS[d], SwingConstants.CENTER);
            l.setFont(font(Font.BOLD, 13));
            l.setForeground(INK_2);
            l.setPreferredSize(new Dimension(100, 30));
            gridPanel.add(l, gc);
        }
        for (int s = 0; s < SLOTS.length; s++) {
            gc.gridx = 0; gc.gridy = s + 1; gc.weightx = 0; gc.weighty = 1;
            JLabel l = new JLabel(SLOTS[s]);
            l.setFont(font(Font.BOLD, 13));
            l.setForeground(INK_2);
            gridPanel.add(l, gc);
            for (int d = 0; d < DAYS.length; d++) {
                gc.gridx = d + 1; gc.weightx = 1;
                gridPanel.add(new Cell(s, d), gc);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void edit(int s, int d) {
        String[] cur = grid[s][d];
        String[] v = form(this, DAYS[d] + ", " + SLOTS[s],
                new String[]{"Course code (leave empty to clear)", "Room"}, cur);
        if (v == null) return;
        grid[s][d] = v[0].isEmpty() ? new String[]{"", ""} : v;
        rebuild();
    }

    // one clickable slot
    private class Cell extends JComponent {
        private final int s, d;
        private boolean hover;

        Cell(int s, int d) {
            this.s = s;
            this.d = d;
            setPreferredSize(new Dimension(100, 76));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { edit(Cell.this.s, Cell.this.d); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            String[] data = grid[s][d];
            boolean filled = !data[0].isEmpty();
            Shape shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(filled ? (hover ? new Color(206, 228, 215) : BRAND_SOFT) : (hover ? LINE : SURFACE_2));
            g2.fill(shape);
            if (!filled) {
                g2.setColor(LINE_2);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
                g2.draw(shape);
            }
            FontMetrics fm;
            if (filled) {
                g2.setFont(font(Font.BOLD, 14));
                fm = g2.getFontMetrics();
                g2.setColor(BRAND);
                g2.drawString(data[0], (getWidth() - fm.stringWidth(data[0])) / 2, getHeight() / 2 - 2);
                g2.setFont(font(Font.PLAIN, 12));
                fm = g2.getFontMetrics();
                g2.setColor(INK_2);
                g2.drawString(data[1], (getWidth() - fm.stringWidth(data[1])) / 2, getHeight() / 2 + 16);
            } else {
                g2.setFont(font(Font.PLAIN, 12));
                fm = g2.getFontMetrics();
                g2.setColor(INK_2);
                String t = "Free";
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() + fm.getAscent()) / 2 - 2);
            }
            g2.dispose();
        }
    }
}
