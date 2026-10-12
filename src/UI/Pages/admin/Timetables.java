package UI.Pages.admin;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import UI.Pages.Components.TimetableCell;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Admin "Timetables" tab: weekly grid (5 days x 4 slots). Click a cell to edit it.
public class Timetables extends Page {

    private static final String[] DAYS  = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final String[] SLOTS = {"8:00 - 10:00", "10:00 - 12:00", "1:00 - 3:00", "3:00 - 5:00"};

    // ---------- Placeholder data (swap for services later) ----------
    // [slot][day] = {course code, room}; empty code = free slot
    private final String[][][] grid = new String[SLOTS.length][DAYS.length][];
    // ---------- end placeholder data ----------

    private final JPanel gridPanel = new JPanel(new GridBagLayout());

    public Timetables() {
        super("Timetables", "Click a slot to assign or clear a course", null);

        for (String[][] row : grid) {
            for (int d = 0; d < row.length; d++) row[d] = new String[]{"", ""};
        }
        grid[0][0] = new String[]{"TICT1113", "Lab 1"};
        grid[1][0] = new String[]{"TICT1142", "Hall A"};
        grid[0][1] = new String[]{"TICT1123", "Lab 2"};
        grid[2][1] = new String[]{"TICT1152", "Hall B"};
        grid[1][2] = new String[]{"TICT1133", "Lab 1"};
        grid[3][2] = new String[]{"TICT1162", "Hall A"};
        grid[0][3] = new String[]{"TICT1113", "Hall A"};
        grid[2][4] = new String[]{"TICT1123", "Lab 2"};

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

        // top-left corner and day headers
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

        // one row per time slot
        for (int s = 0; s < SLOTS.length; s++) {
            gc.gridx = 0; gc.gridy = s + 1; gc.weightx = 0; gc.weighty = 1;
            JLabel l = new JLabel(SLOTS[s]);
            l.setFont(font(Font.BOLD, 13));
            l.setForeground(INK_2);
            gridPanel.add(l, gc);

            for (int d = 0; d < DAYS.length; d++) {
                final int slot = s, day = d;
                gc.gridx = d + 1; gc.weightx = 1;
                gridPanel.add(new TimetableCell(grid[s][d][0], grid[s][d][1], () -> edit(slot, day)), gc);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void edit(int slot, int day) {
        String[] labels = {"Course code (leave empty to clear)", "Room"};
        String[] v = form(this, DAYS[day] + ", " + SLOTS[slot], labels, grid[slot][day]);
        if (v == null) return;
        grid[slot][day] = v[0].isEmpty() ? new String[]{"", ""} : v;
        rebuild();
    }
}
