
package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;

import data.EnrollmentData;
import data.TimetableData;
import data.TimetableEntityData;
import data.CourseOfferingData;
import data.CourseData;
import data.LecturerData;

import model.User;
import model.Enrollment;
import model.TimetableEntity;
import model.CourseOffering;
import model.Course;
import model.Lecturer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static UI.Kit.Palette.*;

public class Timetable extends Page {

    private static final String[] DAYS = {
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"
    };

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final JPanel gridPanel = new JPanel(new GridBagLayout());

    private final Map<String, Map<TimeSlot, List<CourseEntry>>> timetableGrid =
            new LinkedHashMap<>();

    private final TreeSet<TimeSlot> timeSlots = new TreeSet<>();

    private String errorMessage = null;

    public Timetable(User user) {

        super("Timetable",
                "Your published weekly timetable — enrolled courses only.",
                null);

        for (String day : DAYS) {
            timetableGrid.put(day, new TreeMap<>());
        }

        loadTimetable(user);

        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);

        container.add(createLegend(), BorderLayout.NORTH);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        gridPanel.setOpaque(false);
        card.add(gridPanel, BorderLayout.CENTER);

        container.add(card, BorderLayout.CENTER);

        setScrollBody(container);

        rebuild();
    }

    // Load timetable data belonging to the logged-in student
    private void loadTimetable(User user) {

        try {
            int studentId = user.getUser_id();

            EnrollmentData enrollmentData = new EnrollmentData();
            CourseOfferingData offeringData = new CourseOfferingData();
            TimetableData timetableData = new TimetableData();
            TimetableEntityData entityData = new TimetableEntityData();
            CourseData courseData = new CourseData();
            LecturerData lecturerData = new LecturerData();

            // 1. Get student's enrolled offerings
            Set<Integer> enrolledOfferingIds = new HashSet<>();

            List<Enrollment> enrollments =
                    enrollmentData.findByStudent(studentId);

            for (Enrollment enrollment : enrollments) {

                if ("ENROLLED".equalsIgnoreCase(
                        enrollment.getEnrollment_status())) {

                    enrolledOfferingIds.add(enrollment.getOffering_id());
                }
            }

            if (enrolledOfferingIds.isEmpty()) {
                return;
            }

            // Cache course and lecturer information
            Map<Integer, Course> courseCache = new HashMap<>();
            Map<Integer, Lecturer> lecturerCache = new HashMap<>();

            // 2. Get published timetables
            List<model.Timetable> publishedTimetables =
                    timetableData.findByStatus("PUBLISHED");

            // 3. Process timetable entries
            for (model.Timetable timetable : publishedTimetables) {

                List<TimetableEntity> entities =
                        entityData.findByTimetable(
                                timetable.getTimetable_id()
                        );

                for (TimetableEntity entity : entities) {

                    Integer offeringId = entity.getOffering_id();

                    // Ignore entries not belonging to enrolled courses
                    if (offeringId == null ||
                            !enrolledOfferingIds.contains(offeringId)) {
                        continue;
                    }

                    String day = entity.getDay();

                    if (day == null) {
                        continue;
                    }

                    day = day.trim().toUpperCase();

                    if (!timetableGrid.containsKey(day)) {
                        continue;
                    }

                    // 4. Get course offering
                    CourseOffering offering =
                            offeringData.findById(offeringId);

                    if (offering == null) {
                        continue;
                    }

                    // 5. Get course details
                    int courseId = offering.getCourse_id();

                    Course course = courseCache.get(courseId);

                    if (course == null) {
                        course = courseData.findById(courseId);

                        if (course != null) {
                            courseCache.put(courseId, course);
                        }
                    }

                    if (course == null) {
                        continue;
                    }

                    // 6. Get lecturer name
                    String lecturerName = "Lecturer not assigned";

                    Integer lecturerId = offering.getLecturer_id();

                    if (lecturerId != null) {

                        Lecturer lecturer =
                                lecturerCache.get(lecturerId);

                        if (lecturer == null) {
                            lecturer = lecturerData.findById(lecturerId);

                            if (lecturer != null) {
                                lecturerCache.put(lecturerId, lecturer);
                            }
                        }

                        if (lecturer != null) {
                            lecturerName = lecturer.getName();
                        }
                    }

                    // 7. Prepare time slot
                    Time startTime = entity.getStart_time();
                    Time endTime = entity.getEnd_time();

                    if (startTime == null || endTime == null) {
                        continue;
                    }

                    TimeSlot slot = new TimeSlot(
                            startTime.toLocalTime(),
                            endTime.toLocalTime()
                    );

                    timeSlots.add(slot);

                    // 8. Create course entry
                    String courseCode = course.getCourse_code();

                    String sessionType = entity.getSession_type();

                    String room = entity.getLecture_hall();

                    CourseEntry entry = new CourseEntry(
                            courseCode,
                            sessionType,
                            room,
                            lecturerName
                    );

                    timetableGrid
                            .get(day)
                            .computeIfAbsent(slot, k -> new ArrayList<>())
                            .add(entry);
                }
            }

        } catch (Exception e) {

            errorMessage = "Unable to load timetable: " + e.getMessage();

            e.printStackTrace();
        }
    }

    // Theory and Practical legend
    private JPanel createLegend() {

        JPanel legend = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 15, 0)
        );

        legend.setOpaque(false);

        JLabel theoryLbl = new JLabel(" Theory");
        theoryLbl.setIcon(new LegendIcon(false));
        theoryLbl.setFont(font(Font.PLAIN, 12));
        theoryLbl.setForeground(INK_2);

        JLabel practicalLbl = new JLabel(" Practical");
        practicalLbl.setIcon(new LegendIcon(true));
        practicalLbl.setFont(font(Font.PLAIN, 12));
        practicalLbl.setForeground(INK_2);

        legend.add(theoryLbl);
        legend.add(practicalLbl);

        return legend;
    }

    // Rebuild timetable grid
    private void rebuild() {

        gridPanel.removeAll();

        if (errorMessage != null) {
            showMessage(errorMessage);
            return;
        }

        if (timeSlots.isEmpty()) {
            showMessage("No published timetable entries found for your enrolled courses.");
            return;
        }

        GridBagConstraints gc = new GridBagConstraints();

        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(1, 1, 1, 1);

        // Empty top-left corner
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.weighty = 0;

        gridPanel.add(Box.createHorizontalStrut(100), gc);

        // Day headers
        for (int d = 0; d < DAYS.length; d++) {

            gc.gridx = d + 1;
            gc.gridy = 0;
            gc.weightx = 1;

            String day = DAYS[d];

            boolean isToday = isToday(day);

            JPanel headerCell = new JPanel(new BorderLayout());

            headerCell.setBackground(
                    isToday ? BRAND : Color.WHITE
            );

            headerCell.setBorder(
                    BorderFactory.createMatteBorder(
                            0, 0, 1, 0, LINE
                    )
            );

            headerCell.setPreferredSize(new Dimension(120, 45));

            JLabel label = new JLabel(
                    isToday ? shortDay(day) + " · TODAY" : shortDay(day),
                    SwingConstants.CENTER
            );

            label.setFont(font(Font.BOLD, 12));

            label.setForeground(
                    isToday ? Color.WHITE : INK_2
            );

            headerCell.add(label, BorderLayout.CENTER);

            gridPanel.add(headerCell, gc);
        }

        // Dynamic time slots
        int row = 1;

        for (TimeSlot slot : timeSlots) {

            gc.gridx = 0;
            gc.gridy = row;
            gc.weightx = 0;
            gc.weighty = 1;

            JPanel timeCell = new JPanel(new BorderLayout());

            timeCell.setBackground(Color.WHITE);

            timeCell.setBorder(
                    BorderFactory.createMatteBorder(
                            0, 0, 1, 1, LINE
                    )
            );

            JLabel timeLabel = new JLabel(
                    slot.toString(),
                    SwingConstants.CENTER
            );

            timeLabel.setFont(font(Font.PLAIN, 12));
            timeLabel.setForeground(INK_2);

            timeCell.add(timeLabel, BorderLayout.CENTER);

            gridPanel.add(timeCell, gc);

            // Course cells for each day
            for (int d = 0; d < DAYS.length; d++) {

                String day = DAYS[d];

                List<CourseEntry> entries =
                        timetableGrid.get(day).get(slot);

                gc.gridx = d + 1;
                gc.weightx = 1;

                gridPanel.add(createCourseCell(entries), gc);
            }

            row++;
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // Create a cell containing one or more courses
    private JPanel createCourseCell(List<CourseEntry> entries) {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 1, 1, LINE
                        ),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );

        panel.setBackground(Color.WHITE);

        if (entries == null || entries.isEmpty()) {
            return panel;
        }

        for (CourseEntry entry : entries) {

            boolean isPractical =
                    "PRACTICAL".equalsIgnoreCase(entry.sessionType);

            JPanel coursePanel = new JPanel(
                    new BorderLayout(5, 5)
            );

            coursePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            coursePanel.setBorder(
                    new EmptyBorder(5, 5, 8, 5)
            );

            coursePanel.setBackground(
                    isPractical ? BRAND_SOFT : Color.WHITE
            );

            // Course code and type badge
            JPanel top = new JPanel(
                    new FlowLayout(FlowLayout.LEFT, 6, 0)
            );

            top.setOpaque(false);

            JLabel code = new JLabel(entry.courseCode);
            code.setFont(font(Font.BOLD, 12));
            code.setForeground(INK);

            top.add(code);

            String badgeText = isPractical ? "P" : "T";

            JLabel badge = new JLabel(
                    badgeText,
                    SwingConstants.CENTER
            );

            badge.setFont(font(Font.BOLD, 10));
            badge.setPreferredSize(new Dimension(20, 20));
            badge.setOpaque(true);

            if (isPractical) {
                badge.setBackground(BRAND);
                badge.setForeground(Color.WHITE);
            } else {
                badge.setBackground(Color.WHITE);
                badge.setForeground(Color.DARK_GRAY);
                badge.setBorder(
                        BorderFactory.createLineBorder(Color.GRAY)
                );
            }

            top.add(badge);

            // Room and lecturer
            String details = (entry.room == null ? "Room not assigned" : entry.room)
                    + " · " + entry.lecturerName;

            JLabel location = new JLabel(details);
            location.setFont(font(Font.PLAIN, 11));
            location.setForeground(Color.GRAY);

            coursePanel.add(top, BorderLayout.NORTH);
            coursePanel.add(location, BorderLayout.CENTER);

            panel.add(coursePanel);
        }

        return panel;
    }

    private void showMessage(String message) {

        gridPanel.setLayout(new BorderLayout());

        JLabel label = new JLabel(
                "<html><div style='text-align:center; width:500px;'>"
                        + message +
                        "</div></html>",
                SwingConstants.CENTER
        );

        label.setFont(font(Font.PLAIN, 14));
        label.setForeground(INK_2);

        gridPanel.add(label, BorderLayout.CENTER);

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // Check actual current day
    private boolean isToday(String day) {

        DayOfWeek today = LocalDate.now().getDayOfWeek();

        return day.equals(today.name());
    }

    private String shortDay(String day) {
        return day.substring(0, 3);
    }

    // Time slot model
    private static class TimeSlot implements Comparable<TimeSlot> {

        private final LocalTime start;
        private final LocalTime end;

        public TimeSlot(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int compareTo(TimeSlot other) {

            int result = start.compareTo(other.start);

            if (result == 0) {
                return end.compareTo(other.end);
            }

            return result;
        }

        @Override
        public boolean equals(Object obj) {

            if (!(obj instanceof TimeSlot)) {
                return false;
            }

            TimeSlot other = (TimeSlot) obj;

            return start.equals(other.start)
                    && end.equals(other.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public String toString() {

            return start.format(TIME_FORMAT)
                    + " - "
                    + end.format(TIME_FORMAT);
        }
    }

    // Course details for UI
    private static class CourseEntry {

        private final String courseCode;
        private final String sessionType;
        private final String room;
        private final String lecturerName;

        public CourseEntry(
                String courseCode,
                String sessionType,
                String room,
                String lecturerName
        ) {
            this.courseCode = courseCode;
            this.sessionType = sessionType;
            this.room = room;
            this.lecturerName = lecturerName;
        }
    }

    // Custom legend icon
    private static class LegendIcon implements Icon {

        private final boolean filled;

        public LegendIcon(boolean filled) {
            this.filled = filled;
        }

        @Override
        public void paintIcon(
                Component c,
                Graphics g,
                int x,
                int y
        ) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (filled) {

                g2.setColor(BRAND);
                g2.fillRoundRect(x, y + 2, 12, 12, 4, 4);

            } else {

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y + 2, 12, 12, 4, 4);

                g2.setColor(Color.GRAY);
                g2.drawRoundRect(x, y + 2, 12, 12, 4, 4);
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 12;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }
}
