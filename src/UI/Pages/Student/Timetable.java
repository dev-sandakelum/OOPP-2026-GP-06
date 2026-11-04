
package UI.Pages.Student;

import UI.Kit.Glass;
import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import UI.Pages.Components.ShapeIcon;

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
import java.awt.geom.RoundRectangle2D;
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

    // cell tints: theory = same soft blue as the officer timetable, practical = a stronger blue
    private static final Color THEORY_FILL    = BRAND_SOFT;
    private static final Color PRACTICAL_FILL = Glass.alpha(BRAND_LT, 70);

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

        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setOpaque(false);

        container.add(createLegend(), BorderLayout.NORTH);

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

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

    // Theory / Practical legend (same tints as the cells)
    private JPanel createLegend() {

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        legend.setOpaque(false);

        legend.add(legendItem("Theory", THEORY_FILL));
        legend.add(legendItem("Practical", PRACTICAL_FILL));

        return legend;
    }

    private JLabel legendItem(String text, Color color) {
        JLabel l = new JLabel(text, ShapeIcon.square(color, 12), SwingConstants.LEFT);
        l.setIconTextGap(7);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(INK_2);
        return l;
    }

    // Rebuild timetable grid (same layout as the officer Timetables page)
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
        gc.insets = new Insets(5, 5, 5, 5);

        // top-left corner spacer + day headers
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; gc.weighty = 0;
        gridPanel.add(Box.createHorizontalStrut(110), gc);

        for (int d = 0; d < DAYS.length; d++) {
            gc.gridx = d + 1; gc.gridy = 0; gc.weightx = 1;
            gridPanel.add(dayHeader(DAYS[d]), gc);
        }

        // one row per time slot
        int row = 1;
        for (TimeSlot slot : timeSlots) {

            gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.weighty = 1;
            JLabel timeLabel = new JLabel(slot.toString());
            timeLabel.setFont(font(Font.BOLD, 13));
            timeLabel.setForeground(INK_2);
            gridPanel.add(timeLabel, gc);

            for (int d = 0; d < DAYS.length; d++) {
                gc.gridx = d + 1; gc.weightx = 1;
                gridPanel.add(slotCell(timetableGrid.get(DAYS[d]).get(slot)), gc);
            }
            row++;
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // "Monday"; today's column is shown in brand blue with a "Today" chip
    private JComponent dayHeader(String day) {

        String name = day.charAt(0) + day.substring(1).toLowerCase();

        JLabel l = new JLabel(name);
        l.setFont(font(Font.BOLD, 13));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);
        p.add(l);

        if (isToday(day)) {
            l.setForeground(BRAND);
            p.add(new Chip("Today", BRAND));
        } else {
            l.setForeground(INK_2);
        }

        // vertically centre the row
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(100, 30));
        wrap.add(p);
        return wrap;
    }

    // One grid slot: dashed "Free" box, or one tile per course in that slot
    private JComponent slotCell(List<CourseEntry> entries) {

        if (entries == null || entries.isEmpty()) {
            return new SessionTile(null);
        }
        if (entries.size() == 1) {
            return new SessionTile(entries.get(0));
        }

        JPanel stack = new JPanel(new GridLayout(0, 1, 0, 6));
        stack.setOpaque(false);
        for (CourseEntry entry : entries) {
            stack.add(new SessionTile(entry));
        }
        return stack;
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
        label.setBorder(new EmptyBorder(30, 10, 30, 10));

        gridPanel.add(label, BorderLayout.CENTER);

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // Check actual current day
    private boolean isToday(String day) {
        return day.equals(LocalDate.now().getDayOfWeek().name());
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


    // Read-only timetable tile, painted like the officer TimetableCell:
    // rounded tinted box when a course is booked, dashed "Free" box otherwise.
    private static class SessionTile extends JComponent {

        private final CourseEntry entry;   // null = free slot

        SessionTile(CourseEntry entry) {
            this.entry = entry;
            setOpaque(false);
            setPreferredSize(new Dimension(100, entry == null ? 76 : 92));
            if (entry != null) {
                String type = isPractical(entry) ? "Practical" : "Theory";
                setToolTipText(entry.courseCode + " · " + type + " · "
                        + roomOf(entry) + " · " + entry.lecturerName);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = Glass.aa(g);
            Shape shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 18, 18);
            int w = getWidth();

            if (entry == null) {
                g2.setColor(GLASS_FAINT);
                g2.fill(shape);
                g2.setColor(LINE_2);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10f, new float[]{4f, 4f}, 0f));
                g2.draw(shape);

                g2.setFont(font(Font.PLAIN, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(INK_2);
                g2.drawString("Free", (w - fm.stringWidth("Free")) / 2,
                        (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
                return;
            }

            boolean practical = isPractical(entry);
            g2.setColor(practical ? PRACTICAL_FILL : THEORY_FILL);
            g2.fill(shape);
            g2.setColor(GLASS_LINE);
            g2.draw(shape);

            // three centred lines: code / room · type / lecturer
            int cy = getHeight() / 2;

            g2.setFont(font(Font.BOLD, 14));
            g2.setColor(BRAND);
            drawCentred(g2, entry.courseCode, w, cy - 14);

            g2.setFont(font(Font.PLAIN, 12));
            g2.setColor(INK_2);
            drawCentred(g2, roomOf(entry) + "  ·  " + (practical ? "Practical" : "Theory"), w, cy + 4);

            g2.setFont(font(Font.PLAIN, 11));
            drawCentred(g2, entry.lecturerName, w, cy + 22);

            g2.dispose();
        }

        /** Draws text centred horizontally, cut with "…" if it does not fit. */
        private static void drawCentred(Graphics2D g2, String text, int width, int baseline) {
            if (text == null) return;
            FontMetrics fm = g2.getFontMetrics();
            int max = width - 16;
            if (fm.stringWidth(text) > max) {
                while (text.length() > 1 && fm.stringWidth(text + "…") > max) {
                    text = text.substring(0, text.length() - 1);
                }
                text = text + "…";
            }
            g2.drawString(text, (width - fm.stringWidth(text)) / 2, baseline);
        }

        private static boolean isPractical(CourseEntry e) {
            return "PRACTICAL".equalsIgnoreCase(e.sessionType);
        }

        private static String roomOf(CourseEntry e) {
            return e.room == null || e.room.isBlank() ? "Room TBA" : e.room;
        }
    }
}
