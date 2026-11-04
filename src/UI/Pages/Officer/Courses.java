package UI.Pages.Officer;

import UI.Kit.Backdrop;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.SectionCard;
import data.CourseData;
import data.DepartmentData;
import data.LecturerData;
import model.Course;
import model.Department;
import model.Lecturer;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// "Courses" tab for the technical officer AND the admin (AdminPages reuses this page):
// the list of scheduled courses. Both can view / edit / delete; only the admin gets the
// "Add new course" button (the officer DB account has no INSERT on course either, see sql/19).
// Add, View and Edit each open in their own window (the form lives in CourseForm).
public class Courses extends Page {

    private static final String[] COLS = {"Course ID", "Name", "Type", "Lecturer", "Dept", "Batch",
            "GPA", "Credits", "End / CA", "Actions"};
    private static final int COL_TYPE = 2, COL_ACTIONS = 9;
    private static final String[] ACTIONS = {"View", "Edit", "Delete"};

    private final CourseData courseData = new CourseData();

    private final List<Lecturer> lecturers = new ArrayList<>();
    private final List<Department> departments = new ArrayList<>();
    private final List<Course> courses = new ArrayList<>();   // one per table row (model order)

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
    private final JTable table;

    public Courses(User user) {
        super("Courses", "Logged in as: " + (user == null ? "Officer" : user.getName()), null);

        loadLookups();

        // only admins may create courses; officers manage existing ones
        boolean canCreate = user != null && "ADMIN".equalsIgnoreCase(user.getUserType());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false);
        if (canCreate) {
            RoundedButton add = new RoundedButton("+ Add new course", true);
            add.addActionListener(e -> openForm(null));
            top.add(add);
        }

        table = table(model, Map.of("Theory", BRAND, "Practical", OK), COL_TYPE);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(COL_ACTIONS).setPreferredWidth(230);
        table.getColumnModel().getColumn(COL_ACTIONS).setCellRenderer(new ActionsRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onTableClick(e); }
        });

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(tableCard(table), BorderLayout.CENTER);
        setBody(body);

        reload();
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    // -----------------------------------------------------------------
    //  Data
    // -----------------------------------------------------------------

    private void loadLookups() {
        try {
            lecturers.addAll(new LecturerData().findAll());
            departments.addAll(new DepartmentData().findAll());
        } catch (Exception ex) {
            info(this, "Could not load lecturers / departments: " + ex.getMessage());
        }
    }

    private void reload() {
        model.setRowCount(0);
        courses.clear();
        try {
            for (Course c : courseData.findAll()) {
                courses.add(c);
                model.addRow(new Object[]{
                        c.getCourse_code(), c.getCourse_name(), nz(c.getCourse_type()),
                        CourseForm.lecturerCode(c.getLecturer_id()), deptShort(c.getDep_id()),
                        nz(c.getBatch()), nz(c.getGpa_type()), creditText(c.getCredit()),
                        c.getEnd_weight() + " / " + c.getCa_weight(), ""});
            }
        } catch (Exception ex) {
            info(this, "Could not load courses: " + ex.getMessage());
        }
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String creditText(double c) {
        return c == Math.rint(c) ? String.valueOf((int) c) : String.valueOf(c);
    }

    private String lecturerName(Integer id) {
        if (id == null) return "Unassigned";
        for (Lecturer l : lecturers) if (l.getUser_id() == id) return CourseForm.lecturerText(l);
        return CourseForm.lecturerCode(id);
    }

    private String deptName(Integer id) {
        if (id == null) return "-";
        for (Department d : departments) if (d.getDep_id() == id) return d.getDep_name();
        return "-";
    }

    // "Information and Communication Technology" -> ICT, "Bio-system Technology" -> BST
    private String deptShort(Integer depId) {
        String name = deptName(depId);
        if (name.equals("-")) return name;
        StringBuilder sb = new StringBuilder();
        for (String w : name.split("[\\s-]+"))
            if (!w.isEmpty() && !w.equalsIgnoreCase("and")) sb.append(Character.toUpperCase(w.charAt(0)));
        return sb.toString();
    }

    // -----------------------------------------------------------------
    //  Actions
    // -----------------------------------------------------------------

    // View | Edit | Delete thirds of the Actions cell
    private void onTableClick(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int col = table.columnAtPoint(e.getPoint());
        if (row < 0 || col != COL_ACTIONS) return;
        Course c = courses.get(table.convertRowIndexToModel(row));
        Rectangle r = table.getCellRect(row, col, false);
        int third = (e.getX() - r.x) * ACTIONS.length / Math.max(r.width, 1);
        switch (third) {
            case 0 -> openView(c);
            case 1 -> openForm(c);
            default -> delete(c);
        }
    }

    private void delete(Course c) {
        if (!confirm(this, "Delete " + c.getCourse_code() + "?")) return;
        try {
            courseData.delete(c.getCourse_id());
        } catch (Exception ex) {
            info(this, "Could not delete the course: " + ex.getMessage());
            return;
        }
        reload();
    }

    // -----------------------------------------------------------------
    //  Add / Edit window (c == null: schedule a new course)
    // -----------------------------------------------------------------

    private void openForm(Course c) {
        boolean adding = c == null;
        CourseForm f = new CourseForm(lecturers, departments);
        if (!adding) f.load(c);

        JFrame win = window(adding ? "Add Course" : "Edit Course · " + c.getCourse_code(), 760, 860);

        RoundedButton save = new RoundedButton(adding ? "Schedule Course" : "Update Course", true);
        RoundedButton cancel = new RoundedButton("Cancel", false);
        f.setOnChange(() -> save.setEnabled(f.isComplete()));
        save.setEnabled(f.isComplete());
        cancel.addActionListener(e -> win.dispose());
        save.addActionListener(e -> {
            Course course = f.fill(new Course());
            try {
                if (adding) {
                    courseData.insertFull(course);
                } else {
                    course.setCourse_id(c.getCourse_id());
                    courseData.updateFull(course);
                }
            } catch (Exception ex) {
                info(f, "Could not save the course: " + ex.getMessage());
                return;
            }
            win.dispose();
            reload();
        });

        String title = adding ? "Schedule a Course" : "Edit Course";
        String sub = adding ? "Fill in the course details and evaluation criteria"
                : c.getCourse_code() + " — " + c.getCourse_name();
        win.setContentPane(frameBody(title, sub, f, cancel, save));
        win.setVisible(true);
    }

    // -----------------------------------------------------------------
    //  View window
    // -----------------------------------------------------------------

    private void openView(Course c) {
        JPanel details = new JPanel(new GridLayout(0, 2, 20, 12));
        details.setOpaque(false);
        row(details, "Course ID", c.getCourse_code());
        row(details, "Course Name", c.getCourse_name());
        row(details, "Course Type", nz(c.getCourse_type()));
        row(details, "Lecturer", lecturerName(c.getLecturer_id()));
        row(details, "Department", deptName(c.getDep_id()));
        row(details, "Batch", nz(c.getBatch()));
        row(details, "GPA / NGPA", nz(c.getGpa_type()));
        row(details, "Credits", creditText(c.getCredit()));

        JPanel weights = new JPanel(new GridLayout(0, 2, 20, 12));
        weights.setOpaque(false);
        row(weights, "End Examination", c.getEnd_weight() + " %");
        row(weights, "Continuous Assessment", c.getCa_weight() + " %");
        int[] w = {c.getQuiz_weight(), c.getAssignment_weight(), c.getProject_weight(), c.getMid_weight()};
        for (int i = 0; i < w.length; i++) row(weights, CourseForm.CA_NAMES[i], w[i] + " %");

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(left(new SectionCard("Course Details", null, details)));
        body.add(Box.createVerticalStrut(16));
        body.add(left(new SectionCard("Evaluation Criteria", null, weights)));

        JFrame win = window("Course · " + c.getCourse_code(), 680, 640);
        RoundedButton edit = new RoundedButton("Edit", false);
        edit.addActionListener(e -> { win.dispose(); openForm(c); });
        RoundedButton close = new RoundedButton("Close", true);
        close.addActionListener(e -> win.dispose());
        win.setContentPane(frameBody(c.getCourse_code(), c.getCourse_name(), body, edit, close));
        win.setVisible(true);
    }

    private static void row(JPanel p, String name, String value) {
        JLabel v = new JLabel(value == null || value.isBlank() ? "-" : value);
        v.setFont(font(Font.PLAIN, 15));
        v.setForeground(INK);
        JPanel cell = new JPanel(new BorderLayout(0, 3));
        cell.setOpaque(false);
        cell.add(label(name), BorderLayout.NORTH);
        cell.add(v, BorderLayout.CENTER);
        p.add(cell);
    }

    // -----------------------------------------------------------------
    //  Window helpers (same backdrop + glass look as the main frames)
    // -----------------------------------------------------------------

    private JFrame window(String title, int w, int h) {
        JFrame win = new JFrame("AFMS — " + title);
        win.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        win.setSize(w, h);
        win.setMinimumSize(new Dimension(560, 480));
        win.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        try {
            win.setIconImage(new ImageIcon(Objects.requireNonNull(
                    getClass().getResource("/assets/img.png"))).getImage());
        } catch (RuntimeException ignored) { }
        return win;
    }

    private static JComponent frameBody(String title, String sub, JComponent content, JButton... buttons) {
        Page page = new Page(title, sub, null);
        page.setScrollBody(content);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 28, 20, 28));
        for (JButton b : buttons) footer.add(b);

        Backdrop.Panel root = new Backdrop.Panel(new BorderLayout());
        root.add(page, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    // -----------------------------------------------------------------
    //  "View | Edit | Delete" cell
    // -----------------------------------------------------------------

    private static final class ActionsRenderer extends JPanel implements TableCellRenderer {
        ActionsRenderer() {
            super(new GridLayout(1, ACTIONS.length, 6, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(4, 0, 2, 4));
            for (String a : ACTIONS) {
                RoundedButton b = new RoundedButton(a, false);
                b.setBorder(new EmptyBorder(4, 8, 6, 8));
                add(b);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            return this;
        }
    }
}
