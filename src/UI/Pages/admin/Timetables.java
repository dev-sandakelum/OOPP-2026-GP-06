package UI.Pages.admin;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import UI.Pages.Components.TimetableCell;
import data.TimetableData;
import data.TimetableEntityData;
import data.CourseData;
import model.Timetable;
import model.TimetableEntity;
import model.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

/**
 * Admin "Timetables" tab: weekly grid (5 days × 4 slots). Click a cell to edit it.
 */
public class Timetables extends Page {

    private static final String[] DAYS  = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final String[] SLOTS = {"8:00 - 10:00", "10:00 - 12:00", "1:00 - 3:00", "3:00 - 5:00"};

    private final TimetableData timetableData = new TimetableData();
    private final TimetableEntityData entityData = new TimetableEntityData();
    private final CourseData courseData = new CourseData();

    // grid[slot][day] = {course_code, room};  entity_id is tracked separately
    private final String[][][] grid = new String[SLOTS.length][DAYS.length][];
    private final int[][] entityIds = new int[SLOTS.length][DAYS.length]; // Track entity IDs

    private final JPanel gridPanel = new JPanel(new GridBagLayout());
    private int currentTimetableId = -1;

    public Timetables() {
        super("Timetables", "Click a slot to assign or clear a course", null);

        // initialise all cells to empty
        for (int s = 0; s < SLOTS.length; s++) {
            for (int d = 0; d < DAYS.length; d++) {
                grid[s][d] = new String[]{"", ""};
                entityIds[s][d] = -1;
            }
        }

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        gridPanel.setOpaque(false);
        card.add(gridPanel, BorderLayout.CENTER);
        setScrollBody(card);

        loadTimetable();
        rebuild();
    }

    // -----------------------------------------------------------------
    //  Data loading
    // -----------------------------------------------------------------

    private void loadTimetable() {
        try {
            // Get the most recent active timetable
            List<Timetable> timetables = timetableData.findByStatus("ACTIVE");
            if (timetables.isEmpty()) {
                timetables = timetableData.findAll();
            }
            
            if (!timetables.isEmpty()) {
                currentTimetableId = timetables.get(0).getTimetable_id();
                
                // Load all entities for this timetable
                List<TimetableEntity> entities = entityData.findByTimetable(currentTimetableId);
                
                // Build course code map
                Map<Integer, String> courseMap = new HashMap<>();
                for (Course c : courseData.findAll()) {
                    courseMap.put(c.getCourse_id(), c.getCourse_code());
                }
                
                for (TimetableEntity entity : entities) {
                    int dayIndex = dayToIndex(entity.getDay());
                    int slotIndex = timeToSlotIndex(entity.getStart_time(), entity.getEnd_time());
                    
                    if (dayIndex >= 0 && dayIndex < DAYS.length && 
                        slotIndex >= 0 && slotIndex < SLOTS.length) {
                        
                        // Get course code from offering if available
                        String courseCode = "";
                        if (entity.getOffering_id() != null) {
                            // In a full implementation, look up the course from offering
                            courseCode = "COURSE" + entity.getOffering_id();
                        }
                        
                        grid[slotIndex][dayIndex] = new String[]{
                            courseCode,
                            entity.getLecture_hall() != null ? entity.getLecture_hall() : ""
                        };
                        entityIds[slotIndex][dayIndex] = entity.getTimetable_entity_id();
                    }
                }
            }
        } catch (Exception ex) {
            info(this, "Could not load timetable: " + ex.getMessage());
        }
    }

    private int dayToIndex(String day) {
        if (day == null) return -1;
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equalsIgnoreCase(day)) return i;
        }
        return -1;
    }

    private int timeToSlotIndex(Time start, Time end) {
        if (start == null) return -1;
        String hour = start.toString().substring(0, 2);
        return switch (hour) {
            case "08" -> 0;
            case "10" -> 1;
            case "13" -> 2;
            case "15" -> 3;
            default -> -1;
        };
    }

    private Time[] slotToTimes(int slot) {
        return switch (slot) {
            case 0 -> new Time[]{Time.valueOf("08:00:00"), Time.valueOf("10:00:00")};
            case 1 -> new Time[]{Time.valueOf("10:00:00"), Time.valueOf("12:00:00")};
            case 2 -> new Time[]{Time.valueOf("13:00:00"), Time.valueOf("15:00:00")};
            case 3 -> new Time[]{Time.valueOf("15:00:00"), Time.valueOf("17:00:00")};
            default -> new Time[]{Time.valueOf("08:00:00"), Time.valueOf("10:00:00")};
        };
    }

    // -----------------------------------------------------------------
    //  Grid rendering
    // -----------------------------------------------------------------

    private void rebuild() {
        gridPanel.removeAll();
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.BOTH;
        gc.insets = new Insets(5, 5, 5, 5);

        // top-left corner spacer + day headers
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
                gridPanel.add(
                        new TimetableCell(grid[s][d][0], grid[s][d][1],
                                () -> edit(slot, day)), gc);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // -----------------------------------------------------------------
    //  Edit a slot
    // -----------------------------------------------------------------

    private void edit(int slot, int day) {
        String[] labels = {"Course code (leave empty to clear)", "Room"};
        String[] v = form(this, DAYS[day] + ", " + SLOTS[slot], labels, grid[slot][day]);
        if (v == null) return;

        try {
            if (currentTimetableId == -1) {
                // Create a default timetable if none exists
                Timetable t = new Timetable();
                t.setAcademic_year("2026");
                t.setPublished_date(new java.sql.Date(System.currentTimeMillis()));
                t.setStatus("ACTIVE");
                timetableData.insert(t);
                currentTimetableId = t.getTimetable_id();
            }

            if (v[0].isEmpty()) {
                // Clear the slot - delete entity if it exists
                if (entityIds[slot][day] > 0) {
                    entityData.delete(entityIds[slot][day]);
                    entityIds[slot][day] = -1;
                }
                grid[slot][day] = new String[]{"", ""};
            } else {
                // Update or create entity
                TimetableEntity entity = new TimetableEntity();
                entity.setTimetable_id(currentTimetableId);
                entity.setOffering_id(null); // In full implementation, look up offering by course code
                entity.setDay(DAYS[day]);
                Time[] times = slotToTimes(slot);
                entity.setStart_time(times[0]);
                entity.setEnd_time(times[1]);
                entity.setLecture_hall(v[1]);
                entity.setSession_type("LECTURE");

                if (entityIds[slot][day] > 0) {
                    // Update existing
                    entity.setTimetable_entity_id(entityIds[slot][day]);
                    entityData.update(entity);
                } else {
                    // Insert new
                    entityData.insert(entity);
                    entityIds[slot][day] = entity.getTimetable_entity_id();
                }
                
                grid[slot][day] = v;
            }
            rebuild();
        } catch (Exception ex) {
            info(this, "Could not save slot: " + ex.getMessage());
        }
    }
}
